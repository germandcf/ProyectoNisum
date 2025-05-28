package com.nisum.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de error")
public class ErrorResponse {
    @Schema(description = "Mensaje de error", 
            example = "La contraseña no cumple con los requisitos",
            allowableValues = {
                "La contraseña no cumple con los requisitos",
                "Faltan campos requeridos en la solicitud",
                "Formato de correo incorrecto",
                "El correo ya está registrado",
                "El nombre no cumple con los requisitos",
                "El usuario no puede ser nulo",
                "Todos los campos son requeridos"
            })
    private final String mensaje;

    public ErrorResponse(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }
} 