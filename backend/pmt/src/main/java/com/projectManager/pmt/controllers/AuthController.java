package com.projectManager.pmt.controllers;


import com.projectManager.pmt.dto.RegisterRequest;
import com.projectManager.pmt.models.Users;
import com.projectManager.pmt.services.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UsersService usersService;

    @PostMapping("/register")
    public Users register(@RequestBody RegisterRequest request) {
        return usersService.registerUser(request);
    }
}
