package com.mybudget.accounts.repository;

import com.mybudget.accounts.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByKeycloakSub(String keycloakSub);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);

    @Transactional
    @Modifying
    void deleteById(Long id);
}
