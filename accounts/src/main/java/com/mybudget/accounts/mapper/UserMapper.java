package com.mybudget.accounts.mapper;

import com.mybudget.accounts.dto.UserDto;
import com.mybudget.accounts.entity.User;

public class UserMapper {
    public static UserDto mapToUserDto(User user, UserDto userDto) {
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setBalance(user.getBalance());
        return userDto;
    }
}
