package com.sistemaventas.backend.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sistemaventas.backend.dto.request.ProductoRequest;
import com.sistemaventas.backend.dto.response.ApiResponse;
import com.sistemaventas.backend.dto.response.ProductoResponse;
import com.sistemaventas.backend.entity.Producto;
import com.sistemaventas.backend.service.ProductoService;

import jakarta.validation.Valid;

/**
 * Controller (C in MVC): expone endpoints REST para operaciones sobre productos.
 * Actúa como la "vista/controlador" de la capa web — recibe HTTP, llama al servicio
 * y devuelve DTOs al cliente (frontend).
 */
@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductoController {
    
    @Autowired
    private ProductoService productoService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductoResponse>>> obtenerTodosLosProductos() {
        try {
            List<Producto> productos = productoService.obtenerTodosLosProductos();
            List<ProductoResponse> productosResponse = productos.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, "Productos obtenidos exitosamente", productosResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoResponse>> obtenerProductoPorId(@PathVariable Integer id) {
        try {
            Optional<Producto> producto = productoService.buscarPorId(id);
            if (producto.isPresent()) {
                ProductoResponse productoResponse = convertirAResponse(producto.get());
                return ResponseEntity.ok(new ApiResponse<>(true, "Producto encontrado", productoResponse));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Producto no encontrado", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @PostMapping
    public ResponseEntity<?> crearProducto(@Valid @RequestBody ProductoRequest productoRequest) {
        try {
            System.out.println("=== INICIO CREAR PRODUCTO ===");
            System.out.println("Request recibido: " + productoRequest);
            
            // Validar estructura básica del request
            if (productoRequest == null) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "El request no puede ser null", null));
            }
            
            // Intentar crear el producto
            Producto nuevoProducto = productoService.crearProducto(productoRequest);
            
            if (nuevoProducto == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error: no se pudo crear el producto", null));
            }
            
            System.out.println("Producto creado en servicio: " + nuevoProducto);
            
            // Convertir a DTO de respuesta
            ProductoResponse productoResponse = convertirAResponse(nuevoProducto);
            System.out.println("Response generado: " + productoResponse);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Producto creado exitosamente", productoResponse));
                
        } catch (IllegalArgumentException e) {
            String mensaje = "Error de validación: " + e.getMessage();
            System.err.println("ProductoController: " + mensaje);
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, mensaje, null));
                
        } catch (RuntimeException e) {
            String mensaje = "Error al procesar producto: " + e.getMessage();
            System.err.println("ProductoController: " + mensaje);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, mensaje, null));
                
        } catch (Exception e) {
            String mensaje = "Error inesperado al crear producto: " + e.getMessage();
            System.err.println("ProductoController: " + mensaje);
            System.err.println("ProductoController: Tipo de error: " + e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, mensaje, null));
        } finally {
            System.out.println("=== FIN CREAR PRODUCTO ===");
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizarProducto(@PathVariable Integer id,
                                                               @Valid @RequestBody ProductoRequest productoRequest) {
        try {
            // Validar que el ID sea válido
            if (id == null || id <= 0) {
                System.err.println("❌ ID inválido recibido: " + id);
                return ResponseEntity.badRequest().body(null);
            }
            
            System.out.println("📝 Solicitud de actualización para producto ID: " + id);
            Producto productoActualizado = productoService.actualizarProducto(id, productoRequest);
            ProductoResponse productoResponse = convertirAResponse(productoActualizado);
            return ResponseEntity.ok(productoResponse);
        } catch (RuntimeException e) {
            System.err.println("❌ Error en actualización: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            System.err.println("❌ Error interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Integer id) {
        try {
            // Validar que el ID sea válido
            if (id == null || id <= 0) {
                System.err.println("❌ ID inválido recibido para eliminar: " + id);
                return ResponseEntity.badRequest().build();
            }
            
            System.out.println("🗑️ Solicitud de eliminación para producto ID: " + id);
            boolean eliminado = productoService.eliminarProducto(id);
            if (eliminado) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("❌ Error eliminando: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<ProductoResponse>> obtenerProductosPorCategoria(@PathVariable String categoria) {
        try {
            List<Producto> productos = productoService.buscarPorCategoria(categoria);
            List<ProductoResponse> productosResponse = productos.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
            return ResponseEntity.ok(productosResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<ProductoResponse>> buscarProductos(@RequestParam String q) {
        try {
            List<Producto> productos = productoService.buscarProductosPorTermino(q);
            List<ProductoResponse> productosResponse = productos.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
            return ResponseEntity.ok(productosResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @GetMapping("/stock-bajo")
    public ResponseEntity<List<ProductoResponse>> obtenerProductosConStockBajo(@RequestParam(defaultValue = "5") int limite) {
        try {
            List<Producto> productos = productoService.buscarProductosConStockBajo(limite);
            List<ProductoResponse> productosResponse = productos.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
            return ResponseEntity.ok(productosResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    private ProductoResponse convertirAResponse(Producto producto) {
        return new ProductoResponse(
            producto.getIdProducto(),
            producto.getDescripcion(),
            producto.getPrecioUnitario(),
            producto.getCantidadDisponible(),
            producto.getCategoria()
        );
    }
}

