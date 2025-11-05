package com.transportista.tarifas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {
    
    private Long id;
    
    @NotBlank(message = "El nombre es requerido")
    private String nombre;
    
    @NotBlank(message = "El apellido es requerido")
    private String apellido;
    
    @NotBlank(message = "El DNI es requerido")
    private String dni;
    
    @NotBlank(message = "El domicilio es requerido")
    private String domicilio;
    
    @NotBlank(message = "El teléfono es requerido")
    private String telefono;
    
    @Email(message = "Email inválido")
    @NotBlank(message = "El email es requerido")
    private String email;
    
    private Boolean activo;
}
