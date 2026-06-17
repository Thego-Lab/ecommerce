package com.ecommerce.mapper;

import com.ecommerce.entity.Product;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductMapper {

    // ===== 简单 SQL：注解 =====

    @Select("SELECT p.*, c.name AS category_name FROM product p " +
            "LEFT JOIN category c ON p.category_id = c.id WHERE p.id = #{id}")
    Product selectById(Long id);

    @Insert("INSERT INTO product (category_id, name, description, image, price, stock, status) " +
            "VALUES (#{categoryId}, #{name}, #{description}, #{image}, #{price}, #{stock}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    @Update("<script>" +
            "UPDATE product " +
            "<set>" +
            "  <if test='categoryId != null'>category_id = #{categoryId},</if>" +
            "  <if test='name != null'>name = #{name},</if>" +
            "  <if test='description != null'>description = #{description},</if>" +
            "  <if test='image != null'>image = #{image},</if>" +
            "  <if test='price != null'>price = #{price},</if>" +
            "  <if test='stock != null'>stock = #{stock},</if>" +
            "  <if test='status != null'>status = #{status},</if>" +
            "  updated_at = NOW()" +
            "</set>" +
            "WHERE id = #{id}" +
            "</script>")
    int update(Product product);

    @Update("UPDATE product SET status = 0, updated_at = NOW() WHERE id = #{id}")
    int softDelete(Long id);

    @Update("UPDATE product SET stock = stock - #{quantity}, updated_at = NOW() " +
            "WHERE id = #{id} AND stock >= #{quantity}")
    int deductStock(@Param("id") Long id, @Param("quantity") int quantity);

    // ===== 复杂 SQL：XML =====

    List<Product> selectPage(@Param("keyword") String keyword,
                             @Param("categoryId") Long categoryId,
                             @Param("offset") int offset,
                             @Param("size") int size);

    long count(@Param("keyword") String keyword, @Param("categoryId") Long categoryId);
}
