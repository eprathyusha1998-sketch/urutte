package com.urutte.repository;

import com.urutte.model.AiAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiAdminRepository extends JpaRepository<AiAdmin, Long> {
    
    Optional<AiAdmin> findByUsername(String username);
    
    Optional<AiAdmin> findByEmail(String email);
    
    Optional<AiAdmin> findByName(String name);
    
    @Query("SELECT a FROM AiAdmin a WHERE a.isActive = true")
    Optional<AiAdmin> findActiveAiAdmin();
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}
