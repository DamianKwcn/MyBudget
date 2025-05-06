package com.mybudget.accounts.repository;

import com.mybudget.accounts.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByKeycloakSub(String keycloakSub);
    Optional<User> findByUsername(String username);
}
