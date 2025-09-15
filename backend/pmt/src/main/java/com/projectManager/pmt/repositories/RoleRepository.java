package com.projectManager.pmt.repositories;

import com.projectManager.pmt.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    // Requête explicite pour éviter les problèmes de nom de propriété
    @Query("SELECT r FROM Role r WHERE r.user.users_id = :userId AND r.project.project_id = :projectId")
    Optional<Role> findRoleByUserAndProject(@Param("userId") UUID userId, @Param("projectId") UUID projectId);
}
