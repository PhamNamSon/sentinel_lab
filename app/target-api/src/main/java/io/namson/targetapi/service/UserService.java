package io.namson.targetapi.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import io.namson.targetapi.dto.CreateUserRequest;
import io.namson.targetapi.dto.UserResponse;
import io.namson.targetapi.entity.User;
import io.namson.targetapi.exception.ResourceNotFoundException;
import io.namson.targetapi.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse createUser(CreateUserRequest request) {
        User user = new User(
                request.name(),
                request.email());

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getUuid(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getCreatedAt());
    }

    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        return new UserResponse(
                user.getUuid(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt());
    }

}
