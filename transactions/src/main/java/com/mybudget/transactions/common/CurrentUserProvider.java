package com.mybudget.transactions.common;

public interface CurrentUserProvider {
    String getKeycloakSub();
    String getUsername();
}