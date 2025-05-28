package com.nisum.userservice.service.validator.impl;

import com.nisum.userservice.dto.UserDTO;
import com.nisum.userservice.dto.PhoneDTO;
import com.nisum.userservice.model.ValidationConfig;
import com.nisum.userservice.repository.UserRepository;
import com.nisum.userservice.repository.ValidationConfigRepository;
import com.nisum.userservice.service.validator.UserValidator;
import com.nisum.userservice.exception.UserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.Optional;

@Component
public class UserValidatorImpl implements UserValidator {
    private static final Logger logger = LoggerFactory.getLogger(UserValidatorImpl.class);
    private final UserRepository userRepository;
    private final ValidationConfigRepository validationConfigRepository;
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";

    @Autowired
    public UserValidatorImpl(UserRepository userRepository, ValidationConfigRepository validationConfigRepository) {
        this.userRepository = userRepository;
        this.validationConfigRepository = validationConfigRepository;
    }

    @Override
    public void validateUser(UserDTO userDTO) {
        logger.info("Iniciando validación de usuario: {}", userDTO.getEmail());
        List<String> errores = new ArrayList<>();

        // Validar nombre
        if (userDTO.getName() == null || userDTO.getName().trim().isEmpty()) {
            logger.error("El nombre es nulo o vacío");
            errores.add("El nombre no puede estar vacío");
        } else {
            Optional<ValidationConfig> nameLengthConfig = validationConfigRepository.findByConfigKey("name.min.length");
            if (nameLengthConfig.isPresent()) {
                int minLength = Integer.parseInt(nameLengthConfig.get().getConfigValue());
                if (userDTO.getName().length() < minLength) {
                    logger.error("El nombre es demasiado corto: {}", userDTO.getName());
                    errores.add("El nombre debe tener al menos " + minLength + " caracteres");
                }
            }
        }

        // Validar email
        if (userDTO.getEmail() == null || !Pattern.matches(EMAIL_PATTERN, userDTO.getEmail())) {
            logger.error("Email inválido: {}", userDTO.getEmail());
            errores.add("El formato del correo electrónico no es válido");
        } else if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            logger.error("Email ya registrado: {}", userDTO.getEmail());
            errores.add("El correo ya está registrado");
        }

        // Validar contraseña
        if (userDTO.getPassword() == null) {
            logger.error("La contraseña es nula");
            errores.add("La contraseña no puede estar vacía");
        } else {
            String password = userDTO.getPassword();
            List<String> passwordErrors = new ArrayList<>();

            // Validar longitud mínima de contraseña
            Optional<ValidationConfig> passwordLengthConfig = validationConfigRepository.findByConfigKey("password.min.length");
            if (passwordLengthConfig.isPresent()) {
                int minLength = Integer.parseInt(passwordLengthConfig.get().getConfigValue());
                if (password.length() < minLength) {
                    logger.error("La contraseña es demasiado corta: {}", password);
                    passwordErrors.add("La contraseña debe tener al menos " + minLength + " caracteres");
                }
            }

            // Validar formato de contraseña
            Optional<ValidationConfig> passwordPatternConfig = validationConfigRepository.findByConfigKey("password.pattern");
            if (passwordPatternConfig.isPresent() && !Pattern.matches(passwordPatternConfig.get().getConfigValue(), password)) {
                logger.error("La contraseña no cumple con el patrón requerido");
                passwordErrors.add("La contraseña debe cumplir con el formato requerido");
            }

            // Validar que contenga al menos un número
            Optional<ValidationConfig> requireNumberConfig = validationConfigRepository.findByConfigKey("password.require.number");
            if (requireNumberConfig.isPresent() && !Pattern.matches(".*[0-9].*", password)) {
                logger.error("La contraseña no contiene números");
                passwordErrors.add("La contraseña debe contener al menos un número");
            }

            // Validar que contenga al menos una letra minúscula
            Optional<ValidationConfig> requireLowercaseConfig = validationConfigRepository.findByConfigKey("password.require.lowercase");
            if (requireLowercaseConfig.isPresent() && !Pattern.matches(".*[a-z].*", password)) {
                logger.error("La contraseña no contiene letras minúsculas");
                passwordErrors.add("La contraseña debe contener al menos una letra minúscula");
            }

            // Validar que contenga al menos una letra mayúscula
            Optional<ValidationConfig> requireUppercaseConfig = validationConfigRepository.findByConfigKey("password.require.uppercase");
            if (requireUppercaseConfig.isPresent() && !Pattern.matches(".*[A-Z].*", password)) {
                logger.error("La contraseña no contiene letras mayúsculas");
                passwordErrors.add("La contraseña debe contener al menos una letra mayúscula");
            }

            // Validar que contenga al menos un carácter especial
            Optional<ValidationConfig> requireSpecialConfig = validationConfigRepository.findByConfigKey("password.require.special");
            if (requireSpecialConfig.isPresent() && !Pattern.matches(".*[@#$%^&+=!?].*", password)) {
                logger.error("La contraseña no contiene caracteres especiales");
                passwordErrors.add("La contraseña debe contener al menos un carácter especial");
            }

            // Validar que no contenga espacios
            Optional<ValidationConfig> noSpacesConfig = validationConfigRepository.findByConfigKey("password.no.spaces");
            if (noSpacesConfig.isPresent() && password.contains(" ")) {
                logger.error("La contraseña contiene espacios");
                passwordErrors.add("La contraseña no debe contener espacios");
            }

            if (!passwordErrors.isEmpty()) {
                errores.addAll(passwordErrors);
            }
        }

        // Si hay errores, lanzar excepción con todos los mensajes
        if (!errores.isEmpty()) {
            String mensajeError = String.join(" | ", errores);
            logger.error("Errores de validación encontrados: {}", mensajeError);
            throw new UserException(mensajeError);
        }
        
        logger.info("Validación completada exitosamente para el usuario: {}", userDTO.getEmail());
    }

    @Override
    public List<ValidationConfig> getValidationConfig() {
        return validationConfigRepository.findAll();
    }
} 