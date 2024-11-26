package com.mybudget.accounts.mapper;

import com.mybudget.accounts.dto.UserDto;
import com.mybudget.accounts.dto.UserRegisterDto;
import com.mybudget.accounts.entity.User;

public class UserMapper {
    public static User mapToUser(UserRegisterDto userRegisterDto, User user) {
        user.setName(userRegisterDto.getName());
        user.setEmail(userRegisterDto.getEmail());
        user.setPassword(userRegisterDto.getPassword());
        user.setMobileNumber(userRegisterDto.getMobileNumber());
        return user;
    }

    public static UserRegisterDto mapToUserRegisterDto(User user, UserRegisterDto userRegisterDto) {
        userRegisterDto.setName(user.getName());
        userRegisterDto.setEmail(user.getEmail());
        userRegisterDto.setPassword(user.getPassword());
        userRegisterDto.setMobileNumber(user.getMobileNumber());
        return userRegisterDto;
    }

    public static UserDto mapToUserDto(User user, UserDto userDto) {
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        userDto.setBalance(user.getBalance());
        userDto.setMobileNumber(user.getMobileNumber());
        return userDto;
    }
}
