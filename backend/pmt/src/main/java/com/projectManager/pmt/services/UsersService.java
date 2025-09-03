package com.projectManager.pmt.services;

import com.projectManager.pmt.dto.RegisterRequest;
import com.projectManager.pmt.models.Users;

import java.util.List;
import java.util.UUID;

public interface UsersService {

    List<Users> findAll();

    Users registerUser(RegisterRequest request);
}

