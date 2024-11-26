package com.mybudget.accounts.service;

import com.mybudget.accounts.dto.UserDto;
import com.mybudget.accounts.dto.UserRegisterDto;

public interface UserService {
    void createUser(UserRegisterDto userRegisterDto);
    UserDto getUser(String mobileNumber);
    boolean deleteUser(Long userId);
    boolean updateUser(UserRegisterDto userRegisterDto);
}
