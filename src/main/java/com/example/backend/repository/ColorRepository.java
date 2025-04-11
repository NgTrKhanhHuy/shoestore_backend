package com.example.backend.repository;

import com.example.backend.entity.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface ColorRepository extends JpaRepository<Color, Long> {
    Color findByName(String color);
}
