package com.nisum.userservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO para teléfonos")
public class PhoneDTO {
    @JsonIgnore
    private Long id;

    @Schema(description = "Número de teléfono", example = "12345678")
    private String number;

    @Schema(description = "Código de ciudad", example = "2")
    @JsonProperty("cityCode")
    private String cityCode;

    @Schema(description = "Código de país", example = "57")
    @JsonProperty("countryCode")
    private String countryCode;

    // Constructor vacío
    public PhoneDTO() {
    }

    // Getters y setters específicos para el ID
    @JsonIgnore
    public Long getId() {
        return id;
    }

    @JsonIgnore
    public void setId(Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
} 