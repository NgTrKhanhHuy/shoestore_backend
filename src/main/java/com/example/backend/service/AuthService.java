package com.example.backend.service;


import com.example.backend.dto.RegisterDTO;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(RegisterDTO registerDTO) {
        User user = new User();
        user.setEmail(registerDTO.getEmail());
        user.setUsername(registerDTO.getUsername());
        // Mã hóa mật khẩu
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));

        // Gán role mặc định là ROLE_CUSTOMER
        Role role = roleRepository.findByName("ROLE_CUSTOMER");
        if (role == null) {
            role = new Role();
            role.setName("ROLE_CUSTOMER");
            role = roleRepository.save(role);
        }
        user.setRoles(Collections.singletonList(role));
        return userRepository.save(user);
    }
}

