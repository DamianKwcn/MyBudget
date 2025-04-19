package com.mybudget.common.kafka;

public final class Topics {

    //TRANSACTIONS
    public static final String STREAMING_TRANSACTIONS_SAGA_STARTED_V1 = "streaming.transactions.saga_started.v1";
    public static final String STREAMING_TRANSACTIONS_CONFIRMED_V1 = "streaming.transactions.confirmed.v1";
    public static final String STREAMING_TRANSACTIONS_ROLLED_BACK_V1 = "streaming.transactions.rolled_back.v1";
    public static final String QUEUING_TRANSACTIONS_DELETE_V1 = "queuing.transactions.delete.v1";

    //ACCOUNTS
    public static final String QUEUING_ACCOUNTS_BALANCE_UPDATE_REQUEST_V1 = "queuing.accounts.balance_update_request.v1";
    public static final String STREAMING_ACCOUNTS_BALANCE_UPDATE_RESULT_V1 = "streaming.accounts.balance_update_result.v1";

    //USERS
    public static final String QUEUING_USERS_DELETE_V1 = "queuing.users.delete.v1";

    //CATEGORIES
    public static final String QUEUING_CATEGORIES_DELETE_V1 = "queuing.categories.delete.v1";

    private Topics() { }
}
