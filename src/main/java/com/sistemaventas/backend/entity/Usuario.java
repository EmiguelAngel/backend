package com.sistemaventas.backend.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "USUARIO")
public class Usuario {
    
    @Id
    @Column(name = "IDUSUARIO")
    private Integer idUsuario;
    
    // Removed unused 'Rol Rol' field
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 256, message = "El nombre no puede exceder 256 caracteres")
    @Column(name = "NOMBRE", length = 256)
    private String nombre;
    
    @Email(message = "El formato del correo no es válido")
    @NotBlank(message = "El correo es obligatorio")
    @Size(max = 256, message = "El correo no puede exceder 256 caracteres")
    @Column(name = "CORREO", length = 256, unique = true)
    private String correo;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 256, message = "La contraseña debe tener entre 6 y 256 caracteres")
    @Column(name = "CONTRASENA", length = 256)
    private String contrasena;
    
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    @Column(name = "TELEFONO", length = 20)
    private String telefono;
    
     @ManyToOne(fetch = FetchType.EAGER) // Cambiar de LAZY a EAGER
     @JoinColumn(name = "IDROL", referencedColumnName = "IDROL")
     private Rol rol;

@OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
@JsonIgnore
private List<Factura> facturas = new ArrayList<>();
    
    // Constructores
    public Usuario() {}
    
    public Usuario(Integer idUsuario, String nombre, String correo, String contrasena, String telefono) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasena;
        this.telefono = telefono;
    }
    
    // Getters y Setters
    public Integer getIdUsuario() {
        return idUsuario;
    }
    
    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }
    
    public Rol getRol() {
        return rol;
    }
    
    public void setRol(Rol rol) {
        this.rol = rol;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getCorreo() {
        return correo;
    }
    
    public void setCorreo(String correo) {
        this.correo = correo;
    }
    
    public String getContrasena() {
        return contrasena;
    }
    
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    public List<Factura> getFacturas() {
        return facturas;
    }
    
    public void setFacturas(List<Factura> facturas) {
        this.facturas = facturas;
    }
    
    // Método toString para debug
    @Override
    public String toString() {
        return "Usuario{" +
                "idUsuario=" + idUsuario +
                ", nombre='" + nombre + '\'' +
                ", correo='" + correo + '\'' +
                ", telefono='" + telefono + '\'' +
                '}';
    }
}