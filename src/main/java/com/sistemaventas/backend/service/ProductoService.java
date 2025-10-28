package com.sistemaventas.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemaventas.backend.dto.request.ProductoRequest;
import com.sistemaventas.backend.entity.Producto;
import com.sistemaventas.backend.factory.ProductoFactory;
import com.sistemaventas.backend.observer.InventarioNotificationService;
import com.sistemaventas.backend.repository.ProductoRepository;

@Service
@Transactional
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InventarioNotificationService notificationService;

    // Crear producto usando Factory Method Pattern
    @Transactional
    public Producto crearProducto(ProductoRequest productoRequest) {
        System.out.println("=== INICIO SERVICIO CREAR PRODUCTO ===");
        try {
            System.out.println("Request recibido en servicio: " + productoRequest);
            
            // Validaciones básicas
            if (productoRequest == null) {
                throw new IllegalArgumentException("El request no puede ser null");
            }
            
            if (productoRequest.getDescripcion() == null || productoRequest.getDescripcion().trim().isEmpty()) {
                throw new IllegalArgumentException("La descripción del producto es obligatoria");
            }
            
            if (productoRequest.getPrecioUnitario() == null || productoRequest.getPrecioUnitario().doubleValue() <= 0) {
                throw new IllegalArgumentException("El precio unitario debe ser mayor a 0");
            }
            
            if (productoRequest.getCantidadDisponible() == null || productoRequest.getCantidadDisponible() < 0) {
                throw new IllegalArgumentException("La cantidad disponible no puede ser negativa");
            }
            
            if (productoRequest.getCategoria() == null || productoRequest.getCategoria().trim().isEmpty()) {
                throw new IllegalArgumentException("La categoría es obligatoria");
            }
            
            // Generar nuevo ID si no se proporciona o es 0
            if (productoRequest.getIdProducto() == null || productoRequest.getIdProducto() == 0) {
                Integer nuevoId = generarNuevoId();
                System.out.println("ProductoService: Generando nuevo ID: " + nuevoId);
                productoRequest.setIdProducto(nuevoId);
            } else {
                // Verificar que no exista ya un producto con ese ID
                if (productoRepository.existsById(productoRequest.getIdProducto())) {
                    throw new IllegalArgumentException("Ya existe un producto con ID: " + productoRequest.getIdProducto());
                }
            }

            // Usar Factory Method para crear el producto según la categoría
            System.out.println("ProductoService: Creando factory para categoría: " + productoRequest.getCategoria());
            ProductoFactory productoFactory;
            try {
                productoFactory = ProductoFactory.obtenerFactory(productoRequest.getCategoria());
            } catch (Exception e) {
                throw new IllegalArgumentException("Categoría de producto no válida: " + productoRequest.getCategoria());
            }
            
            System.out.println("ProductoService: Creando producto usando factory");
            Producto producto;
            try {
                producto = productoFactory.crearProducto(productoRequest);
            } catch (IllegalArgumentException e) {
                throw e; // Reenviar errores de validación
            } catch (Exception e) {
                throw new RuntimeException("Error al crear el producto usando factory: " + e.getMessage());
            }
            
            if (producto == null) {
                throw new RuntimeException("Error: el factory no pudo crear el producto");
            }
            
            // Guardar en la base de datos
            System.out.println("ProductoService: Guardando producto en base de datos");
            Producto productoGuardado;
            try {
                productoGuardado = productoRepository.save(producto);
            } catch (Exception e) {
                throw new RuntimeException("Error al guardar el producto en la base de datos: " + e.getMessage());
            }
            
            System.out.println("ProductoService: Producto guardado exitosamente con ID: " + productoGuardado.getIdProducto());
            return productoGuardado;
            
        } catch (IllegalArgumentException e) {
            String mensaje = "Error de validación al crear producto: " + e.getMessage();
            System.err.println("ProductoService: " + mensaje);
            throw e;
        } catch (RuntimeException e) {
            System.err.println("ProductoService: Error interno - " + e.getMessage());
            throw e;
        } catch (Exception e) {
            String mensaje = "Error inesperado al crear producto: " + e.getMessage();
            System.err.println("ProductoService: " + mensaje);
            System.err.println("ProductoService: Tipo de error: " + e.getClass().getSimpleName());
            throw new RuntimeException(mensaje, e);
        } finally {
            System.out.println("=== FIN SERVICIO CREAR PRODUCTO ===");
        }
    }

    // Obtener todos los productos
    public List<Producto> obtenerTodosLosProductos() {
        return productoRepository.findAll();
    }

    // Buscar producto por ID
    public Optional<Producto> buscarPorId(Integer id) {
        return productoRepository.findById(id);
    }

    // Buscar productos por categoría
    public List<Producto> buscarPorCategoria(String categoria) {
        return productoRepository.findByCategoria(categoria);
    }

    // Buscar productos por descripción (búsqueda parcial)
    public List<Producto> buscarPorDescripcion(String descripcion) {
        return productoRepository.findByDescripcionContainingIgnoreCase(descripcion);
    }
    
    // Buscar productos por término general (descripción o categoría)
    public List<Producto> buscarProductosPorTermino(String termino) {
        return productoRepository.findByDescripcionContainingIgnoreCaseOrCategoriaContainingIgnoreCase(termino, termino);
    }

    // Buscar productos con stock disponible
    public List<Producto> buscarProductosConStock() {
        return productoRepository.findProductosConStock();
    }

    // Buscar productos con stock bajo
    public List<Producto> buscarProductosConStockBajo(Integer cantidadMinima) {
        return productoRepository.findProductosConStockBajo(cantidadMinima);
    }

    // Actualizar producto
    public Producto actualizarProducto(Integer id, ProductoRequest productoRequest) {
        System.out.println("=== ACTUALIZAR PRODUCTO ===");
        System.out.println("ID a actualizar: " + id);
        System.out.println("Request: " + productoRequest);
        
        Optional<Producto> productoExistente = productoRepository.findById(id);

        if (productoExistente.isEmpty()) {
            System.err.println("❌ ERROR: Producto no encontrado con ID: " + id);
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }
        
        System.out.println("✅ Producto encontrado: " + productoExistente.get().getDescripcion());

        try {
            // Crear producto actualizado usando Factory
            productoRequest.setIdProducto(id);
            System.out.println("Obteniendo factory para categoría: " + productoRequest.getCategoria());
            ProductoFactory factory = ProductoFactory.obtenerFactory(productoRequest.getCategoria());
            
            System.out.println("Creando producto actualizado con factory");
            Producto productoActualizado = factory.crearProducto(productoRequest);
            
            System.out.println("Guardando producto actualizado en BD");
            Producto resultado = productoRepository.save(productoActualizado);
            System.out.println("✅ Producto actualizado exitosamente");
            
            return resultado;
        } catch (Exception e) {
            System.err.println("❌ ERROR actualizando producto: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error actualizando producto: " + e.getMessage(), e);
        }
    }

    // Eliminar producto
    public boolean eliminarProducto(Integer id) {
        System.out.println("=== ELIMINAR PRODUCTO ===");
        System.out.println("ID a eliminar: " + id);
        
        try {
            if (productoRepository.existsById(id)) {
                System.out.println("✅ Producto existe, procediendo a eliminar");
                productoRepository.deleteById(id);
                System.out.println("✅ Producto eliminado exitosamente");
                return true;
            } else {
                System.err.println("❌ Producto no encontrado con ID: " + id);
                return false;
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR eliminando producto: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error eliminando producto: " + e.getMessage(), e);
        }
    }

    // Actualizar stock de producto (CON OBSERVER PATTERN)
    public Producto actualizarStock(Integer id, Integer nuevaCantidad) {
        Optional<Producto> productoOpt = productoRepository.findById(id);

        if (productoOpt.isEmpty()) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }

        Producto producto = productoOpt.get();
        int stockAnterior = producto.getCantidadDisponible();
        producto.setCantidadDisponible(nuevaCantidad);

        Producto productoActualizado = productoRepository.save(producto);

        // NOTIFICAR A OBSERVADORES
        notificationService.procesarCambioStock(productoActualizado, stockAnterior);

        return productoActualizado;
    }

    // Reducir stock (para ventas) - CON OBSERVER PATTERN
    public Producto reducirStock(Integer id, Integer cantidad) {
        Optional<Producto> productoOpt = productoRepository.findById(id);

        if (productoOpt.isEmpty()) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }

        Producto producto = productoOpt.get();
        int stockAnterior = producto.getCantidadDisponible();

        if (!producto.tieneStockSuficiente(cantidad)) {
            throw new RuntimeException(
                    String.format("Stock insuficiente para producto %s. Disponible: %d, Solicitado: %d",
                            producto.getDescripcion(), producto.getCantidadDisponible(), cantidad)
            );
        }

        producto.reducirStock(cantidad);
        Producto productoActualizado = productoRepository.save(producto);

        // NOTIFICAR A OBSERVADORES
        notificationService.procesarCambioStock(productoActualizado, stockAnterior);

        return productoActualizado;
    }

    // Aumentar stock (para compras/devoluciones) - CON OBSERVER PATTERN
    public Producto aumentarStock(Integer id, Integer cantidad) {
        Optional<Producto> productoOpt = productoRepository.findById(id);

        if (productoOpt.isEmpty()) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }

        Producto producto = productoOpt.get();
        int stockAnterior = producto.getCantidadDisponible();

        producto.aumentarStock(cantidad);
        Producto productoActualizado = productoRepository.save(producto);

        // NOTIFICAR A OBSERVADORES
        notificationService.procesarCambioStock(productoActualizado, stockAnterior);

        return productoActualizado;
    }

    // Obtener categorías disponibles
    public List<String> obtenerCategorias() {
        return productoRepository.findDistinctCategorias();
    }

    // Método auxiliar para generar nuevo ID
    private Integer generarNuevoId() {
        try {
            Integer maxId = productoRepository.findMaxId();
            if (maxId == null || maxId == 0) {
                return 1;
            }
            return maxId + 1;
        } catch (Exception e) {
            // En caso de cualquier problema con la consulta, caer en un fallback
            System.err.println("ProductoService: No se pudo obtener maxId desde repositorio, usando fallback (1)");
            e.printStackTrace();
            return 1;
        }
    }

    // Métodos de demostración (opcionales)
    public void demostrarFactoryPattern() {
        System.out.println("=== DEMOSTRACIÓN FACTORY METHOD PATTERN ===");
        ProductoRequest arroz = new ProductoRequest(999, 100, new java.math.BigDecimal("2500"), "Arroz Premium", "granos");
        ProductoRequest aceite = new ProductoRequest(998, 50, new java.math.BigDecimal("3500"), "Aceite de Oliva", "aceites");
        ProductoRequest leche = new ProductoRequest(997, 30, new java.math.BigDecimal("4500"), "Leche Deslactosada", "lácteos");

        try {
            Producto p1 = crearProducto(arroz);
            System.out.println("Creado: " + p1);
            Producto p2 = crearProducto(aceite);
            System.out.println("Creado: " + p2);
            Producto p3 = crearProducto(leche);
            System.out.println("Creado: " + p3);
        } catch (Exception e) {
            System.out.println("Error en demostración: " + e.getMessage());
        }

        System.out.println("=== FIN DEMOSTRACIÓN ===");
    }

    public void demostrarObserverPattern() {
        notificationService.demostrarPatronObserver();
    }

    public List<Producto> obtenerProductosConStockBajo(Integer cantidadMinima) {
        return productoRepository.findAll().stream()
                .filter(producto -> producto.getCantidadDisponible() != null &&
                        producto.getCantidadDisponible() < cantidadMinima)
                .collect(java.util.stream.Collectors.toList());
    }

    public void demostrarAmbosPatrones() {
        System.out.println("=== DEMOSTRACIÓN PATRONES DE DISEÑO ===");
        System.out.println("1. Factory Method Pattern:");
        demostrarFactoryPattern();
        System.out.println("\n2. Observer Pattern:");
        demostrarObserverPattern();
        System.out.println("=== FIN DEMOSTRACIÓN COMPLETA ===");
    }
}