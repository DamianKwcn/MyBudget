package com.mybudget.accounts.common;

public interface CurrentUserProvider {
    String getKeycloakSub();
    String getUsername();
}