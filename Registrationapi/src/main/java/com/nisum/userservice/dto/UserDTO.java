package com.nisum.userservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Date;

@Schema(description = "DTO para usuario")
public class UserDTO {
    @Schema(description = "ID del usuario")
    private String id;

    @Schema(description = "Nombre del usuario")
    private String name;

    @Schema(description = "Email del usuario")
    private String email;

    @Schema(description = "Contraseña del usuario")
    private String password;

    @Schema(description = "Lista de teléfonos del usuario")
    private List<PhoneDTO> phones;

    @Schema(description = "Fecha de creación", example = "2024-03-19")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date created;

    @Schema(description = "Fecha de modificación", example = "2024-03-19")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date modified;

    @Schema(description = "Fecha del último login", example = "2024-03-19")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date lastLogin;

    @Schema(description = "Token del usuario")
    private String token;

    @Schema(description = "Estado activo del usuario")
    private boolean active;

    // Getters y Setters
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

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
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