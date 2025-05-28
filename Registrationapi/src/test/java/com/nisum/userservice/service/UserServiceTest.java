package com.nisum.userservice.service;

import com.nisum.userservice.dto.UserDTO;
import com.nisum.userservice.dto.PhoneDTO;
import com.nisum.userservice.model.User;
import com.nisum.userservice.model.Phone;
import com.nisum.userservice.model.ValidationConfig;
import com.nisum.userservice.repository.UserRepository;
import com.nisum.userservice.repository.ValidationConfigRepository;
import com.nisum.userservice.service.impl.UserServiceImpl;
import com.nisum.userservice.service.validator.UserValidator;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ValidationConfigRepository validationConfigRepository;

    @Mock
    private UserValidator userValidator;

    private UserServiceImpl userService;
    private DateTimeFormatter dateFormatter;
    private UserDTO userDTO;
    private User user;

    @BeforeEach
    void setUp() {
        // Create service instance manually
        userService = new UserServiceImpl(
            userRepository,
            userValidator,
            validationConfigRepository,
            "testSecretKey123456789012345678901234567890",
            3600000L
        );

        dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        // Configurar UserDTO
        userDTO = new UserDTO();
        userDTO.setName("Test User");
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("Password123");
        userDTO.setCreated(LocalDateTime.now().format(dateFormatter));
        userDTO.setModified(LocalDateTime.now().format(dateFormatter));
        userDTO.setLastLogin(LocalDateTime.now().format(dateFormatter));
        userDTO.setToken("test-token");
        userDTO.setActive(true);

        List<PhoneDTO> phones = new ArrayList<>();
        PhoneDTO phone = new PhoneDTO();
        phone.setNumber("123456789");
        phone.setCityCode("1");
        phone.setCountryCode("57");
        phones.add(phone);
        userDTO.setPhones(phones);

        // Configurar User
        user = new User();
        user.setId("test-id-123");
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("Password123");
        user.setCreated(LocalDateTime.now());
        user.setModified(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        user.setToken("test-token");
        user.setActive(true);

        List<Phone> userPhones = new ArrayList<>();
        Phone userPhone = new Phone();
        userPhone.setNumber("123456789");
        userPhone.setCityCode("1");
        userPhone.setCountryCode("57");
        userPhone.setUser(user);
        userPhones.add(userPhone);
        user.setPhones(userPhones);
    }

    private void setupValidationConfigs() {
        // Configurar las validaciones necesarias
        ValidationConfig emailRegexConfig = new ValidationConfig();
        emailRegexConfig.setConfigKey("email.regex");
        emailRegexConfig.setConfigValue("^[A-Za-z0-9+_.-]+@(.+)$");
        when(validationConfigRepository.findByConfigKey("email.regex"))
            .thenReturn(Optional.of(emailRegexConfig));

        ValidationConfig passwordMinLengthConfig = new ValidationConfig();
        passwordMinLengthConfig.setConfigKey("password.min.length");
        passwordMinLengthConfig.setConfigValue("8");
        when(validationConfigRepository.findByConfigKey("password.min.length"))
            .thenReturn(Optional.of(passwordMinLengthConfig));

        ValidationConfig passwordPatternConfig = new ValidationConfig();
        passwordPatternConfig.setConfigKey("password.pattern");
        passwordPatternConfig.setConfigValue("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!?])(?=\\S+$).{8,}$");
        passwordPatternConfig.setDescription("La contraseña debe contener al menos un número, una letra minúscula, una mayúscula y un carácter especial");
        when(validationConfigRepository.findByConfigKey("password.pattern"))
            .thenReturn(Optional.of(passwordPatternConfig));

        ValidationConfig nameMinLengthConfig = new ValidationConfig();
        nameMinLengthConfig.setConfigKey("name.min.length");
        nameMinLengthConfig.setConfigValue("3");
        when(validationConfigRepository.findByConfigKey("name.min.length"))
            .thenReturn(Optional.of(nameMinLengthConfig));
    }

    private void setupEmailValidation() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    }

    @Test
    void createUser_Success() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDTO result = userService.createUser(userDTO);

        assertNotNull(result);
        assertEquals(userDTO.getName(), result.getName());
        assertEquals(userDTO.getEmail(), result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getAllUsers_Success() {
        // Arrange
        List<User> users = Arrays.asList(user);
        when(userRepository.findAll()).thenReturn(users);
        
        // Act
        List<UserDTO> result = userService.getAllUsers();
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        UserDTO resultUser = result.get(0);
        assertEquals(user.getId(), resultUser.getId());
        assertEquals(user.getName(), resultUser.getName());
        assertEquals(user.getEmail(), resultUser.getEmail());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        Optional<UserDTO> result = userService.getUserById("1");

        assertTrue(result.isPresent());
        assertEquals(user.getName(), result.get().getName());
        assertEquals(user.getEmail(), result.get().getEmail());
        verify(userRepository).findById("1");
    }

    @Test
    void getUserById_NotFound() {
        when(userRepository.findById("1")).thenReturn(Optional.empty());

        Optional<UserDTO> result = userService.getUserById("1");

        assertFalse(result.isPresent());
        verify(userRepository).findById("1");
    }

    @Test
    void getUserByEmail_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        
        Optional<UserDTO> result = userService.getUserByEmail(user.getEmail());
        
        assertTrue(result.isPresent());
        assertEquals(user.getEmail(), result.get().getEmail());
        verify(userRepository, times(1)).findByEmail(anyString());
    }

    @Test
    void updateUser_Success() {
        // Arrange
        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        UserDTO updatedDTO = new UserDTO();
        updatedDTO.setName("Updated Name");
        updatedDTO.setEmail("updated@example.com");
        updatedDTO.setPassword("UpdatedPassword123");
        
        // Act
        UserDTO result = userService.updateUser("1", updatedDTO);
        
        // Assert
        assertNotNull(result);
        assertEquals(updatedDTO.getName(), result.getName());
        assertEquals(updatedDTO.getEmail(), result.getEmail());
        verify(userRepository).findById("1");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_NotFound() {
        when(userRepository.findById("1")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.updateUser("1", userDTO));
        verify(userRepository).findById("1");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_Success() {
        // Arrange
        doNothing().when(userRepository).deleteById("1");

        // Act
        userService.deleteUser("1");

        // Assert
        verify(userRepository).deleteById("1");
    }

    @Test
    void deleteUser_NotFound() {
        // Arrange
        doNothing().when(userRepository).deleteById("1");

        // Act
        userService.deleteUser("1");

        // Assert
        verify(userRepository).deleteById("1");
    }

    @Test
    void updateLastLogin_Success() {
        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        Optional<UserDTO> result = userService.updateLastLogin("1");

        assertTrue(result.isPresent());
        verify(userRepository).findById("1");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateLastLogin_NotFound() {
        when(userRepository.findById("1")).thenReturn(Optional.empty());

        Optional<UserDTO> result = userService.updateLastLogin("1");

        assertFalse(result.isPresent());
        verify(userRepository).findById("1");
        verify(userRepository, never()).save(any(User.class));
    }
} 