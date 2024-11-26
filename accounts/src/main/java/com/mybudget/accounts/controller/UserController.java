package com.mybudget.accounts.controller;

import com.mybudget.accounts.constants.UserConstants;
import com.mybudget.accounts.dto.ResponseDto;
import com.mybudget.accounts.dto.UserRegisterDto;
import com.mybudget.accounts.service.implementation.UserServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/api", produces = {MediaType.APPLICATION_JSON_VALUE})
@Validated
public class UserController {

    private final UserServiceImpl userService;

    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseDto> createUser(@Valid @RequestBody UserRegisterDto userRegisterDto) {
        userService.createUser(userRegisterDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto(UserConstants.STATUS_201, UserConstants.MESSAGE_201));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ResponseDto> deleteUser(@RequestParam Long userId) {
        boolean isDeleted = userService.deleteUser(userId);
        if (isDeleted) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDto(UserConstants.STATUS_200, UserConstants.MESSAGE_200));
        } else {
             return ResponseEntity
                     .status(HttpStatus.EXPECTATION_FAILED)
                     .body(new ResponseDto(UserConstants.STATUS_417, UserConstants.MESSAGE_417_DELETE));
        }
    }

}
