package com.projectManager.pmt.repositories;

import com.projectManager.pmt.models.Users;

import org.springframework.data.jpa.repository.JpaRepository;


import java.util.UUID;

public interface UsersRepository extends JpaRepository<Users, UUID> {



}
