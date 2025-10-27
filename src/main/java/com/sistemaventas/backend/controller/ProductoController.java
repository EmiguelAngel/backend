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
    public ResponseEntity<List<ProductoResponse>> obtenerTodosLosProductos() {
        try {
            List<Producto> productos = productoService.obtenerTodosLosProductos();
            List<ProductoResponse> productosResponse = productos.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
            return ResponseEntity.ok(productosResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtenerProductoPorId(@PathVariable Integer id) {
        try {
            Optional<Producto> producto = productoService.buscarPorId(id);
            if (producto.isPresent()) {
                ProductoResponse productoResponse = convertirAResponse(producto.get());
                return ResponseEntity.ok(productoResponse);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @PostMapping
    public ResponseEntity<?> crearProducto(@Valid @RequestBody ProductoRequest productoRequest) {
        try {
            System.out.println("Recibiendo request para crear producto: " + productoRequest);
            Producto nuevoProducto = productoService.crearProducto(productoRequest);
            ProductoResponse productoResponse = convertirAResponse(nuevoProducto);
            return ResponseEntity.status(HttpStatus.CREATED).body(productoResponse);
        } catch (IllegalArgumentException e) {
            System.err.println("Error de validación al crear producto: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Error inesperado al crear producto: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al procesar producto: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizarProducto(@PathVariable Integer id,
                                                               @Valid @RequestBody ProductoRequest productoRequest) {
        try {
            Producto productoActualizado = productoService.actualizarProducto(id, productoRequest);
            ProductoResponse productoResponse = convertirAResponse(productoActualizado);
            return ResponseEntity.ok(productoResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Integer id) {
        try {
            boolean eliminado = productoService.eliminarProducto(id);
            if (eliminado) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
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

