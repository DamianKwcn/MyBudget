package com.mybudget.transactions.repository;

import com.mybudget.transactions.entity.Category;
import com.mybudget.transactions.entity.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByKeycloakSubAndTransactionType(String keycloakSub, TransactionType transactionType);

    List<Category> findByKeycloakSub(String keycloakSub);

    Optional<Category> findByIdAndKeycloakSub(Long id, String keycloakSub);

    boolean existsByKeycloakSubAndCategoryNameIgnoreCase(String keycloakSub, String categoryName);

    void deleteAllByUsername(String username);

}
