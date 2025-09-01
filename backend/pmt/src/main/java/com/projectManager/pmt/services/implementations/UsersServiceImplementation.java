package com.projectManager.pmt.services.implementations;

import com.projectManager.pmt.models.Users;
import com.projectManager.pmt.services.UsersService;
import org.apache.catalina.User;

import java.util.List;

public class UsersServiceImplementation implements UsersService {

    @Override
    public List<Users> findAll() {

        return List.of();
    }
}
