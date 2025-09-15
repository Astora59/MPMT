package com.projectManager.pmt.services.implementations;

import com.projectManager.pmt.dto.LoginRequest;
import com.projectManager.pmt.dto.LoginResponse;
import com.projectManager.pmt.dto.RegisterRequest;
import com.projectManager.pmt.models.Users;
import com.projectManager.pmt.repositories.UsersRepository;
import com.projectManager.pmt.security.JwtUtil;
import com.projectManager.pmt.services.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;




import java.util.List;
import java.util.Optional;

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

    @Override
    public LoginResponse login(LoginRequest request) {
        Optional<Users> user = usersRepository.findByEmail(request.getEmail());

        if (user.isPresent() && user.get().getPasswordHash().equals(request.getPassword())) {
            // Générer un JWT pour cet utilisateur
            String token = JwtUtil.generateToken(user.get().getEmail());
            return new LoginResponse(token); //générer un token et renvoyer le token, java jwT
        } else {
            return new LoginResponse("Échec de connexion : email ou mot de passe incorrect");
        }
    }


}
