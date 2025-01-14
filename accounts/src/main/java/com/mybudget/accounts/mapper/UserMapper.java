package com.mybudget.accounts.mapper;

import com.mybudget.accounts.dto.UserRegisterDto;
import com.mybudget.accounts.entity.User;

public class UserMapper {
    public static User mapToUser(UserRegisterDto userRegisterDto, User user) {
        user.setFirstName(userRegisterDto.getName());
        user.setEmail(userRegisterDto.getEmail());
        return user;
    }

    public static UserRegisterDto mapToUserDto(User user, UserRegisterDto userRegisterDto) {
        userRegisterDto.setName(user.getFirstName());
        userRegisterDto.setEmail(user.getEmail());
        return userRegisterDto;
    }
}
