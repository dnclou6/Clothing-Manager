package com.example.AsmGD1.service.admin;

import com.example.AsmGD1.entity.Category;
import com.example.AsmGD1.entity.Product;
import com.example.AsmGD1.entity.ProductDetail;
import com.example.AsmGD1.repository.CategoryRepository;
import com.example.AsmGD1.repository.admin.ProductDetailRepository;
import com.example.AsmGD1.repository.admin.ProductRepository;
import com.example.AsmGD1.util.QRCodeUtil;
import com.google.zxing.WriterException;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductDetailRepository productDetailRepository;

    public List<Product> findAll() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(product -> {
            Hibernate.initialize(product);
            List<ProductDetail> details = productDetailRepository.findByProductId(product.getId());
            int totalStockQuantity = details.stream()
                    .mapToInt(ProductDetail::getStockQuantity)
                    .sum();
            product.setTotalStockQuantity(totalStockQuantity);
            return product;
        }).collect(Collectors.toList());
    }

    public Page<Product> findAllPaginated(Pageable pageable) {
        // Sắp xếp theo createdAt giảm dần (mới nhất lên đầu)
        Pageable sortedByCreatedAtDesc = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> productPage = productRepository.findAll(sortedByCreatedAtDesc);
        productPage.getContent().forEach(product -> {
            List<ProductDetail> details = productDetailRepository.findByProductId(product.getId());
            int totalStockQuantity = details.stream()
                    .mapToInt(ProductDetail::getStockQuantity)
                    .sum();
            product.setTotalStockQuantity(totalStockQuantity);
        });
        return productPage;
    }

    public Product findById(Integer id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product != null) {
            List<ProductDetail> details = productDetailRepository.findByProductId(id);
            int totalStockQuantity = details.stream()
                    .mapToInt(ProductDetail::getStockQuantity)
                    .sum();
            product.setTotalStockQuantity(totalStockQuantity);
        }
        return product;
    }

    public void save(Product product) {
        productRepository.save(product);
    }

    public void deleteById(Integer id) {
        productRepository.deleteById(id);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Product> findByNameContaining(String name) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        products.forEach(product -> {
            List<ProductDetail> details = productDetailRepository.findByProductId(product.getId());
            int totalStockQuantity = details.stream()
                    .mapToInt(ProductDetail::getStockQuantity)
                    .sum();
            product.setTotalStockQuantity(totalStockQuantity);
        });
        return products;
    }

    public List<ProductDetail> getAllProductDetails() {
        List<ProductDetail> details = productDetailRepository.findAll();
        details.forEach(detail -> {
            Hibernate.initialize(detail.getProduct());
            Hibernate.initialize(detail.getColor());
            Hibernate.initialize(detail.getSize());
            Hibernate.initialize(detail.getImages());
        });
        return details;
    }

    public ProductDetail getProductDetailById(Integer id) {
        ProductDetail detail = productDetailRepository.findById(id).orElse(null);
        if (detail != null) {
            Hibernate.initialize(detail.getProduct());
            Hibernate.initialize(detail.getColor());
            Hibernate.initialize(detail.getSize());
            Hibernate.initialize(detail.getImages());
        }
        return detail;
    }

    public void saveProductDetail(ProductDetail productDetail) {
        productDetailRepository.save(productDetail);
    }

    public List<ProductDetail> getProductDetailsByProductId(Integer productId) {
        List<ProductDetail> details = productDetailRepository.findByProductId(productId);
        details.forEach(detail -> {
            Hibernate.initialize(detail.getColor());
            Hibernate.initialize(detail.getSize());
            Hibernate.initialize(detail.getImages());
        });
        return details;
    }

    public ProductDetail getProductDetailByProductIdAndColorIdAndSizeId(Integer productId, Integer colorId, Integer sizeId) {
        ProductDetail detail = productDetailRepository.findByProductIdAndColorIdAndSizeId(productId, colorId, sizeId);
        if (detail != null) {
            Hibernate.initialize(detail.getProduct());
            Hibernate.initialize(detail.getColor());
            Hibernate.initialize(detail.getSize());
            Hibernate.initialize(detail.getImages());
        }
        return detail;
    }

    public List<ProductDetail> getAllProductDetailsWithQR() {
        List<ProductDetail> list = productDetailRepository.findAll();
        for (ProductDetail pd : list) {
            try {
                String qrDir = "src/main/resources/static/qrcodes/";
                new File(qrDir).mkdirs();
                String path = qrDir + "qr_" + pd.getId() + ".png";
                QRCodeUtil.generateQRCodeImage(String.valueOf(pd.getId()), 200, 200, path);
            } catch (WriterException | IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    // New method for search by name and filter by category
    public Page<Product> findByNameContaining(String searchName, Integer categoryId, Pageable pageable) {
        Page<Product> productPage;
        if (categoryId != null) {
            productPage = productRepository.findByNameContainingIgnoreCaseAndCategoryId(searchName, categoryId, pageable);
        } else {
            productPage = productRepository.findByNameContainingIgnoreCase(searchName, pageable);
        }
        productPage.getContent().forEach(product -> {
            List<ProductDetail> details = productDetailRepository.findByProductId(product.getId());
            int totalStockQuantity = details.stream()
                    .mapToInt(ProductDetail::getStockQuantity)
                    .sum();
            product.setTotalStockQuantity(totalStockQuantity);
        });
        return productPage;
    }

    // Method for filter by category
    public Page<Product> findByCategoryId(Integer categoryId, Pageable pageable) {
        Page<Product> productPage = productRepository.findByCategoryId(categoryId, pageable);
        productPage.getContent().forEach(product -> {
            List<ProductDetail> details = productDetailRepository.findByProductId(product.getId());
            int totalStockQuantity = details.stream()
                    .mapToInt(ProductDetail::getStockQuantity)
                    .sum();
            product.setTotalStockQuantity(totalStockQuantity);
        });
        return productPage;
    }
}