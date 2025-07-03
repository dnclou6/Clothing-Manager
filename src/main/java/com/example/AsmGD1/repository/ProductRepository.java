////package com.example.AsmGD1.repository;
////
////import com.example.AsmGD1.entity.Product;
////import org.springframework.data.jpa.repository.JpaRepository;
////
////import java.util.List;
////
////
////public interface ProductRepository extends JpaRepository<Product, Integer> {
////    List<Product> findTop3ByOrderByIdAsc();
////    List<Product> findByNameContainingIgnoreCase(String name);
////}
//package com.example.AsmGD1.repository;
//
//import com.example.AsmGD1.entity.Product;
//import com.example.AsmGD1.entity.ProductDetail;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Map;
//
//@Repository
//public interface ProductRepository extends JpaRepository<Product, Integer> {
//
//    @Query(value = "SELECT p.id, p.name, p.stock_quantity AS stockQuantity " +
//            "FROM products p " +
//            "WHERE p.stock_quantity > 0 " +
//            "ORDER BY stockQuantity DESC",
//            nativeQuery = true)
//    List<Map<String, Object>> getProductsInStock();
//
//
//}
