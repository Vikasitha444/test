package com.travel.userservice.controller;

import com.travel.userservice.dto.ApiResponse;
import com.travel.userservice.dto.UserDTO;
import com.travel.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@RequestBody UserDTO userDTO) {
        UserDTO createdUser = userService.createUser(userDTO);
        return ResponseEntity.ok(ApiResponse.success(createdUser, "User created successfully"));
    }

    @GetMapping("/{id}/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateUser(@PathVariable Long id) {
        boolean isValid = userService.validateUser(id);
        return ResponseEntity.ok(ApiResponse.success(isValid, isValid ? "User is valid" : "User not found"));
    }
}
