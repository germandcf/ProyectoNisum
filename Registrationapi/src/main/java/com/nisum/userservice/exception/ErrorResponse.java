package com.nisum.userservice.exception;

import java.util.ArrayList;
import java.util.List;

public class ErrorResponse {
    private String mensaje;

    public ErrorResponse(String mensaje) {
        this.mensaje = mensaje;
    }

    public ErrorResponse(List<String> mensajes) {
        this.mensaje = String.join(" | ", mensajes);
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
} 