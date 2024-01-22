package com.multimedia.onlineshop.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchData {
    private String productNameSearch;
    private Integer[] productTypeIds;
    private String sortBy;  //name OR price
    private String sortOrder;  //asc OR desc
    private Integer pageNumber;
    private Integer itemsPerPage;
}
