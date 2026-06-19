package com.ecommerce.dto;

/**
 * 存储在 Redis 和 ThreadLocal 中的用户信息（脱敏，不存密码）
 */
public class UserDTO {
    private Long id;
    private String nickname;
    private String role;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
