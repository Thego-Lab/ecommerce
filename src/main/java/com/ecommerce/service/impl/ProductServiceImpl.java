package com.ecommerce.service.impl;

import com.ecommerce.dto.request.ProductQueryRequest;
import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.PageResult;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.ecommerce.utils.RedisConstants.*;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ==================== 分页查询 ====================

    @Override
    public PageResult<Product> page(ProductQueryRequest request) {
        int page = request.getPage() != null ? request.getPage() : 1;
        int size = request.getSize() != null ? request.getSize() : 10;
        int offset = (page - 1) * size;

        // 构建分页缓存 key
        String kw = request.getKeyword() != null ? request.getKeyword() : "";
        String cid = request.getCategoryId() != null ? request.getCategoryId().toString() : "";
        String pageKey = CACHE_PRODUCT_PAGE_KEY + kw + ":" + cid + ":" + page + ":" + size;

        // 1. 查 Redis 分页缓存
        String cached = stringRedisTemplate.opsForValue().get(pageKey);
        if (cached != null) {
            try {
                @SuppressWarnings("unchecked")
                PageResult<Product> result = MAPPER.readValue(cached, PageResult.class);
                return result;
            } catch (JsonProcessingException e) {
                // 反序列化失败，忽略缓存
            }
        }

        // 2. 查 DB
        List<Product> list = productMapper.selectPage(request.getKeyword(), request.getCategoryId(), offset, size);
        long total = productMapper.count(request.getKeyword(), request.getCategoryId());
        PageResult<Product> result = new PageResult<>(total, page, size, list);

        // 3. 写入 Redis
        try {
            stringRedisTemplate.opsForValue().set(
                    pageKey, MAPPER.writeValueAsString(result),
                    CACHE_PRODUCT_PAGE_TTL, TimeUnit.MINUTES);
        } catch (JsonProcessingException ignored) {}

        return result;
    }

    // ==================== 详情查询 ====================

    @Override
    public Product getById(Long id) {
        String key = CACHE_PRODUCT_KEY + id;

        // 1. 查 Redis 缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json != null) {
            if (json.isEmpty()) {
                // 空值缓存（防穿透），说明该商品不存在
                throw new BusinessException(404, "商品不存在");
            }
            try {
                return MAPPER.readValue(json, Product.class);
            } catch (JsonProcessingException e) {
                // 反序列化失败，忽略缓存
            }
        }

        // 2. 查 DB
        Product product = productMapper.selectById(id);
        if (product == null) {
            // 缓存空值，防止缓存穿透（短过期，2分钟）
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            throw new BusinessException(404, "商品不存在");
        }

        // 3. 写入 Redis
        try {
            stringRedisTemplate.opsForValue().set(key, MAPPER.writeValueAsString(product),
                    CACHE_PRODUCT_TTL, TimeUnit.MINUTES);
        } catch (JsonProcessingException ignored) {}

        return product;
    }

    // ==================== 增删改 + 缓存清理 ====================

    @Override
    public Product create(ProductRequest request) {
        Product product = new Product();
        product.setCategoryId(request.getCategoryId());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setImage(request.getImage());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock() != null ? request.getStock() : 0);
        product.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        productMapper.insert(product);
        // 新商品写入 Redis
        cacheProduct(product);
        // 清理分页缓存（商品数量变了）
        clearPageCache();
        return product;
    }

    @Override
    public void update(Long id, ProductRequest request) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (request.getCategoryId() != null) product.setCategoryId(request.getCategoryId());
        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getImage() != null) product.setImage(request.getImage());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getStock() != null) product.setStock(request.getStock());
        if (request.getStatus() != null) product.setStatus(request.getStatus());
        productMapper.update(product);
        // 更新 Redis + 清理分页缓存
        cacheProduct(product);
        clearPageCache();
    }

    @Override
    public void delete(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        productMapper.softDelete(id);
        // 删除商品缓存 + 清理分页缓存
        stringRedisTemplate.delete(CACHE_PRODUCT_KEY + id);
        clearPageCache();
    }

    // ==================== 缓存辅助方法 ====================

    private void cacheProduct(Product product) {
        try {
            String key = CACHE_PRODUCT_KEY + product.getId();
            stringRedisTemplate.opsForValue().set(key, MAPPER.writeValueAsString(product),
                    CACHE_PRODUCT_TTL, TimeUnit.MINUTES);
        } catch (JsonProcessingException ignored) {}
    }

    /**
     * 清理所有分页缓存（通配符删除）
     * 商品增删改后调用，保证列表数据一致
     */
    private void clearPageCache() {
        Set<String> keys = stringRedisTemplate.keys(CACHE_PRODUCT_PAGE_KEY + "*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }
}
