package com.nisum.userservice.service.validator;

import com.nisum.userservice.dto.UserDTO;
import com.nisum.userservice.model.User;
import com.nisum.userservice.model.ValidationConfig;
import com.nisum.userservice.repository.UserRepository;
import com.nisum.userservice.repository.ValidationConfigRepository;
import com.nisum.userservice.service.validator.impl.UserValidatorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ValidationConfigRepository validationConfigRepository;

    @InjectMocks
    private UserValidatorImpl userValidator;

    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        userDTO = new UserDTO();
        userDTO.setName("Test User");
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("Password123");

        // Configurar los mocks lenient
        lenient().when(validationConfigRepository.findByConfigKey("name.min.length"))
            .thenReturn(Optional.of(createConfig("name.min.length", "3", "Minimum length for names")));
        lenient().when(validationConfigRepository.findByConfigKey("email.regex"))
            .thenReturn(Optional.of(createConfig("email.regex", "^[A-Za-z0-9+_.-]+@(.+)$", "Email format validation")));
        lenient().when(validationConfigRepository.findByConfigKey("password.min.length"))
            .thenReturn(Optional.of(createConfig("password.min.length", "8", "Minimum length for passwords")));
        lenient().when(userRepository.findByEmail(anyString()))
            .thenReturn(Optional.empty());
    }

    private ValidationConfig createConfig(String key, String value, String description) {
        ValidationConfig config = new ValidationConfig();
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setDescription(description);
        return config;
    }

    @Test
    void validateUser_NullName() {
        userDTO.setName(null);
        assertThrows(ResponseStatusException.class, () -> userValidator.validateUser(userDTO));
    }

    @Test
    void validateUser_EmptyName() {
        userDTO.setName("");
        assertThrows(ResponseStatusException.class, () -> userValidator.validateUser(userDTO));
    }

    @Test
    void validateUser_ShortName() {
        userDTO.setName("Jo");
        assertThrows(ResponseStatusException.class, () -> userValidator.validateUser(userDTO));
    }

    @Test
    void validateUser_NullEmail() {
        userDTO.setEmail(null);
        assertThrows(ResponseStatusException.class, () -> userValidator.validateUser(userDTO));
    }

    @Test
    void validateUser_InvalidEmail() {
        userDTO.setEmail("invalid-email");
        assertThrows(ResponseStatusException.class, () -> userValidator.validateUser(userDTO));
    }

    @Test
    void validateUser_DuplicateEmail() {
        User existingUser = new User();
        existingUser.setEmail(userDTO.getEmail());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(existingUser));
        assertThrows(ResponseStatusException.class, () -> userValidator.validateUser(userDTO));
    }

    @Test
    void validateUser_NullPassword() {
        userDTO.setPassword(null);
        assertThrows(ResponseStatusException.class, () -> userValidator.validateUser(userDTO));
    }

    @Test
    void validateUser_ShortPassword() {
        userDTO.setPassword("123");
        assertThrows(ResponseStatusException.class, () -> userValidator.validateUser(userDTO));
    }
} 