package com.example.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequestDTO {
    private Long id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;

    @NotNull(message = "Giá không được để trống")
    @Positive(message = "Giá phải lớn hơn 0")
    private double price;

    @NotNull(message = "Phần trăm giảm không được để trống")
    @Min(value = 0, message = "Phần trăm giảm tối thiểu là 0")
    @Max(value = 100, message = "Phần trăm giảm không vượt quá 100")
    private Integer discount;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

//    // File ảnh (sản phẩm bắt buộc chọn ảnh)
//    @NotNull(message = "Vui lòng chọn ảnh cho sản phẩm")
//    private MultipartFile image;

    // Dành cho cập nhật: nếu không có file mới, giữ lại ảnh cũ
    private String oldImg;

    // Danh sách variants (mỗi variant là 1 sản phẩm con với thông tin size, color và stock)
    @NotNull(message = "Vui lòng cung cấp thông tin variant của sản phẩm")
    private List<VariantDTO> variants;

    @Data
    public static class VariantDTO {
        private Long id;
        @NotBlank(message = "Size không được để trống")
        private String size;

        @NotBlank(message = "Màu không được để trống")
        private String color;

        @NotNull(message = "Số lượng không được để trống")
        @Positive(message = "Số lượng phải lớn hơn 0")
        private Integer stock;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public Integer getStock() {
            return stock;
        }

        public void setStock(Integer stock) {
            this.stock = stock;
        }
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Integer getDiscount() {
        return discount;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getOldImg() {
        return oldImg;
    }

    public void setOldImg(String oldImg) {
        this.oldImg = oldImg;
    }

    public List<VariantDTO> getVariants() {
        return variants;
    }

    public void setVariants(List<VariantDTO> variants) {
        this.variants = variants;
    }
}
