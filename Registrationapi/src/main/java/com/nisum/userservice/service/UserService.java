package com.nisum.userservice.service;

import com.nisum.userservice.dto.UserDTO;
import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDTO createUser(UserDTO userDTO);
    List<UserDTO> getAllUsers();
    Optional<UserDTO> getUserById(String id);
    Optional<UserDTO> getUserByEmail(String email);
    UserDTO updateUser(String id, UserDTO userDTO);
    void deleteUser(String id);
    Optional<UserDTO> updateLastLogin(String id);
} 