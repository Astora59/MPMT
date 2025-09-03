package com.projectManager.pmt.services.implementations;

import com.projectManager.pmt.dto.RegisterRequest;
import com.projectManager.pmt.models.Users;
import com.projectManager.pmt.repositories.UsersRepository;
import com.projectManager.pmt.services.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;




import java.util.List;

@Service
public class UsersServiceImplementation implements UsersService {

    @Autowired
    private UsersRepository usersRepository;

    @Override
    public List<Users> findAll() {

        return usersRepository.findAll();
    }

    @Override
    public Users registerUser(RegisterRequest request) {
        Users user = new Users();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(request.getPassword());



        return usersRepository.save(user);
    }
}
