package com.mybudget.transactions.repository;

import com.mybudget.transactions.entity.Category;
import com.mybudget.transactions.entity.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByKeycloakSubAndTransactionType(String keycloakSub, TransactionType transactionType);
    List<Category> findAllByKeycloakSub(String keycloakSub);
    Optional<Category> findByIdAndKeycloakSub(Long id, String keycloakSub);
    boolean existsByKeycloakSubAndCategoryNameIgnoreCase(String keycloakSub, String categoryName);
    void deleteAllByUsername(String username);
}
