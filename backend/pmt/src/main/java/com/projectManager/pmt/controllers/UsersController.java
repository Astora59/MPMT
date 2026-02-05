package com.projectManager.pmt.controllers;


import com.projectManager.pmt.dto.LoginRequest;
import com.projectManager.pmt.dto.LoginResponse;
import com.projectManager.pmt.models.Users;
import com.projectManager.pmt.services.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:4200")
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
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        try {
            LoginResponse response = usersService.login(request);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

}
