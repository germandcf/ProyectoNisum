package com.nisum.userservice.service.validator;

import com.nisum.userservice.dto.UserDTO;
import com.nisum.userservice.model.ValidationConfig;
import java.util.List;

public interface UserValidator {
    void validateUser(UserDTO userDTO);
    List<ValidationConfig> getValidationConfig();
} 