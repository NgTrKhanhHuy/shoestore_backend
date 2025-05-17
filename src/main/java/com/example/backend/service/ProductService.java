package com.example.backend.service;

import com.example.backend.dto.ProductRequestDTO;
import com.example.backend.dto.ProductRequestDTO.VariantDTO;
import com.example.backend.dto.ProductResponseDto;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {
    @Autowired
    private  ProductRepository productRepository;
    @Autowired
    private  CategoryRepository categoryRepository;
    @Autowired

    private  ColorRepository colorRepository;
    @Autowired

    private  SizeRepository sizeRepository;
    @Autowired
    private  ProductVariantRepository productVariantRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;



    //    public Page<ProductResponseDto> findPaginatedProducts(Pageable pageable) {
//        return productRepository.findAll(pageable).map(this::convertToDto);
//    }
public Page<ProductResponseDto> findPaginatedProducts(Pageable pageable, String search) {
    Specification<Product> spec = createSearchSpecification(search);
    return productRepository.findAll(spec, pageable).map(this::convertToDto);
}
    // Hàm mới dành cho public (có cả search và category filter)
    public Page<ProductResponseDto> findPaginatedProductsForPublic(
            Pageable pageable,
            String search,
            Long categoryId,
            List<String> sizes
    ) {
        Specification<Product> spec = Specification.where(createSearchSpecification(search))
                .and(createCategorySpecification(categoryId))
                .and(createSizeSpecification(sizes));
        return productRepository.findAll(spec, pageable).map(this::convertToDto);
    }
    private Specification<Product> createCategorySpecification(Long categoryId) {
        return (root, query, criteriaBuilder) -> {
            if (categoryId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("category").get("id"), categoryId);
        };
    }

    private Specification<Product> createSearchSpecification(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + search.toLowerCase() + "%"
            );
        };
    }
    private Specification<Product> createSizeSpecification(List<String> sizes) {
        return (root, query, criteriaBuilder) -> {
            if (sizes == null || sizes.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            query.distinct(true); // Avoid duplicate products
            // Join with variants and filter by size
            return root.join("variants").get("size").get("value").in(sizes);
        };
    }
//    tim kiem theo 2 truong la name va mo ta
//    private Specification<Product> createSearchSpecification(String search) {
//        return (root, query, criteriaBuilder) -> {
//            if (search == null || search.trim().isEmpty()) {
//                return criteriaBuilder.conjunction();
//            }
//            String pattern = "%" + search.toLowerCase() + "%";
//            return criteriaBuilder.or(
//                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
//                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
//            );
//        };
//    }

//    public ProductResponseDto convertToDto(Product product) {
//        ProductResponseDto dto = new ProductResponseDto();
//        dto.setId(product.getId());
//        dto.setName(product.getName());
//        dto.setDescription(product.getDescription());
//        dto.setPrice(product.getPrice());
//        dto.setImageUrl(product.getImageUrl());
//        dto.setDiscount(product.getDiscount());
//        dto.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
//
//
//        // Chuyển đổi các variant (lọc theo sizes nếu cần)
//        List<ProductResponseDto.ProductVariantResponseDto> variantDtos =
//                product.getVariants().stream()
//                        .filter(variant -> {
//                            // Nếu sizes được cung cấp, chỉ giữ các variant có size khớp
//                            // Comment dòng dưới nếu muốn giữ tất cả variants
//                            // return sizes == null || sizes.isEmpty() || sizes.contains(variant.getSize().getValue());
//                            return true; // Giữ tất cả variants
//                        })
//                        .map(variant -> {
//                            ProductResponseDto.ProductVariantResponseDto vDto = new ProductResponseDto.ProductVariantResponseDto();
//                            vDto.setId(variant.getId());
//                            vDto.setStock(variant.getStock());
//                            vDto.setSize(variant.getSize() != null ? variant.getSize().getValue() : null);
//                            vDto.setColor(variant.getColor() != null ? variant.getColor().getName() : null);
//
//                            return vDto;
//                        })
//                        .toList();
//        dto.setVariants(variantDtos);
//        return dto;
//    }
public ProductResponseDto convertToDto(Product product) {
    ProductResponseDto dto = new ProductResponseDto();
    dto.setId(product.getId());
    dto.setName(product.getName());
    dto.setDescription(product.getDescription());
    dto.setPrice(product.getPrice());
    dto.setImageUrl(product.getImageUrl());
    dto.setDiscount(product.getDiscount());
    dto.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);

    List<ProductResponseDto.ProductVariantResponseDto> variantDtos = new ArrayList<>();
    int totalSold = 0;

    // Duyệt từng variant, build DTO và cộng tổng đã bán
    for (ProductVariant variant : product.getVariants()) {
        ProductResponseDto.ProductVariantResponseDto vDto =
                new ProductResponseDto.ProductVariantResponseDto();
        vDto.setId(variant.getId());
        vDto.setStock(variant.getStock());
        vDto.setSize(variant.getSize() != null ? variant.getSize().getValue() : null);
        vDto.setColor(variant.getColor() != null ? variant.getColor().getName() : null);

        // Gọi repository đúng method
        Integer soldCount = orderItemRepository
                .sumSoldByVariantAndStatus(
                        variant.getId(),
                        OrderStatus.DELIVERED      // truyền enum, không .getStatusCode()
                );
        totalSold += (soldCount != null ? soldCount : 0);

        variantDtos.add(vDto);
    }

    dto.setVariants(variantDtos);
    dto.setTotalSold(totalSold);
    return dto;
}    // Đường dẫn thư mục lưu ảnh (user.dir + /uploads/images/)
    private final String uploadDir = System.getProperty("user.dir") + "/uploads/images";

    public Product createProduct(ProductRequestDTO dto, MultipartFile file) throws IOException {
        // Upload file ảnh và lấy URL ảnh
        String imageUrl = uploadFile(file);

        // Tìm danh mục theo categoryId
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại"));

        // Tạo Product
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setDiscount(dto.getDiscount());
        product.setImageUrl(imageUrl);
        product.setCategory(category);

        // Lưu sản phẩm để có ID
        product = productRepository.save(product);

        // Xử lý từng variant
        List<VariantDTO> variantDTOs = dto.getVariants();
        if (variantDTOs != null) {
            for (VariantDTO variantDTO : variantDTOs) {
                ProductVariant variant = new ProductVariant();
                variant.setStock(variantDTO.getStock());
                variant.setProduct(product);

                // Xử lý Size: nếu đã có thì sử dụng, nếu chưa tạo mới
                Size size = sizeRepository.findByValue(variantDTO.getSize());
                if (size == null) {
                    size = new Size();
                    size.setValue(variantDTO.getSize());
                    size = sizeRepository.save(size);
                }
                variant.setSize(size);

                // Xử lý Color: nếu đã có thì sử dụng, nếu chưa tạo mới
                Color color = colorRepository.findByName(variantDTO.getColor());
                if (color == null) {
                    color = new Color();
                    color.setName(variantDTO.getColor());
                    color = colorRepository.save(color);
                }
                variant.setColor(color);

                // Lưu variant
                productVariantRepository.save(variant);
                product.getVariants().add(variant);
            }
        }
        return product;
    }

    public Product updateProduct(ProductRequestDTO dto, MultipartFile file) throws IOException {
        // Lấy sản phẩm cần cập nhật
        Product product = productRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));

        // Nếu có file mới, upload và cập nhật ảnh, ngược lại giữ lại ảnh cũ
        if (file != null && !file.isEmpty()) {
            String newImageUrl = uploadFile(file);
            product.setImageUrl(newImageUrl);
        } else {
            product.setImageUrl(dto.getOldImg());
        }

        // Cập nhật các thông tin khác
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setDiscount(dto.getDiscount());

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại"));
        product.setCategory(category);

        // Xử lý cập nhật variant:
        List<VariantDTO> variantDTOs = dto.getVariants();
        if (variantDTOs != null) {
            for (VariantDTO vDto : variantDTOs) {
                if (vDto.getId() != null) {
                    // Variant đã có: update trường
                    ProductVariant variant = product.getVariants().stream()
                            .filter(v -> v.getId().equals(vDto.getId()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Variant không tồn tại, id: " + vDto.getId()));
                    variant.setStock(vDto.getStock());

                    Size size = sizeRepository.findByValue(vDto.getSize());
                    if (size == null) {
                        size = new Size();
                        size.setValue(vDto.getSize());
                        size = sizeRepository.save(size);
                    }
                    variant.setSize(size);

                    Color color = colorRepository.findByName(vDto.getColor());
                    if (color == null) {
                        color = new Color();
                        color.setName(vDto.getColor());
                        color = colorRepository.save(color);
                    }
                    variant.setColor(color);

                    productVariantRepository.save(variant);
                } else {
                    // Variant mới: tạo mới
                    ProductVariant newVariant = new ProductVariant();
                    newVariant.setStock(vDto.getStock());
                    newVariant.setProduct(product);

                    Size size = sizeRepository.findByValue(vDto.getSize());
                    if (size == null) {
                        size = new Size();
                        size.setValue(vDto.getSize());
                        size = sizeRepository.save(size);
                    }
                    newVariant.setSize(size);

                    Color color = colorRepository.findByName(vDto.getColor());
                    if (color == null) {
                        color = new Color();
                        color.setName(vDto.getColor());
                        color = colorRepository.save(color);
                    }
                    newVariant.setColor(color);

                    productVariantRepository.save(newVariant);
                    product.getVariants().add(newVariant);
                }
            }
        }

        return productRepository.save(product);
    }
    public Product findById(Long id) {
        return productRepository.findById(id).orElse(null);
    }


    private String uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Không có file để upload");
        }
        File uploadFolder = new File(uploadDir);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFilename;
        File destFile = new File(uploadFolder, uniqueFileName);
        file.transferTo(destFile);
        return "/uploads/images/" + uniqueFileName;
    }
}