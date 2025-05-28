package com.nisum.userservice.service;

import com.nisum.userservice.dto.UserDTO;
import com.nisum.userservice.model.User;
import com.nisum.userservice.model.ValidationConfig;
import com.nisum.userservice.repository.UserRepository;
import com.nisum.userservice.repository.ValidationConfigRepository;
import com.nisum.userservice.service.impl.UserServiceImpl;
import com.nisum.userservice.service.validator.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Date;

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

    @InjectMocks
    private UserServiceImpl userService;

    private UserDTO userDTO;
    private User user;

    @BeforeEach
    void setUp() {
        // Inicializar User
        user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("Password123@");
        user.setCreated(new Date());
        user.setModified(new Date());
        user.setLastLogin(new Date());
        user.setToken("test-token");
        user.setActive(true);

        // Inicializar UserDTO
        userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setPassword(user.getPassword());
        userDTO.setCreated(user.getCreated());
        userDTO.setModified(user.getModified());
        userDTO.setLastLogin(user.getLastLogin());
        userDTO.setToken(user.getToken());
        userDTO.setActive(user.isActive());
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
        // Configurar validaciones
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

        // Configurar validación de email
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        
        // Configurar guardado
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        UserDTO result = userService.createUser(userDTO);
        
        assertNotNull(result);
        assertEquals(userDTO.getName(), result.getName());
        assertEquals(userDTO.getEmail(), result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(validationConfigRepository, times(1)).findByConfigKey("email.regex");
        verify(validationConfigRepository, times(1)).findByConfigKey("password.min.length");
        verify(validationConfigRepository, times(1)).findByConfigKey("password.pattern");
    }

    @Test
    void getAllUsers_Success() {
        List<User> users = Arrays.asList(user);
        when(userRepository.findAll()).thenReturn(users);
        
        List<UserDTO> result = userService.getAllUsers();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(user.getName(), result.get(0).getName());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));
        
        Optional<UserDTO> result = userService.getUserById(user.getId());
        
        assertTrue(result.isPresent());
        assertEquals(user.getName(), result.get().getName());
        verify(userRepository, times(1)).findById(anyString());
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
        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        doNothing().when(userValidator).validateUser(any(UserDTO.class));
        
        UserDTO updatedDTO = new UserDTO();
        updatedDTO.setName("Updated Name");
        updatedDTO.setEmail("updated@example.com");
        
        UserDTO result = userService.updateUser(user.getId(), updatedDTO);
        
        assertNotNull(result);
        assertEquals(updatedDTO.getName(), result.getName());
        assertEquals(updatedDTO.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findById(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userValidator, times(1)).validateUser(any(UserDTO.class));
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.existsById(anyString())).thenReturn(true);
        doNothing().when(userRepository).deleteById(anyString());
        
        userService.deleteUser(user.getId());
        
        verify(userRepository, times(1)).existsById(anyString());
        verify(userRepository, times(1)).deleteById(anyString());
    }

    @Test
    void updateLastLogin_Success() {
        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        Optional<UserDTO> result = userService.updateLastLogin(user.getId());
        
        assertTrue(result.isPresent());
        assertNotNull(result.get().getLastLogin());
        verify(userRepository, times(1)).findById(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }
} 