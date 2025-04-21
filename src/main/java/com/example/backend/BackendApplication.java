package com.example.backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        // Thiết lập các biến môi trường vào System properties (nếu cần)
        // Ví dụ:
//        System.setProperty("SENDGRID_API_KEY", dotenv.get("SENDGRID_API_KEY"));
//                System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));

        SpringApplication.run(BackendApplication.class, args);
    }

}
