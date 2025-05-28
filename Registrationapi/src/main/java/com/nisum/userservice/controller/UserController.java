package com.nisum.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nisum.userservice.dto.UserDTO;
import com.nisum.userservice.dto.PhoneDTO;
import com.nisum.userservice.model.User;
import com.nisum.userservice.model.Phone;
import com.nisum.userservice.model.ValidationConfig;
import com.nisum.userservice.service.UserService;
import com.nisum.userservice.repository.ValidationConfigRepository;
import com.nisum.userservice.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.ArrayList;
import java.util.Date;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final ValidationConfigRepository validationConfigRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public UserController(UserService userService, 
                         ValidationConfigRepository validationConfigRepository,
                         UserRepository userRepository,
                         ObjectMapper objectMapper) {
        this.userService = userService;
        this.validationConfigRepository = validationConfigRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "Crear un nuevo usuario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente",
            content = @Content(schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "400", description = "Error de validación",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        if (userDTO == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faltan campos requeridos en la solicitud");
        }

        // Validar campos requeridos (solo si son nulos o vacíos)
        if (userDTO.getName() == null || userDTO.getName().trim().isEmpty() ||
            userDTO.getEmail() == null || userDTO.getEmail().trim().isEmpty() ||
            userDTO.getPassword() == null || userDTO.getPassword().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faltan campos requeridos en la solicitud");
        }

        // Validar email
        ValidationConfig emailRegexConfig = validationConfigRepository.findByConfigKey("email.regex")
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error de configuración: expresión regular no encontrada"));

        String regex = emailRegexConfig.getConfigValue().replace("\\\\", "\\");
        if (!userDTO.getEmail().matches(regex)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de correo incorrecto");
        }

        // Validar unicidad del email
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo ya está registrado");
        }

        // Validar longitud mínima de la contraseña
        validationConfigRepository.findByConfigKey("password.min.length")
            .ifPresent(config -> {
                int minLength = Integer.parseInt(config.getConfigValue());
                if (userDTO.getPassword().length() < minLength) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña no cumple con los requisitos");
                }
            });

        // Validar patrón de la contraseña
        validationConfigRepository.findByConfigKey("password.pattern")
            .ifPresent(config -> {
                String pattern = config.getConfigValue();
                if (!userDTO.getPassword().matches(pattern)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña no cumple con los requisitos");
                }
            });

        // Validar nombre
        validationConfigRepository.findByConfigKey("name.min.length")
            .ifPresent(config -> {
                int minLength = Integer.parseInt(config.getConfigValue());
                if (userDTO.getName().length() < minLength) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre no cumple con los requisitos");
                }
            });

        // Validar teléfonos solo si hay al menos uno
        if (userDTO.getPhones() != null && !userDTO.getPhones().isEmpty()) {
            for (PhoneDTO phone : userDTO.getPhones()) {
                if (phone.getNumber() == null || phone.getNumber().trim().isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El número de teléfono es requerido");
                }
                if (phone.getCityCode() == null || phone.getCityCode().trim().isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código de ciudad es requerido");
                }
                if (phone.getCountryCode() == null || phone.getCountryCode().trim().isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código de país es requerido");
                }
            }
        }

        UserDTO createdUser = userService.createUser(userDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @Operation(summary = "Obtener todos los usuarios")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = UserDTO.class, example = "#{generateExampleJson()}"))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Obtener un usuario por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario encontrado",
            content = @Content(schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Obtener un usuario por su email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario encontrado",
            content = @Content(schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Actualizar un usuario existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente",
            content = @Content(schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "400", description = "Error de validación",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody UserDTO userDTO) {
        try {
            UserDTO updatedUser = userService.updateUser(id, userDTO);
            return ResponseEntity.ok(updatedUser);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getReason()), e.getStatus());
        } catch (Exception e) {
            e.printStackTrace(); // Para debugging
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Eliminar un usuario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Actualizar el último login de un usuario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Último login actualizado exitosamente",
            content = @Content(schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PatchMapping("/{id}/last-login")
    public ResponseEntity<UserDTO> updateLastLogin(@PathVariable String id) {
        return userService.updateLastLogin(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Schema(description = "Respuesta de error")
    public static class ErrorResponse {
        @Schema(description = "Mensaje de error", 
                example = "La contraseña no cumple con los requisitos",
                allowableValues = {
                    "La contraseña no cumple con los requisitos",
                    "Faltan campos requeridos en la solicitud",
                    "Formato de correo incorrecto",
                    "El correo ya está registrado",
                    "El nombre no cumple con los requisitos",
                    "El usuario no puede ser nulo",
                    "Todos los campos son requeridos"
                })
        private final String mensaje;

        public ErrorResponse(String mensaje) {
            this.mensaje = mensaje;
        }

        public String getMensaje() {
            return mensaje;
        }
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPassword(user.getPassword());
        dto.setCreated(user.getCreated());
        dto.setModified(user.getModified());
        dto.setLastLogin(user.getLastLogin());
        dto.setToken(user.getToken());
        dto.setActive(user.isActive());
        
        if (user.getPhones() != null) {
            List<PhoneDTO> phoneDTOs = user.getPhones().stream()
                .map(this::convertPhoneToDTO)
                .collect(Collectors.toList());
            dto.setPhones(phoneDTOs);
        }
        
        return dto;
    }

    private PhoneDTO convertPhoneToDTO(Phone phone) {
        PhoneDTO dto = new PhoneDTO();
        dto.setId(phone.getId());
        dto.setNumber(phone.getNumber());
        dto.setCityCode(phone.getCityCode());
        dto.setCountryCode(phone.getCountryCode());
        return dto;
    }

    private User convertToEntity(UserDTO dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        if (dto.getPhones() != null) {
            List<Phone> phones = dto.getPhones().stream()
                .map(this::convertPhoneToEntity)
                .collect(Collectors.toList());
            user.setPhones(phones);
        }
        return user;
    }

    private Phone convertPhoneToEntity(PhoneDTO dto) {
        Phone phone = new Phone();
        phone.setNumber(dto.getNumber());
        phone.setCityCode(dto.getCityCode());
        phone.setCountryCode(dto.getCountryCode());
        return phone;
    }

    private String generateExampleJson() {
        try {
            UserDTO example = new UserDTO();
            example.setId("uuid");
            example.setName("nombre");
            example.setEmail("email@dominio.com");
            example.setPassword("password");
            
            PhoneDTO phone = new PhoneDTO();
            phone.setId(1L);
            phone.setNumber("123456789");
            phone.setCityCode("123");
            phone.setCountryCode("123");
            
            example.setPhones(List.of(phone));
            example.setCreated(new Date());
            example.setModified(new Date());
            example.setLastLogin(new Date());
            example.setToken("token");
            example.setActive(true);
            
            return objectMapper.writeValueAsString(example);
        } catch (Exception e) {
            return "{}";
        }
    }
} 