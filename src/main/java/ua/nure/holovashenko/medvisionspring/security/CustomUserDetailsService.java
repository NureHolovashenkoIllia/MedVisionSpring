package ua.nure.holovashenko.medvisionspring.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import ua.nure.holovashenko.medvisionspring.entity.User;
import ua.nure.holovashenko.medvisionspring.repository.UserRepository;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPw(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getUserRole().name()))
        );
    }
}
