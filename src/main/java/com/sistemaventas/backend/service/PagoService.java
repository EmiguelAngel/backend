package com.sistemaventas.backend.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemaventas.backend.dto.request.VentaRequest;
import com.sistemaventas.backend.entity.Factura;
import com.sistemaventas.backend.entity.Pago;
import com.sistemaventas.backend.repository.PagoRepository;

@Service
@Transactional
/**
 * Service (Business layer): contiene la lógica de procesamiento de pagos.
 * En el patrón MVC esta capa implementa la lógica de negocio y actúa como
 * intermediaria entre los controladores (HTTP) y los repositorios (persistencia).
 */
public class PagoService {
    
    @Autowired
    private PagoRepository pagoRepository;
    
    // Procesar pago para una venta
    public Pago procesarPago(VentaRequest.DatosPago datosPago, BigDecimal monto, Factura factura) {
        try {
            // Validar datos de pago
            validarDatosPago(datosPago, monto);
            
            // Simular procesamiento del pago según método
            boolean pagoExitoso = procesarPagoSegunMetodo(datosPago, monto);
            
            if (!pagoExitoso) {
                throw new RuntimeException("Error al procesar el pago con " + datosPago.getMetodoPago());
            }
            
            // Crear registro de pago
            Pago pago = new Pago();
            pago.setIdPago(generarIdPago());
            pago.setFactura(factura);
            pago.setMetodoPago(datosPago.getMetodoPago());
            pago.setMonto(monto);
            
            // Agregar datos del titular si es pago con tarjeta
            String metodo = datosPago.getMetodoPago().toLowerCase();
            if (metodo.contains("tarjeta") || metodo.contains("credito") || metodo.contains("debito")) {
                pago.setNombreTitular(datosPago.getNombreTitular());
                // Solo almacenar los últimos 4 dígitos por seguridad
                if (datosPago.getNumeroTarjeta() != null) {
                    String numeroTarjeta = datosPago.getNumeroTarjeta().replaceAll("\\s", "");
                    if (numeroTarjeta.length() >= 4) {
                        pago.setNumeroTarjeta("****" + numeroTarjeta.substring(numeroTarjeta.length() - 4));
                    }
                }
            }
            
            return pagoRepository.save(pago);
            
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Error de validación de pago: " + e.getMessage());
        } catch (RuntimeException e) {
            throw new RuntimeException("Error al procesar pago: " + e.getMessage());
        }
    }
    
    // Obtener todos los pagos
    public List<Pago> obtenerTodosLosPagos() {
        return pagoRepository.findAll();
    }
    
    // Buscar pago por ID
    public Optional<Pago> buscarPorId(Integer id) {
        return pagoRepository.findById(id);
    }
    
    // Buscar pagos por método
    public List<Pago> buscarPorMetodo(String metodoPago) {
        return pagoRepository.findByMetodoPago(metodoPago);
    }
    
    // Validar datos de pago
    private void validarDatosPago(VentaRequest.DatosPago datosPago, BigDecimal monto) {
        if (datosPago == null) {
            throw new IllegalArgumentException("Los datos de pago son obligatorios");
        }
        
        if (datosPago.getMetodoPago() == null || datosPago.getMetodoPago().trim().isEmpty()) {
            throw new IllegalArgumentException("El método de pago es obligatorio");
        }
        
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }
        
        // Validaciones específicas por método de pago
        String metodo = datosPago.getMetodoPago().toLowerCase();
        
        // Mercado Pago no requiere validación de tarjeta porque ya fue procesado
        if (metodo.equals("mercado_pago") || metodo.equals("mercado pago")) {
            return; // No validar datos de tarjeta
        }
        
        if (metodo.contains("tarjeta") || metodo.contains("credito") || metodo.contains("debito")) {
            validarDatosTarjeta(datosPago);
        }
    }
    
    // Validar datos específicos de tarjeta
    private void validarDatosTarjeta(VentaRequest.DatosPago datosPago) {
        if (datosPago.getNumeroTarjeta() == null || datosPago.getNumeroTarjeta().length() < 13) {
            throw new IllegalArgumentException("Número de tarjeta inválido");
        }
        
        if (datosPago.getNombreTitular() == null || datosPago.getNombreTitular().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del titular es obligatorio");
        }
        
        if (datosPago.getCodigoSeguridad() == null || datosPago.getCodigoSeguridad().length() < 3) {
            throw new IllegalArgumentException("Código de seguridad inválido");
        }
    }
    
    // Simular procesamiento de pago según método
    private boolean procesarPagoSegunMetodo(VentaRequest.DatosPago datosPago, BigDecimal monto) {
        String metodo = datosPago.getMetodoPago().toLowerCase();
        
        System.out.println("💳 Procesando pago: " + datosPago.getMetodoPago() + " por $" + monto);
        
        switch (metodo) {
            case "efectivo" -> {
                return procesarPagoEfectivo(monto);
            }
                
            case "tarjeta credito", "tarjeta crédito", "tarjeta_credito" -> {
                return procesarPagoTarjetaCredito(datosPago, monto);
            }
            case "tarjeta debito", "tarjeta débito", "tarjeta_debito" -> {
                return procesarPagoTarjetaDebito(datosPago, monto);
            }
                
            case "transferencia" -> {
                return procesarPagoTransferencia(monto);
            }
            
            case "mercado_pago", "mercado pago" -> {
                return procesarPagoMercadoPago(monto);
            }
            
            default -> {
                System.out.println("⚠️ Método de pago no reconocido, procesando como genérico");
                return true; // Por defecto aceptar
            }
        }
    }
    
    private boolean procesarPagoEfectivo(BigDecimal monto) {
        System.out.println("💵 Procesando pago en efectivo por $" + monto);
        // Siempre exitoso para efectivo
        System.out.println("✅ Pago en efectivo procesado exitosamente");
        return true;
    }
    
    private boolean procesarPagoTarjetaCredito(VentaRequest.DatosPago datosPago, BigDecimal monto) {
        System.out.println("💳 Procesando tarjeta de crédito:");
        System.out.println("   Titular: " + datosPago.getNombreTitular());
        System.out.println("   Número: ****" + datosPago.getNumeroTarjeta().substring(Math.max(0, datosPago.getNumeroTarjeta().length() - 4)));
        System.out.println("   Monto: $" + monto);
        
        // Simular validación con banco (aquí iría integración real)
        simulateDelay(2000); // Simular tiempo de procesamiento
        
        // 95% de éxito para simulación
        boolean exitoso = ThreadLocalRandom.current().nextDouble() > 0.05;
        
        if (exitoso) {
            System.out.println("✅ Pago con tarjeta de crédito aprobado");
        } else {
            System.out.println("❌ Pago con tarjeta de crédito rechazado");
        }
        
        return exitoso;
    }
    
    private boolean procesarPagoTarjetaDebito(VentaRequest.DatosPago datosPago, BigDecimal monto) {
        System.out.println("💳 Procesando tarjeta de débito:");
        System.out.println("   Titular: " + datosPago.getNombreTitular());
        System.out.println("   Número: ****" + datosPago.getNumeroTarjeta().substring(Math.max(0, datosPago.getNumeroTarjeta().length() - 4)));
        System.out.println("   Monto: $" + monto);
        
        // Simular validación de fondos
        simulateDelay(1500);
        
        // 90% de éxito para débito
        boolean exitoso = ThreadLocalRandom.current().nextDouble() > 0.10;
        
        if (exitoso) {
            System.out.println("✅ Pago con tarjeta de débito aprobado");
        } else {
            System.out.println("❌ Pago con tarjeta de débito rechazado (fondos insuficientes)");
        }
        
        return exitoso;
    }
    
    private boolean procesarPagoTransferencia(BigDecimal monto) {
        System.out.println("🏦 Procesando transferencia bancaria por $" + monto);
        
        // Simular tiempo de procesamiento bancario
        simulateDelay(3000);
        
        // 98% de éxito para transferencias
        boolean exitoso = ThreadLocalRandom.current().nextDouble() > 0.02;
        
        if (exitoso) {
            System.out.println("✅ Transferencia bancaria procesada exitosamente");
        } else {
            System.out.println("❌ Error en transferencia bancaria");
        }
        
        return exitoso;
    }
    
    private boolean procesarPagoMercadoPago(BigDecimal monto) {
        System.out.println("🔵 Procesando pago con Mercado Pago:");
        System.out.println("   Monto: $" + monto);
        
        // El pago ya fue aprobado por Mercado Pago, solo registramos
        System.out.println("✅ Pago con Mercado Pago confirmado");
        return true;
    }
    
    // Simular delay de procesamiento
    private void simulateDelay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    // Generar ID de pago
    private Integer generarIdPago() {
        List<Pago> pagos = pagoRepository.findAll();
        if (pagos.isEmpty()) {
            return 1;
        }
        return pagos.stream()
                .mapToInt(Pago::getIdPago)
                .max()
                .orElse(0) + 1;
    }
    
    // Obtener estadísticas de pagos por método
    public void mostrarEstadisticasPagos() {
        System.out.println("=== ESTADÍSTICAS DE PAGOS ===");
        
        List<String> metodos = pagoRepository.findDistinctMetodosPago();
        
        for (String metodo : metodos) {
            List<Pago> pagosPorMetodo = pagoRepository.findByMetodoPago(metodo);
            BigDecimal total = pagosPorMetodo.stream()
                    .map(Pago::getMonto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            System.out.println(metodo + ": " + pagosPorMetodo.size() + " pagos, Total: $" + total);
        }
        
        System.out.println("=== FIN ESTADÍSTICAS ===");
    }
}