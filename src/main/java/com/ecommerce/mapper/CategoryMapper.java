package com.ecommerce.mapper;

import com.ecommerce.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {

    @Select("SELECT * FROM category WHERE status = 1 ORDER BY sort_order ASC")
    List<Category> selectAllActive();

    @Select("SELECT * FROM category WHERE id = #{id}")
    Category selectById(Long id);

    @Insert("INSERT INTO category (name, parent_id, sort_order, status) " +
            "VALUES (#{name}, #{parentId}, #{sortOrder}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    @Update("<script>" +
            "UPDATE category " +
            "<set>" +
            "  <if test='name != null'>name = #{name},</if>" +
            "  <if test='parentId != null'>parent_id = #{parentId},</if>" +
            "  <if test='sortOrder != null'>sort_order = #{sortOrder},</if>" +
            "  <if test='status != null'>status = #{status},</if>" +
            "  updated_at = NOW()" +
            "</set>" +
            "WHERE id = #{id}" +
            "</script>")
    int update(Category category);

    @Delete("DELETE FROM category WHERE id = #{id}")
    int delete(Long id);

    @Select("SELECT COUNT(*) FROM product WHERE category_id = #{categoryId} AND status = 1")
    int countProductsByCategory(Long categoryId);
}
