package com.mybudget.accounts.listener.Event;

public interface EventHandler<E> {
    void handle(E event);
}
