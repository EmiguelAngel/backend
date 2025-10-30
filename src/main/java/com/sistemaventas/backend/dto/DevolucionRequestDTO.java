package com.sistemaventas.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevolucionRequestDTO {
    
    @NotNull(message = "El ID de la factura es obligatorio")
    private Integer idFactura;
    
    @NotNull(message = "El motivo de la devolución es obligatorio")
    @Size(min = 10, max = 500, message = "El motivo debe tener entre 10 y 500 caracteres")
    private String motivo;
    
    @NotNull(message = "El usuario que procesa la devolución es obligatorio")
    @Size(max = 100, message = "El nombre de usuario no puede exceder 100 caracteres")
    private String usuarioDevolucion;
}
