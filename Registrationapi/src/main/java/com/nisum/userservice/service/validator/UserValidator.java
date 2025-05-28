package com.nisum.userservice.service.validator;

import com.nisum.userservice.dto.UserDTO;

public interface UserValidator {
    void validateUser(UserDTO userDTO);
} 