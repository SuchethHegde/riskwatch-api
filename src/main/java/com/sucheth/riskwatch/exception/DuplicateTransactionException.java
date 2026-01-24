package com.sucheth.riskwatch.exception;

public class DuplicateTransactionException extends RuntimeException {
    
    public DuplicateTransactionException(String transactionId) {
        super(String.format("Transaction with ID '%s' already exists", transactionId));
    }
}
