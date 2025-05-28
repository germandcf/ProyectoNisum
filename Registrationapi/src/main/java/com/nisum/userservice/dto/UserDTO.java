package com.nisum.userservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "DTO para usuarios")
public class UserDTO {
    private String id;

    @Schema(description = "Nombre del usuario", example = "Juan Pérez")
    private String name;

    @Schema(description = "Correo electrónico del usuario", example = "juan@example.com")
    private String email;

    @Schema(description = "Contraseña del usuario", example = "Password123")
    private String password;

    @Schema(description = "Lista de teléfonos del usuario")
    private List<PhoneDTO> phones;

    @Schema(description = "Fecha de creación del usuario", example = "2024-05-28 14:30:00")
    private String created;

    @Schema(description = "Fecha de última modificación del usuario", example = "2024-05-28 14:30:00")
    private String modified;

    @Schema(description = "Fecha del último login del usuario", example = "2024-05-28 14:30:00")
    private String lastLogin;

    @Schema(description = "Token de autenticación del usuario")
    private String token;

    @Schema(description = "Estado de activación del usuario")
    private boolean active;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<PhoneDTO> getPhones() {
        return phones;
    }

    public void setPhones(List<PhoneDTO> phones) {
        this.phones = phones;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
} 