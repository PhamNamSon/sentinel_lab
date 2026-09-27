package io.namson.targetapi.service;

import org.springframework.stereotype.Service;

import io.namson.targetapi.dto.CreateUserRequest;
import io.namson.targetapi.dto.UserResponse;
import io.namson.targetapi.entity.User;
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

}
