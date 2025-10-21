package com.sistemaventas.backend.controller;

import java.util.List;
import java.util.Optional;

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

import com.sistemaventas.backend.entity.Usuario;
import com.sistemaventas.backend.service.UsuarioService;

import jakarta.validation.Valid;

/**
 * Controller (C in MVC): Endpoints para gestión de usuarios. Recibe solicitudes HTTP
 * relacionadas con usuarios, delega a la capa de servicio y retorna entidades/DTOs.
 */
@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:4200") // Para Angular
public class UsuarioController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    // GET /api/usuarios - Obtener todos los usuarios
    @GetMapping
    public ResponseEntity<List<Usuario>> obtenerTodosLosUsuarios() {
        try {
            List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    // GET /api/usuarios/{id} - Obtener usuario por ID
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable Integer id) {
        try {
            Optional<Usuario> usuario = usuarioService.buscarPorId(id);
            if (usuario.isPresent()) {
                return ResponseEntity.ok(usuario.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    // GET /api/usuarios/correo/{correo} - Buscar por correo
    @GetMapping("/correo/{correo}")
    public ResponseEntity<Usuario> obtenerUsuarioPorCorreo(@PathVariable String correo) {
        try {
            Optional<Usuario> usuario = usuarioService.buscarPorCorreo(correo);
            if (usuario.isPresent()) {
                return ResponseEntity.ok(usuario.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    // POST /api/usuarios - Crear nuevo usuario
    @PostMapping
    public ResponseEntity<?> crearUsuario(@Valid @RequestBody Usuario usuario) {
        try {
            Usuario nuevoUsuario = usuarioService.crearUsuario(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }
    
    // PUT /api/usuarios/{id} - Actualizar usuario
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Integer id, 
                                               @Valid @RequestBody Usuario usuario) {
        try {
            Usuario usuarioActualizado = usuarioService.actualizarUsuario(id, usuario);
            return ResponseEntity.ok(usuarioActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }
    
    // DELETE /api/usuarios/{id} - Eliminar usuario
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Integer id) {
        try {
            boolean eliminado = usuarioService.eliminarUsuario(id);
            if (eliminado) {
                return ResponseEntity.ok().body("Usuario eliminado correctamente");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }
    
    // GET /api/usuarios/rol/{idRol} - Buscar por rol
    @GetMapping("/rol/{idRol}")
    public ResponseEntity<List<Usuario>> obtenerUsuariosPorRol(@PathVariable Integer idRol) {
        try {
            List<Usuario> usuarios = usuarioService.buscarPorRol(idRol);
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    // GET /api/usuarios/buscar?nombre=texto - Buscar por nombre
    @GetMapping("/buscar")
    public ResponseEntity<List<Usuario>> buscarPorNombre(@RequestParam String nombre) {
        try {
            List<Usuario> usuarios = usuarioService.buscarPorNombre(nombre);
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    // POST /api/usuarios/login - Login básico
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Optional<Usuario> usuario = usuarioService.verificarCredenciales(
                    loginRequest.getCorreo(), 
                    loginRequest.getContrasena()
            );
            
            if (usuario.isPresent()) {
                // Crear respuesta JSON estructurada
                LoginResponse response = new LoginResponse();
                response.setSuccess(true);
                response.setMessage("Login exitoso");
                response.setUser(usuario.get());
                response.setToken("jwt_token_placeholder"); // En producción, generar JWT real
                
                return ResponseEntity.ok().body(response);
            } else {
                ErrorResponse error = new ErrorResponse();
                error.setSuccess(false);
                error.setMessage("Credenciales incorrectas");
                
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse();
            error.setSuccess(false);
            error.setMessage("Error interno del servidor");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Clase interna para el login request
    public static class LoginRequest {
        private String correo;
        private String contrasena;
        
        // Constructores
        public LoginRequest() {}
        
        public LoginRequest(String correo, String contrasena) {
            this.correo = correo;
            this.contrasena = contrasena;
        }
        
        // Getters y Setters
        public String getCorreo() { return correo; }
        public void setCorreo(String correo) { this.correo = correo; }
        
        public String getContrasena() { return contrasena; }
        public void setContrasena(String contrasena) { this.contrasena = contrasena; }
    }
    
    // Clase para respuesta exitosa de login
    public static class LoginResponse {
        private boolean success;
        private String message;
        private Usuario user;
        private String token;
        
        // Constructores
        public LoginResponse() {}
        
        // Getters y Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public Usuario getUser() { return user; }
        public void setUser(Usuario user) { this.user = user; }
        
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
    
    // Clase para respuesta de error
    public static class ErrorResponse {
        private boolean success;
        private String message;
        
        // Constructores
        public ErrorResponse() {}
        
        // Getters y Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}