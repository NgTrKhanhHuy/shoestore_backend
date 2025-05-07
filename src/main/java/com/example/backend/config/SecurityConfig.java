package com.example.backend.config;


import com.example.backend.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Cách viết với Lambda
//                .csrf().disable() // Tạm disable CSRF cho API
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/","/api/**","/uploads/**","/api/auth/**","/api/auth/logout","/api/auth/register", "/api/auth/login", "/api/auth/check-email", "/api/auth/check-username").permitAll()
//                        .anyRequest().permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/admin/products/**","/api/admin/categories/**").permitAll()
                                .requestMatchers("/api/cart/**","/api/checkout/**","/api/orders/**","/api/user/**").authenticated() // Yêu cầu xác thực cho giỏ hàng
                                .requestMatchers("/api/admin/**")
//                                .permitAll()
                                .hasRole("ADMIN").anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults()); // Bạn có thể cấu hình xác thực khác nếu cần
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Định nghĩa AuthenticationManager để dùng trong login
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
}

