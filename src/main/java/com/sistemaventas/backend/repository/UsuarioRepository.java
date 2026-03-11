package com.sistemaventas.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sistemaventas.backend.entity.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    
    // Buscar usuario por correo
    Optional<Usuario> findByCorreo(String correo);
    
    // Buscar usuarios por rol
    @Query("SELECT u FROM Usuario u WHERE u.rol.idRol = :idRol")
    List<Usuario> findByRolId(@Param("idRol") Integer idRol);
    
    // Buscar usuarios por nombre (búsqueda parcial)
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Usuario> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);
    
    // Verificar si existe un correo
    boolean existsByCorreo(String correo);
    
    // Buscar usuarios activos (que tienen facturas)
    @Query("SELECT DISTINCT u FROM Usuario u WHERE SIZE(u.facturas) > 0")
    List<Usuario> findUsuariosConFacturas();
    
    // Contar usuarios por rol
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol.idRol = :idRol")
    Long countByRolId(@Param("idRol") Integer idRol);
}