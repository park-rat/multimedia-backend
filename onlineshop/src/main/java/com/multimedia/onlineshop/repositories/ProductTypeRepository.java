package com.multimedia.onlineshop.repositories;

import com.multimedia.onlineshop.models.ProductType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductTypeRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<ProductType> getAllProductTypes() {
        return jdbcTemplate.query("select id, name from product_types order by name asc",
                BeanPropertyRowMapper.newInstance(ProductType.class));
    }
}
