package com.nisum.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nisum.userservice.dto.UserDTO;
import com.nisum.userservice.dto.PhoneDTO;
import com.nisum.userservice.dto.ErrorResponse;
import com.nisum.userservice.model.User;
import com.nisum.userservice.model.Phone;
import com.nisum.userservice.model.ValidationConfig;
import com.nisum.userservice.service.UserService;
import com.nisum.userservice.repository.ValidationConfigRepository;
import com.nisum.userservice.repository.UserRepository;
import com.nisum.userservice.service.validator.UserValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.ArrayList;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@Tag(name = "User Controller", description = "APIs para gestión de usuarios")
public class UserController {

    private final UserService userService;
    private final ValidationConfigRepository validationConfigRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final UserValidator userValidator;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public UserController(UserService userService, 
                         ValidationConfigRepository validationConfigRepository,
                         UserRepository userRepository,
                         ObjectMapper objectMapper,
                         UserValidator userValidator) {
        this.userService = userService;
        this.validationConfigRepository = validationConfigRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.userValidator = userValidator;
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo usuario", description = "Crea un nuevo usuario con la información proporcionada")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente",
            content = @Content(schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos de usuario inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) {
        try {
            userValidator.validateUser(userDTO);
            UserDTO createdUser = userService.createUser(userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
    }

    @GetMapping
    @Operation(summary = "Obtener todos los usuarios", description = "Retorna una lista de todos los usuarios registrados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        try {
            List<UserDTO> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error al obtener los usuarios: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID", description = "Retorna un usuario específico basado en su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario encontrado exitosamente",
            content = @Content(schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserDTO> getUserById(
            @PathVariable String id) {
        return userService.getUserById(id)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Obtener usuario por email", description = "Retorna un usuario específico basado en su email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario encontrado exitosamente",
            content = @Content(schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserDTO> getUserByEmail(
            @PathVariable String email) {
        return userService.getUserByEmail(email)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Actualiza la información de un usuario existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente",
            content = @Content(schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos de usuario inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody UserDTO userDTO) {
        try {
            userValidator.validateUser(userDTO);
            UserDTO updatedUser = userService.updateUser(id, userDTO);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse("Error al actualizar el usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error al eliminar el usuario: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/last-login")
    @Operation(summary = "Actualizar último login", description = "Actualiza la fecha del último login del usuario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Último login actualizado exitosamente",
            content = @Content(schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserDTO> updateLastLogin(@PathVariable String id) {
        return userService.updateLastLogin(id)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    @GetMapping("/validation-config")
    @Operation(summary = "Obtener configuración de validación", description = "Retorna la configuración actual de validación")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuración obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = ValidationConfig.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getValidationConfig() {
        return ResponseEntity.ok(userValidator.getValidationConfig());
    }

    private PhoneDTO convertPhoneToDTO(Phone phone) {
        PhoneDTO dto = new PhoneDTO();
        dto.setNumber(phone.getNumber());
        dto.setCityCode(phone.getCityCode());
        dto.setCountryCode(phone.getCountryCode());
        return dto;
    }

    private Phone convertPhoneToEntity(PhoneDTO dto) {
        Phone phone = new Phone();
        phone.setNumber(dto.getNumber());
        phone.setCityCode(dto.getCityCode());
        phone.setCountryCode(dto.getCountryCode());
        return phone;
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPassword(user.getPassword());
        dto.setCreated(user.getCreated().format(DATE_FORMATTER));
        dto.setModified(user.getModified().format(DATE_FORMATTER));
        dto.setLastLogin(user.getLastLogin() != null ? user.getLastLogin().format(DATE_FORMATTER) : null);
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

    private User convertToEntity(UserDTO dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        if (dto.getPhones() != null) {
            List<Phone> phones = dto.getPhones().stream()
                .map(phoneDTO -> {
                    Phone phone = new Phone();
                    phone.setNumber(phoneDTO.getNumber());
                    phone.setCityCode(phoneDTO.getCityCode());
                    phone.setCountryCode(phoneDTO.getCountryCode());
                    phone.setUser(user);
                    return phone;
                })
                .collect(Collectors.toList());
            user.setPhones(phones);
        }
        return user;
    }

    private String generateExampleJson() {
        try {
            UserDTO example = new UserDTO();
            example.setName("Juan Rodríguez");
            example.setEmail("juan@rodriguez.org");
            example.setPassword("hunter2");
            example.setActive(true);
            
            List<PhoneDTO> phones = new ArrayList<>();
            PhoneDTO phone = new PhoneDTO();
            phone.setNumber("1234567");
            phone.setCityCode("1");
            phone.setCountryCode("57");
            phones.add(phone);
            example.setPhones(phones);
            
            return objectMapper.writeValueAsString(example);
        } catch (Exception e) {
            return "{}";
        }
    }
} 