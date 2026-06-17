package com.ecommerce.service;

import com.ecommerce.dto.request.CategoryRequest;
import com.ecommerce.dto.response.CategoryVO;
import com.ecommerce.entity.Category;

import java.util.List;

public interface CategoryService {
    List<CategoryVO> listAsTree();
    Category getById(Long id);
    Category create(CategoryRequest request);
    void update(Long id, CategoryRequest request);
    void delete(Long id);
}
