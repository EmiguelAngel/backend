package com.sistemaventas.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentRefundClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.PaymentRefund;
import com.sistemaventas.backend.dto.DevolucionDTO;
import com.sistemaventas.backend.dto.DevolucionRequestDTO;
import com.sistemaventas.backend.entity.DetalleFactura;
import com.sistemaventas.backend.entity.Devolucion;
import com.sistemaventas.backend.entity.Factura;
import com.sistemaventas.backend.entity.Producto;
import com.sistemaventas.backend.exception.ResourceNotFoundException;
import com.sistemaventas.backend.repository.DetalleFacturaRepository;
import com.sistemaventas.backend.repository.DevolucionRepository;
import com.sistemaventas.backend.repository.FacturaRepository;
import com.sistemaventas.backend.repository.ProductoRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DevolucionService {

    @Autowired
    private DevolucionRepository devolucionRepository;

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private DetalleFacturaRepository detalleFacturaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Value("${mercadopago.access.token}")
    private String mercadoPagoAccessToken;

    /**
     * Procesa una devolución completa:
     * 1. Valida la factura
     * 2. Procesa el reembolso en Mercado Pago
     * 3. Restaura el inventario
     * 4. Marca la factura como devuelta
     * 5. Registra la devolución
     */
    @Transactional
    public DevolucionDTO procesarDevolucion(DevolucionRequestDTO request) {
        log.info("Iniciando proceso de devolución para factura ID: {}", request.getIdFactura());

        // 1. Validar factura
        Factura factura = facturaRepository.findById(request.getIdFactura())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Factura no encontrada con ID: " + request.getIdFactura()));

        // Verificar que no esté ya devuelta
        if (Boolean.TRUE.equals(factura.getDevuelta())) {
            throw new IllegalStateException("Esta factura ya fue devuelta anteriormente");
        }

        // Determinar si la factura fue pagada con Mercado Pago
        boolean tienePagoMercadoPago = factura.getPaymentId() != null 
                && !factura.getPaymentId().isEmpty() 
                && esPaymentIdValido(factura.getPaymentId());

        String refundId = null;

        // 2. Procesar reembolso en Mercado Pago (solo si aplica)
        if (tienePagoMercadoPago) {
            log.info("Factura pagada con Mercado Pago. Procesando reembolso...");
            refundId = procesarReembolsoMercadoPago(factura.getPaymentId());
        } else {
            log.info("Factura sin pago de Mercado Pago. Solo se restaurará inventario.");
        }

        // 3. Restaurar inventario
        restaurarInventario(factura);

        // 4. Marcar factura como devuelta
        factura.setDevuelta(true);
        facturaRepository.save(factura);

        // 5. Registrar la devolución
        Devolucion devolucion = Devolucion.builder()
                .factura(factura)
                .paymentId(factura.getPaymentId())
                .refundId(refundId)
                .montoDevuelto(factura.getTotal())
                .motivo(request.getMotivo())
                .estado("APROBADA")
                .fechaDevolucion(LocalDateTime.now())
                .usuarioDevolucion(request.getUsuarioDevolucion())
                .build();

        devolucion = devolucionRepository.save(devolucion);

        log.info("Devolución procesada exitosamente. ID: {}, RefundID: {}", 
                devolucion.getIdDevolucion(), refundId);

        return convertirADTO(devolucion);
    }

    /**
     * Procesa el reembolso en Mercado Pago
     */
    private String procesarReembolsoMercadoPago(String paymentId) {
        try {
            log.info("Procesando reembolso en Mercado Pago para payment_id: {}", paymentId);

            // Configurar Mercado Pago
            MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken);

            // Crear cliente de reembolso
            PaymentRefundClient refundClient = new PaymentRefundClient();

            // Procesar reembolso total
            PaymentRefund refund = refundClient.refund(Long.valueOf(paymentId));

            log.info("Reembolso procesado exitosamente. Refund ID: {}, Status: {}", 
                    refund.getId(), refund.getStatus());

            return refund.getId().toString();

        } catch (MPException | MPApiException e) {
            log.error("Error al procesar reembolso en Mercado Pago: {}", e.getMessage(), e);
            throw new RuntimeException(
                    "Error al procesar el reembolso con Mercado Pago: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            log.error("Payment ID inválido: {}", paymentId, e);
            throw new IllegalArgumentException("El ID de pago de Mercado Pago es inválido", e);
        }
    }

    /**
     * Restaura el inventario de los productos de la factura
     */
    private void restaurarInventario(Factura factura) {
        log.info("Restaurando inventario para factura ID: {}", factura.getIdFactura());

        List<DetalleFactura> detalles = detalleFacturaRepository.findByFactura_IdFactura(factura.getIdFactura());

        for (DetalleFactura detalle : detalles) {
            Producto producto = detalle.getProducto();
            int cantidadAnterior = producto.getCantidadDisponible();
            int nuevaCantidad = cantidadAnterior + detalle.getCantidad();

            log.info("Restaurando producto ID: {} - Cantidad anterior: {}, Devolver: {}, Nueva cantidad: {}",
                    producto.getIdProducto(), cantidadAnterior, detalle.getCantidad(), nuevaCantidad);

            producto.setCantidadDisponible(nuevaCantidad);
            productoRepository.save(producto);
        }

        log.info("Inventario restaurado exitosamente para {} productos", detalles.size());
    }

    /**
     * Obtiene todas las devoluciones
     */
    public List<DevolucionDTO> obtenerTodasLasDevoluciones() {
        return devolucionRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las devoluciones de una factura específica
     */
    public List<DevolucionDTO> obtenerDevolucionesPorFactura(Integer idFactura) {
        return devolucionRepository.findByFactura_IdFactura(idFactura).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las devoluciones por estado
     */
    public List<DevolucionDTO> obtenerDevolucionesPorEstado(String estado) {
        return devolucionRepository.findByEstado(estado).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una devolución por su ID
     */
    public DevolucionDTO obtenerDevolucionPorId(Long id) {
        Devolucion devolucion = devolucionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Devolución no encontrada con ID: " + id));
        return convertirADTO(devolucion);
    }

    /**
     * Convierte una entidad Devolucion a DTO
     */
    private DevolucionDTO convertirADTO(Devolucion devolucion) {
        return DevolucionDTO.builder()
                .idDevolucion(devolucion.getIdDevolucion())
                .idFactura(devolucion.getFactura().getIdFactura())
                .numeroFactura("FAC-" + devolucion.getFactura().getIdFactura())
                .paymentId(devolucion.getPaymentId())
                .refundId(devolucion.getRefundId())
                .montoDevuelto(devolucion.getMontoDevuelto())
                .motivo(devolucion.getMotivo())
                .estado(devolucion.getEstado())
                .fechaDevolucion(devolucion.getFechaDevolucion())
                .usuarioDevolucion(devolucion.getUsuarioDevolucion())
                .build();
    }

    /**
     * Valida que el payment_id sea un número válido de Mercado Pago
     * Los payment_ids reales son números largos, no strings con prefijos como "TEST_MP_"
     */
    private boolean esPaymentIdValido(String paymentId) {
        if (paymentId == null || paymentId.trim().isEmpty()) {
            return false;
        }

        // Rechazar IDs de prueba conocidos
        if (paymentId.startsWith("TEST_") || paymentId.startsWith("test_")) {
            return false;
        }

        // Verificar que sea un número válido
        try {
            Long.parseLong(paymentId);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
