package com.mybudget.accounts.mapper;

import com.mybudget.accounts.dto.UserRegisterDto;
import com.mybudget.accounts.entity.User;

public class UserMapper {
    public static User mapToUser(UserRegisterDto userRegisterDto, User user) {
        user.setName(userRegisterDto.getName());
        user.setEmail(userRegisterDto.getEmail());
        user.setPassword(userRegisterDto.getPassword());
        return user;
    }

    public static UserRegisterDto mapToUserDto(User user, UserRegisterDto userRegisterDto) {
        userRegisterDto.setName(user.getName());
        userRegisterDto.setEmail(user.getEmail());
        userRegisterDto.setPassword(user.getPassword());
        return userRegisterDto;
    }
}
