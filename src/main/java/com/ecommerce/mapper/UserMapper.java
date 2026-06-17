package com.ecommerce.mapper;

import com.ecommerce.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE id = #{id}")
    User selectById(Long id);

    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectByUsername(String username);

    @Insert("INSERT INTO user (username, password, nickname, phone, email, avatar) " +
            "VALUES (#{username}, #{password}, #{nickname}, #{phone}, #{email}, #{avatar})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("<script>" +
            "UPDATE user " +
            "<set>" +
            "  <if test='password != null'>password = #{password},</if>" +
            "  <if test='nickname != null'>nickname = #{nickname},</if>" +
            "  <if test='phone != null'>phone = #{phone},</if>" +
            "  <if test='email != null'>email = #{email},</if>" +
            "  <if test='avatar != null'>avatar = #{avatar},</if>" +
            "  updated_at = NOW()" +
            "</set>" +
            "WHERE id = #{id}" +
            "</script>")
    int update(User user);
}
