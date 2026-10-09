package com.ecommerce.backend.mapper;

import com.ecommerce.backend.dto.CategoryResponse;
import com.ecommerce.backend.entity.Category;

public class CategoryMapper {

    public static CategoryResponse toResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        return response;
    }
}
