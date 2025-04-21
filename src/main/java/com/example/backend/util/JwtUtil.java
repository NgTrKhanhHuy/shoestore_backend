//package com.example.backend.util;
//
//import io.jsonwebtoken.*;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.stereotype.Component;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Component
//public class JwtUtil {
//    @Value("${jwt.secret}")
//
//    private String SECRET_KEY;
//
//    public String generateToken(String username, Collection<? extends GrantedAuthority> authorities) {
//        List<String> roles = authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
//
//        return Jwts.builder()
//                .setSubject(username)
//                .claim("roles", roles)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + 3600_000)) // 1h
//                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
//                .compact();
//    }
//
//    public String getUsernameFromToken(String token) {
//        return extractClaims(token).getSubject();
//    }
//
//    public List<String> getRolesFromToken(String token) {
//        return extractClaims(token).get("roles", List.class);
//    }
//
//    public boolean isTokenValid(String token) {
//        try {
//            extractClaims(token);
//            return true;
//        } catch (JwtException e) {
//            return false;
//        }
//    }
//
//    private Claims extractClaims(String token) {
//        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
//    }
//}
