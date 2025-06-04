package com.indhive.security;

import com.indhive.model.User;
import com.indhive.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

        @Autowired
        private UserRepository userRepository;

        @Override
        public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "Usuario no encontrado con email: " + email));

                List<SimpleGrantedAuthority> authorities = Arrays.stream(user.getRoles().split(","))
                                .map(role -> new SimpleGrantedAuthority(role.trim()))
                                .collect(Collectors.toList());

                return new org.springframework.security.core.userdetails.User(
                                user.getEmail(), // Usar el email como identificador principal
                                user.getPassword(),
                                authorities);
        }
}
