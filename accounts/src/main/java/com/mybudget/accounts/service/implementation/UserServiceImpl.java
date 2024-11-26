package com.mybudget.accounts.service.implementation;

import com.mybudget.accounts.dto.UserRegisterDto;
import com.mybudget.accounts.entity.User;
import com.mybudget.accounts.exception.ResourceNotFoundException;
import com.mybudget.accounts.exception.UserAlreadyExistsException;
import com.mybudget.accounts.mapper.UserMapper;
import com.mybudget.accounts.repository.UserRepository;
import com.mybudget.accounts.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;

    @Override
    public void createUser(UserRegisterDto userRegisterDto) {
        User user = UserMapper.mapToUser(userRegisterDto, new User());
        Optional<User> optionalUser = userRepository.findByEmail(userRegisterDto.getEmail());
        if (optionalUser.isPresent()) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }
        user.setBalance(0L);
        userRepository.save(user);
    }

    @Override
    public boolean deleteUser(Long userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", userId.toString())
        );
        userRepository.deleteByUserId(user.getUserId());
        return true;
    }

    @Override
    public boolean updateUser(UserRegisterDto userRegisterDto) {
        return false;
    }
}
