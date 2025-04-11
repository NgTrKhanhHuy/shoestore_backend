package com.example.backend.controller;


import com.example.backend.config.CustomUserDetails;
import com.example.backend.dto.LoginDTO;
import com.example.backend.dto.RegisterDTO;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody RegisterDTO registerDTO,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }

        // Kiểm tra trùng email & username
        if (userRepository.findByEmail(registerDTO.getEmail()) != null) {
            return ResponseEntity.badRequest().body("Email đã được sử dụng!");
        }
        if (userRepository.findByUsername(registerDTO.getUsername()) != null) {
            return ResponseEntity.badRequest().body("Username đã được sử dụng!");
        }
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu xác nhận không khớp!");
        }

        User registeredUser = authService.register(registerDTO);
        return ResponseEntity.ok("Đăng ký thành công!");
    }

    // Endpoint kiểm tra trùng email
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam("email") String email) {
        boolean exists = userRepository.findByEmail(email) != null;
        return ResponseEntity.ok(Collections.singletonMap("exists", exists));
    }

    // Endpoint kiểm tra trùng username
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam("username") String username) {
        boolean exists = userRepository.findByUsername(username) != null;
        return ResponseEntity.ok(Collections.singletonMap("exists", exists));
    }

//    @PostMapping("/login")
//    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
//        try {
//            // Tạo authentication token từ email và password
//            UsernamePasswordAuthenticationToken authRequest =
//                    new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword());
//            // Xác thực thông tin đăng nhập
//            Authentication authentication = authenticationManager.authenticate(authRequest);
//
//            // Lưu authentication vào SecurityContext
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//            // Lưu SecurityContext vào session
//            request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
//
//            return ResponseEntity.ok("Đăng nhập thành công!");
//        } catch (AuthenticationException ex) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai email hoặc mật khẩu!");
//        }
//    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request,
                                       HttpServletResponse response) {
        try {
            UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword());
            Authentication authentication = authenticationManager.authenticate(authRequest);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            // Kiểm tra role của user
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
//            String redirectPath = isAdmin ? "/admin/home" : "/user";
//
//            // Trả về JSON chứa thông tin chuyển hướng
//            return ResponseEntity.ok(Collections.singletonMap("redirect", redirectPath));
            String redirectParam = request.getParameter("redirect");
            String redirectUrl = (redirectParam != null && !redirectParam.isEmpty())
                    ? redirectParam
                    : (isAdmin ? "/admin/home" : "/");

            // Set cookie "userRole" cho client (không HttpOnly để middleware có thể đọc)
            Cookie roleCookie = new Cookie("userRole", isAdmin ? "ROLE_ADMIN" : "ROLE_CUSTOMER");
            roleCookie.setPath("/");
            response.addCookie(roleCookie);

            // Trả về redirect URL và thông tin user (nếu cần)
            Map<String, Object> result = new HashMap<>();
            result.put("redirect", redirectUrl);
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            result.put("user", Map.of(
                    "email", userDetails.getEmail(),
                    "username", userDetails.getUsername() //  lấy username thực tế
            ));
            return ResponseEntity.ok(result);

        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai email hoặc mật khẩu!");
        }
    }
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập");
        }
        // Giả sử authentication.getPrincipal() trả về UserDetails
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        // Bạn có thể lấy thêm thông tin từ DB nếu cần
        // Ở đây trả về một Map hoặc DTO
        Map<String, Object> profile = new HashMap<>();
        profile.put("email", userDetails.getEmail()); // nếu username là email
        profile.put("username", userDetails.getUsername()); // nếu username là email

        // Nếu bạn đã lưu thêm username trong User entity và CustomUserDetails, trả về nó
        // Ví dụ:
        // Nếu bạn có thêm thông tin khác, thêm vào đây
        return ResponseEntity.ok(profile);
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request, HttpServletResponse response) {
        // Invalidate session để đăng xuất người dùng
        request.getSession().invalidate();

        // Xóa cookie "userRole"
        Cookie userRoleCookie = new Cookie("userRole", null);
        userRoleCookie.setPath("/");         // Đảm bảo đường dẫn giống như khi set cookie
        userRoleCookie.setMaxAge(0);           // Thiết lập thời gian sống là 0 để xóa
        response.addCookie(userRoleCookie);
        return ResponseEntity.ok("Đăng xuất thành công!");
    }



}

