package com.projectManager.pmt.repositories;

import com.projectManager.pmt.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    @Query("""
    SELECT DISTINCT r.project
    FROM Role r
    WHERE r.user.email = :email
""")
    List<Project> findProjectsByUserEmail(@Param("email") String email);
}
