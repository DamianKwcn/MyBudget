package com.mybudget.accounts.service;

import com.mybudget.accounts.dto.UserRegisterDto;

public interface UserService {
    void createUser(UserRegisterDto userRegisterDto);
    boolean deleteUser(Long userId);
    boolean updateUser(UserRegisterDto userRegisterDto);
}
