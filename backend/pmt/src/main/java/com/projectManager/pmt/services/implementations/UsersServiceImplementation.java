package com.projectManager.pmt.services.implementations;

import com.projectManager.pmt.models.Users;
import com.projectManager.pmt.repositories.UsersRepository;
import com.projectManager.pmt.services.UsersService;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UsersServiceImplementation implements UsersService {

    @Autowired
    private UsersRepository usersRepository;

    @Override
    public List<Users> findAll() {

        return usersRepository.findAll();
    }
}
