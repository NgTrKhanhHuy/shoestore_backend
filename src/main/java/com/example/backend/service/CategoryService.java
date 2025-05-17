package com.example.backend.service;

import com.example.backend.dto.CategoryDTO;
import com.example.backend.dto.CategoryTreeDTO;
import com.example.backend.entity.Category;
import com.example.backend.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
//    public Category create(CategoryDTO dto) {
//        if (categoryRepository.existsByName(dto.getName())) {
//            throw new IllegalArgumentException("Tên danh mục đã tồn tại");
//        }
//
//        Category category = new Category();
//        category.setName(dto.getName());
//        return categoryRepository.save(category);
//    }
    public Category create(CategoryDTO dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Tên danh mục đã tồn tại");
        }

        Category category = new Category();
        category.setName(dto.getName());

        if (dto.getParentId() != null) {
            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục cha"));
            category.setParent(parent);
        }

        return categoryRepository.save(category);
    }


    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public List<CategoryTreeDTO> getCategoryTree() {
        List<Category> allCategories = categoryRepository.findAll();
        Map<Long, CategoryTreeDTO> dtoMap = new HashMap<>();

        for (Category category : allCategories) {
            dtoMap.put(category.getId(), new CategoryTreeDTO(category));
        }

        List<CategoryTreeDTO> roots = new ArrayList<>();

        for (Category category : allCategories) {
            CategoryTreeDTO dto = dtoMap.get(category.getId());
            if (category.getParent() != null) {
                CategoryTreeDTO parentDTO = dtoMap.get(category.getParent().getId());
                parentDTO.getChildren().add(dto);
            } else {
                roots.add(dto);
            }
        }

        return roots;
    }

}
