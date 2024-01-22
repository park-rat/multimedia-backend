package com.multimedia.onlineshop.controllers;

import com.multimedia.onlineshop.models.Product;
import com.multimedia.onlineshop.models.ProductSearchData;
import com.multimedia.onlineshop.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @CrossOrigin
    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable int id) {
        Product product = productRepository.getProductById(id);
        if (product != null) {
            return ResponseEntity.ok(product);
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }

    @CrossOrigin
    @PostMapping("/products")
    public ResponseEntity<List<Product>> getProducts(@RequestBody ProductSearchData data) {
        if ("".equals(data.getProductNameSearch())) {
            data.setProductNameSearch(null);
        }
        if (data.getProductTypeIds() != null && data.getProductTypeIds().length == 0) {
            data.setProductTypeIds(null);
        }
        if (!"name".equals(data.getSortBy()) && !"price".equals(data.getSortBy())) {
            data.setSortBy("name");
        }
        if (!"asc".equals(data.getSortOrder()) && !"desc".equals(data.getSortOrder())) {
            data.setSortOrder("asc");
        }
        if (data.getPageNumber() == null || data.getPageNumber() < 1) {
            data.setPageNumber(1);
        }
        if (data.getItemsPerPage() == null || data.getItemsPerPage() < 1) {
            data.setItemsPerPage(6);
        }
        return ResponseEntity.ok(productRepository.getProducts(data));
    }
}
