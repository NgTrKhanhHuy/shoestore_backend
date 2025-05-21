package com.example.backend.repository;

import com.example.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
    @Query(value = "WITH RECURSIVE subcategories AS (" +
            "SELECT id, parent_id FROM categories WHERE id = :categoryId " +
            "UNION ALL " +
            "SELECT c.id, c.parent_id FROM categories c " +
            "INNER JOIN subcategories s ON c.parent_id = s.id) " +
            "SELECT id FROM subcategories", nativeQuery = true)
    List<Long> findAllSubCategoryIds(@Param("categoryId") Long categoryId);

    boolean existsByNameAndParent(String name, Category parent);

    boolean existsByNameAndParentIsNull(String name);


}
