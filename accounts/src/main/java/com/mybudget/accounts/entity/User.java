package com.mybudget.accounts.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @ToString @AllArgsConstructor @NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keycloak_sub", nullable = false, unique = true)
    private String keycloakSub;

    @Column(name = "given_name")
    private String firstName;

    @Column(name = "family_name")
    private String lastName;

    @Column(name = "preferred_username")
    private String username;

    private String email;
}
