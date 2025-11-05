package com.transportista.tarifas.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es requerido")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "El apellido es requerido")
    @Column(nullable = false)
    private String apellido;

    @NotBlank(message = "El DNI es requerido")
    @Column(unique = true, nullable = false, length = 20)
    private String dni;

    @NotBlank(message = "El domicilio es requerido")
    @Column(nullable = false)
    private String domicilio;

    @NotBlank(message = "El teléfono es requerido")
    @Column(nullable = false, length = 20)
    private String telefono;

    @Email(message = "Email inválido")
    @NotBlank(message = "El email es requerido")
    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "activo")
    private Boolean activo = true;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
        if (activo == null) {
            activo = true;
        }
    }
}
