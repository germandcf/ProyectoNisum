package com.nisum.userservice.service.impl;

import com.nisum.userservice.dto.UserDTO;
import com.nisum.userservice.dto.PhoneDTO;
import com.nisum.userservice.exception.ResourceNotFoundException;
import com.nisum.userservice.model.User;
import com.nisum.userservice.model.Phone;
import com.nisum.userservice.model.ValidationConfig;
import com.nisum.userservice.repository.UserRepository;
import com.nisum.userservice.repository.ValidationConfigRepository;
import com.nisum.userservice.service.UserService;
import com.nisum.userservice.service.validator.UserValidator;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final ValidationConfigRepository validationConfigRepository;
    private final SecretKey key;
    private final long jwtExpiration;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public UserServiceImpl(UserRepository userRepository, 
                         UserValidator userValidator,
                         ValidationConfigRepository validationConfigRepository,
                         @Value("${jwt.secret}") String jwtSecret,
                         @Value("${jwt.expiration}") long jwtExpiration) {
        this.userRepository = userRepository;
        this.userValidator = userValidator;
        this.validationConfigRepository = validationConfigRepository;
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.jwtExpiration = jwtExpiration;
    }

    @Override
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        if (userDTO == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario no puede ser nulo");
        }

        try {
            // Validar el usuario
            userValidator.validateUser(userDTO);

            // Convertir DTO a entidad
            User user = new User();
            user.setName(userDTO.getName());
            user.setEmail(userDTO.getEmail());
            user.setPassword(userDTO.getPassword());
            user.setToken(UUID.randomUUID().toString());
            user.setActive(true);
            
            LocalDateTime now = LocalDateTime.now();
            user.setCreated(now);
            user.setModified(now);
            user.setLastLogin(now);

            // Manejar los teléfonos si existen
            if (userDTO.getPhones() != null && !userDTO.getPhones().isEmpty()) {
                List<Phone> phones = userDTO.getPhones().stream()
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

            // Guardar el usuario
            User savedUser = userRepository.save(user);

            // Convertir la entidad guardada a DTO sin IDs
            UserDTO savedUserDTO = new UserDTO();
            savedUserDTO.setId(savedUser.getId());
            savedUserDTO.setName(savedUser.getName());
            savedUserDTO.setEmail(savedUser.getEmail());
            savedUserDTO.setPassword(savedUser.getPassword());
            savedUserDTO.setCreated(savedUser.getCreated().format(DATE_FORMATTER));
            savedUserDTO.setModified(savedUser.getModified().format(DATE_FORMATTER));
            savedUserDTO.setLastLogin(savedUser.getLastLogin().format(DATE_FORMATTER));
            savedUserDTO.setToken(savedUser.getToken());
            savedUserDTO.setActive(savedUser.isActive());

            // Convertir los teléfonos guardados a DTOs sin IDs
            if (savedUser.getPhones() != null && !savedUser.getPhones().isEmpty()) {
                List<PhoneDTO> phoneDTOs = savedUser.getPhones().stream()
                    .map(phone -> {
                        PhoneDTO phoneDTO = new PhoneDTO();
                        phoneDTO.setNumber(phone.getNumber());
                        phoneDTO.setCityCode(phone.getCityCode());
                        phoneDTO.setCountryCode(phone.getCountryCode());
                        return phoneDTO;
                    })
                    .collect(Collectors.toList());
                savedUserDTO.setPhones(phoneDTOs);
            }

            return savedUserDTO;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(String id) {
        return userRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public UserDTO updateUser(String id, UserDTO userDTO) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setName(userDTO.getName());
                    user.setEmail(userDTO.getEmail());
                    user.setPassword(userDTO.getPassword());
                    user.setModified(LocalDateTime.now());
                    return userRepository.save(user);
                })
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Optional<UserDTO> updateLastLogin(String id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setLastLogin(LocalDateTime.now());
                    return userRepository.save(user);
                })
                .map(this::convertToDTO);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
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
                .map(phone -> {
                    PhoneDTO phoneDTO = new PhoneDTO();
                    phoneDTO.setNumber(phone.getNumber());
                    phoneDTO.setCityCode(phone.getCityCode());
                    phoneDTO.setCountryCode(phone.getCountryCode());
                    return phoneDTO;
                })
                .collect(Collectors.toList());
            dto.setPhones(phoneDTOs);
        }
        
        return dto;
    }

    private User convertToEntity(UserDTO dto) {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
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
} 