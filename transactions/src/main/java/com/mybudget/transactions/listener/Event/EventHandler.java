package com.mybudget.transactions.listener.Event;

public interface EventHandler<E> {
    void handle(E event);
}
