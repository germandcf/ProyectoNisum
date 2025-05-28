package com.nisum.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nisum.userservice.dto.UserDTO;
import com.nisum.userservice.service.UserService;
import com.nisum.userservice.repository.ValidationConfigRepository;
import com.nisum.userservice.repository.UserRepository;
import com.nisum.userservice.model.ValidationConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private ValidationConfigRepository validationConfigRepository;

    @MockBean
    private UserRepository userRepository;

    private UserDTO userDTO;
    private String userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID().toString();
        userDTO = new UserDTO();
        userDTO.setId(userId);
        userDTO.setName("Test User");
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("Password123@");
        userDTO.setCreated(new Date());
        userDTO.setModified(new Date());
        userDTO.setLastLogin(new Date());
        userDTO.setToken("test-token");
        userDTO.setActive(true);

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

        // Configurar el mock del repositorio de usuarios
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    }

    @Test
    void createUser_Success() throws Exception {
        when(userService.createUser(any(UserDTO.class))).thenReturn(userDTO);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value(userDTO.getName()))
                .andExpect(jsonPath("$.email").value(userDTO.getEmail()));
    }

    @Test
    void getAllUsers_Success() throws Exception {
        List<UserDTO> users = Arrays.asList(userDTO);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(userId))
                .andExpect(jsonPath("$[0].name").value(userDTO.getName()))
                .andExpect(jsonPath("$[0].email").value(userDTO.getEmail()));
    }

    @Test
    void getUserById_Success() throws Exception {
        when(userService.getUserById(userId)).thenReturn(Optional.of(userDTO));

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value(userDTO.getName()))
                .andExpect(jsonPath("$.email").value(userDTO.getEmail()));
    }

    @Test
    void getUserById_NotFound() throws Exception {
        when(userService.getUserById(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/{id}", "non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserByEmail_Success() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(Optional.of(userDTO));

        mockMvc.perform(get("/api/users/email/{email}", userDTO.getEmail()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value(userDTO.getEmail()));
    }

    @Test
    void getUserByEmail_NotFound() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/email/{email}", "non-existent@email.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_Success() throws Exception {
        when(userService.updateUser(anyString(), any(UserDTO.class))).thenReturn(userDTO);

        mockMvc.perform(put("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value(userDTO.getName()))
                .andExpect(jsonPath("$.email").value(userDTO.getEmail()));
    }

    @Test
    void deleteUser_Success() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateLastLogin_Success() throws Exception {
        when(userService.updateLastLogin(userId)).thenReturn(Optional.of(userDTO));

        mockMvc.perform(patch("/api/users/{id}/last-login", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.lastLogin").exists());
    }

    @Test
    void updateLastLogin_NotFound() throws Exception {
        when(userService.updateLastLogin(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/users/{id}/last-login", "non-existent-id"))
                .andExpect(status().isNotFound());
    }
} 