package com.nisum.userservice.service.validator.impl;

import com.nisum.userservice.dto.UserDTO;
import com.nisum.userservice.dto.PhoneDTO;
import com.nisum.userservice.model.ValidationConfig;
import com.nisum.userservice.repository.UserRepository;
import com.nisum.userservice.repository.ValidationConfigRepository;
import com.nisum.userservice.service.validator.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
public class UserValidatorImpl implements UserValidator {

    private final UserRepository userRepository;
    private final ValidationConfigRepository validationConfigRepository;

    @Autowired
    public UserValidatorImpl(UserRepository userRepository, ValidationConfigRepository validationConfigRepository) {
        this.userRepository = userRepository;
        this.validationConfigRepository = validationConfigRepository;
    }

    @Override
    public void validateUser(UserDTO userDTO) {
        if (userDTO == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario no puede ser nulo");
        }

        List<String> errores = new ArrayList<>();

        // Validar campos requeridos
        if (userDTO.getName() == null || userDTO.getName().trim().isEmpty() ||
            userDTO.getEmail() == null || userDTO.getEmail().trim().isEmpty() ||
            userDTO.getPassword() == null || userDTO.getPassword().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Todos los campos son requeridos");
        }

        // Validar email
        Optional<ValidationConfig> emailRegexConfig = validationConfigRepository.findByConfigKey("email.regex");
        if (emailRegexConfig.isPresent()) {
            String regex = emailRegexConfig.get().getConfigValue();
            
            if (!Pattern.matches(regex, userDTO.getEmail())) {
                errores.add("El email no cumple con el formato requerido");
            } else if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
                errores.add("El email ya está registrado");
            }
        }

        // Validar contraseña
        Optional<ValidationConfig> passwordMinLengthConfig = validationConfigRepository.findByConfigKey("password.min.length");
        if (passwordMinLengthConfig.isPresent()) {
            int minLength = Integer.parseInt(passwordMinLengthConfig.get().getConfigValue());
            
            if (userDTO.getPassword().length() < minLength) {
                errores.add("La contraseña debe tener al menos " + minLength + " caracteres");
            }
        }

        Optional<ValidationConfig> passwordPatternConfig = validationConfigRepository.findByConfigKey("password.pattern");
        if (passwordPatternConfig.isPresent()) {
            String pattern = passwordPatternConfig.get().getConfigValue();
            
            if (!Pattern.matches(pattern, userDTO.getPassword())) {
                errores.add("La contraseña no cumple con los requisitos");
            }
        }

        // Validar nombre
        Optional<ValidationConfig> nameMinLengthConfig = validationConfigRepository.findByConfigKey("name.min.length");
        if (nameMinLengthConfig.isPresent()) {
            int minLength = Integer.parseInt(nameMinLengthConfig.get().getConfigValue());
            if (userDTO.getName().length() < minLength) {
                errores.add("El nombre debe tener al menos " + minLength + " caracteres");
            }
        }

        // Validar teléfonos solo si hay al menos uno
        if (userDTO.getPhones() != null && !userDTO.getPhones().isEmpty()) {
            for (PhoneDTO phone : userDTO.getPhones()) {
                if (phone.getNumber() == null || phone.getNumber().trim().isEmpty()) {
                    errores.add("El número de teléfono es requerido");
                }
                if (phone.getCityCode() == null || phone.getCityCode().trim().isEmpty()) {
                    errores.add("El código de ciudad es requerido");
                }
                if (phone.getCountryCode() == null || phone.getCountryCode().trim().isEmpty()) {
                    errores.add("El código de país es requerido");
                }
            }
        }

        if (!errores.isEmpty()) {
            String mensaje = String.join(", ", errores);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
        }
    }
} 