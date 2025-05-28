package com.nisum.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO para teléfono")
public class PhoneDTO {
    @Schema(description = "ID del teléfono")
    private Long id;

    @Schema(description = "Número de teléfono")
    private String number;

    @Schema(description = "Código de ciudad", example = "9")
    private String cityCode;

    @Schema(description = "Código de país", example = "57")
    private String countryCode;

    // Constructor vacío
    public PhoneDTO() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

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

    // Getters y setters adicionales para manejar los nombres alternativos
    public void setCitycode(String citycode) {
        this.cityCode = citycode;
    }

    public void setContrycode(String contrycode) {
        this.countryCode = contrycode;
    }
} 