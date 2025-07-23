package com.back.domain.user.user.dto;

import com.back.domain.user.user.entity.User;

public record UserDto (
    String email,
    String name,
    String passward
) {
    public UserDto (String email, String name, String passward) {
        this.email = email;
        this.name = name;
        this.passward = passward;
    }

    public UserDto (User user){
        this(
                user.getEmail(),
                user.getName(),
                user.getPassword()
        );
    }
}