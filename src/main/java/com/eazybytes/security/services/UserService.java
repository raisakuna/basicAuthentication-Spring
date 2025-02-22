package com.eazybytes.security.services;

import com.eazybytes.dtos.UserDTO;
import com.eazybytes.model.User;

import java.util.List;

public interface UserService {
    void updateUserRole(Long userId, String roleName);

    List<User> getAllUsers();

    UserDTO getUserById(Long id);
}
