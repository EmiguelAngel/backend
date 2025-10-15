package com.sistemaventas.backend.service;

import com.sistemaventas.backend.entity.Usuario;
import com.sistemaventas.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    // Obtener todos los usuarios
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }
    
    // Buscar usuario por ID
    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepository.findById(id);
    }
    
    // Buscar usuario por correo
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }
    
    // Crear nuevo usuario
    public Usuario crearUsuario(Usuario usuario) {
        // Verificar que el correo no exista
        if (usuarioRepository.existsByCorreo(usuario.getCorreo())) {
            throw new RuntimeException("Ya existe un usuario con ese correo electrónico");
        }
        
        // Generar nuevo ID (en un caso real, mejor usar @GeneratedValue)
        Integer nuevoId = generarNuevoId();
        usuario.setIdUsuario(nuevoId);
        
        return usuarioRepository.save(usuario);
    }
    
    // Actualizar usuario
    public Usuario actualizarUsuario(Integer id, Usuario usuarioActualizado) {
        Optional<Usuario> usuarioExistente = usuarioRepository.findById(id);
        
        if (usuarioExistente.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
        
        Usuario usuario = usuarioExistente.get();
        
        // Verificar si el nuevo correo ya existe en otro usuario
        if (!usuario.getCorreo().equals(usuarioActualizado.getCorreo()) && 
            usuarioRepository.existsByCorreo(usuarioActualizado.getCorreo())) {
            throw new RuntimeException("Ya existe un usuario con ese correo electrónico");
        }
        
        // Actualizar campos
        usuario.setNombre(usuarioActualizado.getNombre());
        usuario.setCorreo(usuarioActualizado.getCorreo());
        usuario.setTelefono(usuarioActualizado.getTelefono());
        usuario.setRol(usuarioActualizado.getRol());
        
        // Solo actualizar contraseña si se proporciona una nueva
        if (usuarioActualizado.getContrasena() != null && !usuarioActualizado.getContrasena().trim().isEmpty()) {
            usuario.setContrasena(usuarioActualizado.getContrasena());
        }
        
        return usuarioRepository.save(usuario);
    }
    
    // Eliminar usuario
    public boolean eliminarUsuario(Integer id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    // Buscar usuarios por rol
    public List<Usuario> buscarPorRol(Integer idRol) {
        return usuarioRepository.findByRolId(idRol);
    }
    
    // Buscar usuarios por nombre (búsqueda parcial)
    public List<Usuario> buscarPorNombre(String nombre) {
        return usuarioRepository.findByNombreContainingIgnoreCase(nombre);
    }
    
    // Verificar credenciales (para login básico)
    public Optional<Usuario> verificarCredenciales(String correo, String contrasena) {
        Optional<Usuario> usuario = usuarioRepository.findByCorreo(correo);
        
        if (usuario.isPresent() && usuario.get().getContrasena().equals(contrasena)) {
            return usuario;
        }
        
        return Optional.empty();
    }
    
    // Método auxiliar para generar ID
    private Integer generarNuevoId() {
        // En un caso real, es mejor usar @GeneratedValue en la entidad
        // Este es solo un ejemplo simple
        List<Usuario> usuarios = usuarioRepository.findAll();
        if (usuarios.isEmpty()) {
            return 1;
        }
        return usuarios.stream()
                .mapToInt(Usuario::getIdUsuario)
                .max()
                .orElse(0) + 1;
    }
}