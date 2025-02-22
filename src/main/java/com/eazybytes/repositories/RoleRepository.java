package com.eazybytes.repositories;

import com.eazybytes.model.AppRole;
import com.eazybytes.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(AppRole appRole);

}

// To avoid NullPointerException problem, Java 8 introduced Optional class.
// Optional<Role> helps avoid NullPointerException when querying the database.
// Encourages functional programming with methods like .orElse(), .ifPresent(), and .map().
// We define a Spring Data JPA repository that returns Optional<Role> instead of null.
//
