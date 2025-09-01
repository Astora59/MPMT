package com.projectManager.pmt.controllers;


import com.projectManager.pmt.models.Users;
import com.projectManager.pmt.services.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/users")
public class UsersController {

    @Autowired
    private UsersService usersService;


    @GetMapping
    public List<Users> findAll() {
        return usersService.findAll();
    }

}
