package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class CategoryDTO {
    private Long id;
    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;

    public CategoryDTO(String name) {
        this.name = name;
    }

    public CategoryDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public CategoryDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
