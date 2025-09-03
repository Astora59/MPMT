package com.projectManager.pmt.controllers;


import com.projectManager.pmt.dto.LoginRequest;
import com.projectManager.pmt.dto.LoginResponse;
import com.projectManager.pmt.models.Users;
import com.projectManager.pmt.services.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/auth")
public class UsersController {

    @Autowired
    private UsersService usersService;


    @GetMapping
    public List<Users> findAll() {
        return usersService.findAll();
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return usersService.login(request);
    }

}
