package com.example.backend.service;

import com.example.backend.config.CustomUserDetails;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getPassword(),
                getAuthorities(user));

    }

    private List<GrantedAuthority> getAuthorities(User user) {
        // Bạn có thể lấy quyền (roles) từ user và chuyển nó thành quyền (authority)
        return AuthorityUtils.createAuthorityList(user.getRoles().stream()
                .map(role -> role.getName())
                .toArray(String[]::new));
    }
}
