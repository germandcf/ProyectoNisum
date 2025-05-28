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
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ValidationConfigRepository validationConfigRepository;
    private final SecretKey key;
    private final UserValidator userValidator;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, ValidationConfigRepository validationConfigRepository, UserValidator userValidator) {
        this.userRepository = userRepository;
        this.validationConfigRepository = validationConfigRepository;
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        this.userValidator = userValidator;
    }

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        if (userDTO == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }

        List<String> errores = new ArrayList<>();

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

        if (!errores.isEmpty()) {
            String mensaje = String.join(", ", errores);
            throw new IllegalArgumentException(mensaje);
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());
        user.setCreated(new java.util.Date());
        user.setModified(new java.util.Date());
        user.setLastLogin(new java.util.Date());
        user.setToken(UUID.randomUUID().toString());
        user.setActive(true);

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
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
    public UserDTO updateUser(String id, UserDTO userDTO) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

            userValidator.validateUser(userDTO);
            
            user.setName(userDTO.getName());
            user.setEmail(userDTO.getEmail());
            user.setModified(new Date());

            User updatedUser = userRepository.save(user);
            return convertToDTO(updatedUser);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public Optional<UserDTO> updateLastLogin(String id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setLastLogin(new Date());
                    return convertToDTO(userRepository.save(user));
                });
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
                .map(phone -> {
                    PhoneDTO phoneDTO = new PhoneDTO();
                    phoneDTO.setId(phone.getId());
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

    private String generateToken() {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtExpiration);
            
            return Jwts.builder()
                    .setSubject(UUID.randomUUID().toString())
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(key)
                    .compact();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error al generar el token: " + e.getMessage());
        }
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