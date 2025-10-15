package com.sistemaventas.backend.repository;

import com.sistemaventas.backend.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    
    // Buscar productos por categoría
    List<Producto> findByCategoria(String categoria);
    
    // Buscar productos por descripción (búsqueda parcial, ignorando mayúsculas)
    List<Producto> findByDescripcionContainingIgnoreCase(String descripcion);
    
    // Buscar productos por descripción o categoría (búsqueda general)
    List<Producto> findByDescripcionContainingIgnoreCaseOrCategoriaContainingIgnoreCase(String descripcion, String categoria);
    
    // Buscar productos con stock disponible (cantidad > 0)
    @Query("SELECT p FROM Producto p WHERE p.cantidadDisponible > 0")
    List<Producto> findProductosConStock();
    
    // Buscar productos con stock bajo (menor a la cantidad especificada)
    @Query("SELECT p FROM Producto p WHERE p.cantidadDisponible <= :cantidadMinima")
    List<Producto> findProductosConStockBajo(@Param("cantidadMinima") Integer cantidadMinima);
    
    // Buscar productos agotados
    @Query("SELECT p FROM Producto p WHERE p.cantidadDisponible = 0 OR p.cantidadDisponible IS NULL")
    List<Producto> findProductosAgotados();
    
    // Buscar productos por rango de precio
    @Query("SELECT p FROM Producto p WHERE p.precioUnitario BETWEEN :precioMin AND :precioMax")
    List<Producto> findByPrecioUnitarioBetween(@Param("precioMin") BigDecimal precioMin, 
                                              @Param("precioMax") BigDecimal precioMax);
    
    // Buscar productos más caros que un precio específico
    List<Producto> findByPrecioUnitarioGreaterThan(BigDecimal precio);
    
    // Buscar productos más baratos que un precio específico
    List<Producto> findByPrecioUnitarioLessThan(BigDecimal precio);
    
    // Obtener todas las categorías únicas
    @Query("SELECT DISTINCT p.categoria FROM Producto p ORDER BY p.categoria")
    List<String> findDistinctCategorias();
    
    // Contar productos por categoría
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.categoria = :categoria")
    Long countByCategoria(@Param("categoria") String categoria);
    
    // Buscar productos más vendidos (los que tienen más registros en DetalleFactura)
    @Query("SELECT p FROM Producto p WHERE SIZE(p.detallesFactura) > 0 ORDER BY SIZE(p.detallesFactura) DESC")
    List<Producto> findProductosMasVendidos();
    
    // Buscar productos que nunca se han vendido
    @Query("SELECT p FROM Producto p WHERE SIZE(p.detallesFactura) = 0")
    List<Producto> findProductosNuncaVendidos();
    
    // Buscar productos por categoría y con stock disponible
    @Query("SELECT p FROM Producto p WHERE p.categoria = :categoria AND p.cantidadDisponible > 0")
    List<Producto> findByCategoriaConStock(@Param("categoria") String categoria);
    
    // Buscar productos que necesitan restock (cantidad <= cantidad mínima)
    @Query("SELECT p FROM Producto p WHERE p.cantidadDisponible <= :stockMinimo ORDER BY p.cantidadDisponible ASC")
    List<Producto> findProductosQueNecesitanRestock(@Param("stockMinimo") Integer stockMinimo);
    
    // Valor total del inventario
    @Query("SELECT SUM(p.precioUnitario * p.cantidadDisponible) FROM Producto p")
    BigDecimal calcularValorTotalInventario();
    
    // Valor del inventario por categoría
    @Query("SELECT SUM(p.precioUnitario * p.cantidadDisponible) FROM Producto p WHERE p.categoria = :categoria")
    BigDecimal calcularValorInventarioPorCategoria(@Param("categoria") String categoria);
    
    // Productos ordenados por precio (ascendente)
    List<Producto> findAllByOrderByPrecioUnitarioAsc();
    
    // Productos ordenados por precio (descendente)
    List<Producto> findAllByOrderByPrecioUnitarioDesc();
    
    // Productos ordenados por stock (ascendente) - útil para ver qué necesita restock
    List<Producto> findAllByOrderByCantidadDisponibleAsc();
    
    // Buscar productos por múltiples criterios
    @Query("SELECT p FROM Producto p WHERE " +
           "(:categoria IS NULL OR p.categoria = :categoria) AND " +
           "(:descripcion IS NULL OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :descripcion, '%'))) AND " +
           "(:precioMin IS NULL OR p.precioUnitario >= :precioMin) AND " +
           "(:precioMax IS NULL OR p.precioUnitario <= :precioMax) AND " +
           "(:soloConStock IS NULL OR p.cantidadDisponible > 0)")
    List<Producto> buscarProductosConFiltros(@Param("categoria") String categoria,
                                           @Param("descripcion") String descripcion,
                                           @Param("precioMin") BigDecimal precioMin,
                                           @Param("precioMax") BigDecimal precioMax,
                                           @Param("soloConStock") Boolean soloConStock);
}