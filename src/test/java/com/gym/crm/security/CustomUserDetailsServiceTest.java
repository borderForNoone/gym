package com.gym.crm.security;

import com.gym.crm.model.User;
import com.gym.crm.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {
    private static final String USERNAME = "tom.tomas";
    private static final String NOT_FOUND_USERNAME = "unknown";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        User user = User.builder().username(USERNAME).password("hash").isActive(true).build();

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername(USERNAME);
        assertThat(result.getUsername()).isEqualTo(USERNAME);
        assertThat(result.getPassword()).isEqualTo("hash");
        assertThat(result.isEnabled()).isTrue();
        verify(userRepository, times(1)).findByUsername(USERNAME);
    }

    @Test
    void loadUserByUsername_shouldReturnDisabledUserDetails_whenUserIsInactive() {
        User user = User.builder().username(USERNAME).password("hash").isActive(false).build();

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername(USERNAME);
        assertThat(result.getUsername()).isEqualTo(USERNAME);
        assertThat(result.isEnabled()).isFalse();
        verify(userRepository, times(1)).findByUsername(USERNAME);
    }

    @Test
    void loadUserByUsername_shouldThrow_whenUserNotFound() {
        when(userRepository.findByUsername(NOT_FOUND_USERNAME)).thenReturn(Optional.empty());

        Throwable actual = catchThrowable(() -> service.loadUserByUsername(NOT_FOUND_USERNAME));

        assertThat(actual).isInstanceOf(UsernameNotFoundException.class).hasMessageContaining(NOT_FOUND_USERNAME);
        verify(userRepository, times(1)).findByUsername(NOT_FOUND_USERNAME);
    }
}