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
        System.out.println("ProductoService: Iniciando creación de producto en transacción");
        
        // Validaciones básicas
        if (productoRequest == null) {
            throw new IllegalArgumentException("El request no puede ser null");
        }
        
        // Generar nuevo ID si no se proporciona
        if (productoRequest.getIdProducto() == null) {
            Integer nuevoId = generarNuevoId();
            System.out.println("ProductoService: Generando nuevo ID: " + nuevoId);
            productoRequest.setIdProducto(nuevoId);
        }

        // Verificar que no exista ya un producto con ese ID
        if (productoRepository.existsById(productoRequest.getIdProducto())) {
            throw new RuntimeException("Ya existe un producto con ID: " + productoRequest.getIdProducto());
        }

        // Usar Factory Method para crear el producto según la categoría
        try {
            System.out.println("ProductoService: Creando factory para categoría: " + productoRequest.getCategoria());
            ProductoFactory factory = ProductoFactory.obtenerFactory(productoRequest.getCategoria());
            
            System.out.println("ProductoService: Creando producto usando factory");
            Producto producto = factory.crearProducto(productoRequest);
            
            System.out.println("ProductoService: Guardando producto en base de datos");
            Producto productoGuardado = productoRepository.save(producto);
            System.out.println("ProductoService: Producto guardado exitosamente con ID: " + productoGuardado.getIdProducto());
            
            return productoGuardado;
        } catch (Exception e) {
            System.err.println("ProductoService: Error al crear producto: " + e.getMessage());
            e.printStackTrace();
            throw e;
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
        Optional<Producto> productoExistente = productoRepository.findById(id);

        if (productoExistente.isEmpty()) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }

        // Crear producto actualizado usando Factory
        productoRequest.setIdProducto(id);
        ProductoFactory factory = ProductoFactory.obtenerFactory(productoRequest.getCategoria());
        Producto productoActualizado = factory.crearProducto(productoRequest);

        return productoRepository.save(productoActualizado);
    }

    // Eliminar producto
    public boolean eliminarProducto(Integer id) {
        if (productoRepository.existsById(id)) {
            productoRepository.deleteById(id);
            return true;
        }
        return false;
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