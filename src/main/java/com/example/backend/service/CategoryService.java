package com.example.backend.service;

import com.example.backend.dto.CategoryDTO;
import com.example.backend.entity.Category;
import com.example.backend.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    public Category create(CategoryDTO dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Tên danh mục đã tồn tại");
        }

        Category category = new Category();
        category.setName(dto.getName());
        return categoryRepository.save(category);
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }
}
