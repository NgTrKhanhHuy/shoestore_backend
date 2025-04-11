package com.example.backend.repository;

import com.example.backend.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface SizeRepository extends JpaRepository<Size, Long> {

    Size findByValue(String size);
}
