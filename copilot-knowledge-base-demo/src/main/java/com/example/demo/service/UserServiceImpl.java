package com.example.demo.service;

import com.example.demo.dto.UserDTO;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserDTO> getAllUsers() {
        logger.info("Fetching all users");
        return userRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserById(Long id) {
        logger.info("Fetching user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User not found with id: {}", id);
                    return new NoSuchElementException("User not found with id: " + id);
                });
        return toDTO(user);
    }

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        logger.info("Creating user with email: {}", userDTO.getEmail());
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            logger.warn("User already exists with email: {}", userDTO.getEmail());
            throw new IllegalArgumentException("User already exists with email: " + userDTO.getEmail());
        }
        User user = toEntity(userDTO);
        User savedUser = userRepository.save(user);
        logger.info("User created with id: {}", savedUser.getId());
        return toDTO(savedUser);
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        logger.info("Updating user with id: {}", id);
        User existing = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User not found with id: {}", id);
                    return new NoSuchElementException("User not found with id: " + id);
                });
        if (!existing.getEmail().equalsIgnoreCase(userDTO.getEmail())
                && userRepository.existsByEmail(userDTO.getEmail())) {
            logger.warn("Email already in use: {}", userDTO.getEmail());
            throw new IllegalArgumentException("Email already in use: " + userDTO.getEmail());
        }
        existing.setFirstName(userDTO.getFirstName());
        existing.setLastName(userDTO.getLastName());
        existing.setEmail(userDTO.getEmail());
        User updatedUser = userRepository.save(existing);
        logger.info("User updated with id: {}", updatedUser.getId());
        return toDTO(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        logger.info("Deleting user with id: {}", id);
        if (!userRepository.existsById(id)) {
            logger.warn("User not found with id: {}", id);
            throw new NoSuchElementException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        logger.info("User deleted with id: {}", id);
    }

    private UserDTO toDTO(User user) {
        return new UserDTO(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail());
    }

    private User toEntity(UserDTO userDTO) {
        return new User(userDTO.getFirstName(), userDTO.getLastName(), userDTO.getEmail());
    }
}
