package io.namson.targetapi.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;

import io.namson.targetapi.dto.UserResponse;
import io.namson.targetapi.service.UserService;
import io.namson.targetapi.dto.CreateUserRequest;
import io.namson.targetapi.dto.UserOrderResponse;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {

        this.userService = userService;

    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@RequestBody CreateUserRequest request) {

        return userService.createUser(request);

    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable UUID id) {

        return userService.getUserById(id);

    }

    @GetMapping("/{id}/orders")
    public List<UserOrderResponse> getUserOrders(@PathVariable UUID id) {

        return userService.getUserOrders(id);

    }

}
