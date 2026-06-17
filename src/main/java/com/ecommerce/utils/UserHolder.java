package com.ecommerce.utils;

import com.ecommerce.dto.UserDTO;

/**
 * ThreadLocal 用户上下文，同一请求链路中随时取用，无需传参
 */
public class UserHolder {
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void saveUser(UserDTO user) { tl.set(user); }

    public static UserDTO getUser() { return tl.get(); }

    public static Long getUserId() {
        UserDTO user = tl.get();
        return user != null ? user.getId() : null;
    }

    public static void removeUser() { tl.remove(); }
}
