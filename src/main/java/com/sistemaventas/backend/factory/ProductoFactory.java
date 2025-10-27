package com.sistemaventas.backend.factory;

import com.sistemaventas.backend.dto.request.ProductoRequest;
import com.sistemaventas.backend.entity.Producto;

// Abstract Factory
public abstract class ProductoFactory {
    
    // Factory Method abstracto
    public abstract Producto crearProducto(ProductoRequest request);
    
    // Factory Method estático para obtener la fábrica correcta
    public static ProductoFactory obtenerFactory(String categoria) {
        if (categoria == null || categoria.trim().isEmpty()) {
            return new ProductoGeneralFactory();
        }
        
        return switch (categoria.toLowerCase().trim()) {
            case "granos" -> new GranosProductoFactory();
            case "aceites" -> new AceitesProductoFactory();
            case "lácteos", "lacteos" -> new LacteosProductoFactory();
            case "panadería", "panaderia" -> new PanaderiaProductoFactory();
            case "endulzantes" -> new EndulzantesProductoFactory();
            default -> new ProductoGeneralFactory();
        };
    }
    
    // Método común para validaciones básicas
    protected void validarProductoBase(ProductoRequest request) {
        System.out.println("ProductoFactory: Validando producto base");
        if (request == null) {
            throw new IllegalArgumentException("El request no puede ser null");
        }
        System.out.println("ProductoFactory: Validando descripción");
        if (request.getDescripcion() == null || request.getDescripcion().trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción del producto es obligatoria");
        }
        System.out.println("ProductoFactory: Validando precio unitario");
        if (request.getPrecioUnitario() == null || request.getPrecioUnitario().doubleValue() <= 0) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor a 0");
        }
        System.out.println("ProductoFactory: Validando cantidad disponible");
        if (request.getCantidadDisponible() == null || request.getCantidadDisponible() < 0) {
            throw new IllegalArgumentException("La cantidad disponible no puede ser negativa");
        }
        System.out.println("ProductoFactory: Validaciones básicas completadas exitosamente");
    }
    
    // Método común para crear producto base
    protected Producto crearProductoBase(ProductoRequest request) {
        validarProductoBase(request);
        
        Producto producto = new Producto();
        producto.setIdProducto(request.getIdProducto());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecioUnitario(request.getPrecioUnitario());
        producto.setCantidadDisponible(request.getCantidadDisponible());
        
        return producto;
    }
}

class GranosProductoFactory extends ProductoFactory {
    @Override
    public Producto crearProducto(ProductoRequest request) {
        Producto producto = crearProductoBase(request);
        producto.setCategoria(request.getCategoria());
        
        // Lógica específica para granos
        aplicarReglasGranos(producto);
        
        return producto;
    }
    
    private void aplicarReglasGranos(Producto producto) {
        // Validaciones específicas para granos
        if (producto.getDescripcion().toLowerCase().contains("arroz")) {
            // Aplicar reglas específicas para arroz
            if (producto.getPrecioUnitario().doubleValue() < 1000) {
                throw new IllegalArgumentException("El arroz debe tener un precio mínimo de $1000");
            }
        }
        
        // Si es un grano premium (precio alto), dar más tiempo de almacenamiento
        if (producto.getPrecioUnitario().doubleValue() > 3000) {
            System.out.println("Producto de grano premium detectado: " + producto.getDescripcion());
        }
    }
}

// Factory para productos de Aceites
class AceitesProductoFactory extends ProductoFactory {
    @Override
    public Producto crearProducto(ProductoRequest request) {
        Producto producto = crearProductoBase(request);
        producto.setCategoria("Aceites");
        
        // Lógica específica para aceites
        aplicarReglasAceites(producto);
        
        return producto;
    }
    
    private void aplicarReglasAceites(Producto producto) {
        // Validaciones específicas para aceites
        if (producto.getPrecioUnitario().doubleValue() < 2000) {
            throw new IllegalArgumentException("Los aceites deben tener un precio mínimo de $2000");
        }
        
        // Todos los aceites requieren condiciones especiales de almacenamiento
        System.out.println("NOTA: Aceite requiere almacenamiento en lugar fresco y seco: " + producto.getDescripcion());
    }
}

// Factory para productos Lácteos
class LacteosProductoFactory extends ProductoFactory {
    @Override
    public Producto crearProducto(ProductoRequest request) {
        Producto producto = crearProductoBase(request);
        producto.setCategoria("Lácteos");
        
        // Lógica específica para lácteos
        aplicarReglasLacteos(producto);
        
        return producto;
    }
    
    private void aplicarReglasLacteos(Producto producto) {
        // Los lácteos requieren refrigeración
        System.out.println("IMPORTANTE: Producto lácteo requiere refrigeración: " + producto.getDescripcion());
        
        // Validar que la cantidad no sea excesiva (productos perecederos)
        if (producto.getCantidadDisponible() > 100) {
            System.out.println("ADVERTENCIA: Gran cantidad de producto perecedero en inventario");
        }
    }
}

// Factory para productos de Panadería
class PanaderiaProductoFactory extends ProductoFactory {
    @Override
    public Producto crearProducto(ProductoRequest request) {
        Producto producto = crearProductoBase(request);
        producto.setCategoria("Panadería");
        
        // Lógica específica para panadería
        aplicarReglasPanaderia(producto);
        
        return producto;
    }
    
    private void aplicarReglasPanaderia(Producto producto) {
        // Los productos de panadería tienen vida útil corta
        System.out.println("NOTA: Producto de panadería - verificar fecha de vencimiento: " + producto.getDescripcion());
        
        // Limitar cantidad máxima por ser perecedero
        if (producto.getCantidadDisponible() > 50) {
            System.out.println("ADVERTENCIA: Cantidad alta para producto de panadería (vida útil corta)");
        }
    }
}

// Factory para Endulzantes
class EndulzantesProductoFactory extends ProductoFactory {
    @Override
    public Producto crearProducto(ProductoRequest request) {
        Producto producto = crearProductoBase(request);
        producto.setCategoria("Endulzantes");
        
        // Lógica específica para endulzantes
        aplicarReglasEndulzantes(producto);
        
        return producto;
    }
    
    private void aplicarReglasEndulzantes(Producto producto) {
        // Los endulzantes deben protegerse de la humedad
        System.out.println("NOTA: Endulzante - proteger de humedad: " + producto.getDescripcion());
        
        if (producto.getDescripcion().toLowerCase().contains("azúcar") || 
            producto.getDescripcion().toLowerCase().contains("azucar")) {
            // Reglas específicas para azúcar
            if (producto.getPrecioUnitario().doubleValue() < 800) {
                throw new IllegalArgumentException("El azúcar debe tener un precio mínimo de $800");
            }
        }
    }
}

// Factory General para categorías no específicas
class ProductoGeneralFactory extends ProductoFactory {
    @Override
    public Producto crearProducto(ProductoRequest request) {
        Producto producto = crearProductoBase(request);
        
        // Si no se especifica categoría, usar "General"
        if (request.getCategoria() == null || request.getCategoria().trim().isEmpty()) {
            producto.setCategoria("General");
        } else {
            producto.setCategoria(request.getCategoria());
        }
        
        System.out.println("Producto creado con factory general: " + producto.getDescripcion());
        
        return producto;
    }
}