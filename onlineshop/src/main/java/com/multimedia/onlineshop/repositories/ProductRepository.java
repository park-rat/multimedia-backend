package com.multimedia.onlineshop.repositories;

import com.multimedia.onlineshop.models.Product;
import com.multimedia.onlineshop.models.ProductSearchData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ProductRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static Product mapProductRow(ResultSet rs, int rowNum) throws SQLException {
        return new Product(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("image_url"),
            rs.getString("product_type"),
            rs.getFloat("price"),
            rs.getInt("quantity"),
            rs.getInt("pages_total")
        );
    }

    public Product getProductById(int id) {
        String query =
            "select p.id, p.name, p.description, p.image_url, pt.name product_type, p.price, p.quantity, 0 pages_total " +
            "from products p inner join product_types pt on p.product_type_id = pt.id where p.id = ?";
        Product res = null;
        try {
            res = jdbcTemplate.queryForObject(query, ProductRepository::mapProductRow, id);
        }
        catch (DataAccessException e) {}
        return res;
    }

    public List<Product> getProducts(ProductSearchData data) {
        List<Object> args = new ArrayList<>();
        args.add((float) data.getItemsPerPage());
        String query = "select " +
            "p.id, p.name, p.description, p.image_url, pt.name product_type, p.price, p.quantity, " +
            "ceil( count(*) over() / ? ) pages_total " +
            "from products p inner join product_types pt on p.product_type_id = pt.id ";
        String condition = "";
        if (data.getProductNameSearch() != null) {
            condition += "p.name ilike concat('%', ?, '%') and ";
            args.add(data.getProductNameSearch().replace(" ", "%"));
        }
        if (data.getProductTypeIds() != null) {
            condition += "p.product_type_id in (";
            for (Integer i : data.getProductTypeIds()) {
                condition += "?,";
                args.add(i);
            }
            condition = condition.substring(0, condition.length() - 1) + ") and ";
        }
        if (condition.length() != 0) {
            query += "where " + condition.substring(0, condition.length() - 4);
        }
        query += "order by p." + data.getSortBy() + " " + data.getSortOrder() + " limit ? offset ?";
        args.add(data.getItemsPerPage());
        args.add(data.getItemsPerPage() * (data.getPageNumber() - 1));

        return jdbcTemplate.query(query, ProductRepository::mapProductRow, args.toArray());
    }
}
