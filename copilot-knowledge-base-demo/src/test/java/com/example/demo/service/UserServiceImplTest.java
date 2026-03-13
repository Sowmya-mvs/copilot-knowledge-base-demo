package com.example.demo.service;

import com.example.demo.dto.UserDTO;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        user = new User("Jane", "Doe", "jane@example.com");
        user.setId(1L);
        userDTO = new UserDTO(null, "Jane", "Doe", "jane@example.com");
    }

    @Test
    void getAllUsers_returnsEmptyList_whenNoUsers() {
        when(userRepository.findAll()).thenReturn(List.of());
        assertThat(userService.getAllUsers()).isEmpty();
    }

    @Test
    void getAllUsers_returnsMappedDTOs() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        List<UserDTO> result = userService.getAllUsers();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    void getUserById_returnsDTO_whenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        UserDTO result = userService.getUserById(1L);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    void getUserById_throwsException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createUser_savesAndReturnsDTO_whenEmailIsNew() {
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        UserDTO result = userService.createUser(userDTO);
        assertThat(result.getEmail()).isEqualTo("jane@example.com");
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void createUser_throwsException_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);
        assertThatThrownBy(() -> userService.createUser(userDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("jane@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_updatesAndReturnsDTO_whenUserExists() {
        UserDTO updateDTO = new UserDTO(null, "John", "Doe", "john@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        User updated = new User("John", "Doe", "john@example.com");
        updated.setId(1L);
        when(userRepository.save(any(User.class))).thenReturn(updated);
        UserDTO result = userService.updateUser(1L, updateDTO);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void updateUser_throwsException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.updateUser(99L, userDTO))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void updateUser_throwsException_whenEmailTakenByOtherUser() {
        UserDTO updateDTO = new UserDTO(null, "Jane", "Doe", "other@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("other@example.com")).thenReturn(true);
        assertThatThrownBy(() -> userService.updateUser(1L, updateDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("other@example.com");
    }

    @Test
    void deleteUser_deletesUser_whenUserExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        userService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_throwsException_whenUserNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(NoSuchElementException.class);
    }
}
