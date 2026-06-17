package com.ecommerce.service.impl;

import com.ecommerce.dto.request.CategoryRequest;
import com.ecommerce.dto.response.CategoryVO;
import com.ecommerce.entity.Category;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.mapper.CategoryMapper;
import com.ecommerce.service.CategoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ecommerce.utils.RedisConstants.*;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public List<CategoryVO> listAsTree() {
        // 1. 查 Redis 缓存
        String cached = stringRedisTemplate.opsForValue().get(CACHE_CATEGORY_TREE_KEY);
        if (cached != null) {
            try {
                return MAPPER.readValue(cached,
                        MAPPER.getTypeFactory().constructCollectionType(List.class, CategoryVO.class));
            } catch (JsonProcessingException e) {
                // 反序列化失败，忽略
            }
        }

        // 2. 查 DB 并构建树
        List<Category> all = categoryMapper.selectAllActive();
        Map<Long, CategoryVO> map = all.stream()
                .map(CategoryVO::fromEntity)
                .collect(Collectors.toMap(CategoryVO::getId, vo -> vo));

        List<CategoryVO> roots = new ArrayList<>();
        for (CategoryVO vo : map.values()) {
            if (vo.getParentId() == null || vo.getParentId() == 0) {
                roots.add(vo);
            } else {
                CategoryVO parent = map.get(vo.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                }
            }
        }

        // 3. 写入 Redis（1 小时长缓存，增删改时会主动清理）
        try {
            stringRedisTemplate.opsForValue().set(
                    CACHE_CATEGORY_TREE_KEY, MAPPER.writeValueAsString(roots),
                    CACHE_CATEGORY_TTL, TimeUnit.MINUTES);
        } catch (JsonProcessingException ignored) {}

        return roots;
    }

    @Override
    public Category getById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }
        return category;
    }

    @Override
    public Category create(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        category.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        categoryMapper.insert(category);
        // 分类变了，清树缓存
        stringRedisTemplate.delete(CACHE_CATEGORY_TREE_KEY);
        return category;
    }

    @Override
    public void update(Long id, CategoryRequest request) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }
        if (request.getName() != null) category.setName(request.getName());
        if (request.getParentId() != null) category.setParentId(request.getParentId());
        if (request.getSortOrder() != null) category.setSortOrder(request.getSortOrder());
        if (request.getStatus() != null) category.setStatus(request.getStatus());
        categoryMapper.update(category);
        // 分类变了，清树缓存
        stringRedisTemplate.delete(CACHE_CATEGORY_TREE_KEY);
    }

    @Override
    public void delete(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }
        if (categoryMapper.countProductsByCategory(id) > 0) {
            throw new BusinessException("该分类下还有商品，无法删除");
        }
        categoryMapper.delete(id);
        // 分类变了，清树缓存
        stringRedisTemplate.delete(CACHE_CATEGORY_TREE_KEY);
    }
}
