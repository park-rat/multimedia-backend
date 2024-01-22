package com.multimedia.onlineshop.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private int id;
    private String name;
    private String description;
    private String imageUrl;
    private String productType;
    private float price;
    private int quantity;
    private int pagesTotal;
}
