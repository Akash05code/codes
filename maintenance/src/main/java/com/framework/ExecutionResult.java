package com.framework;

public class ExecutionResult {

    private final String provider;
    private final String currency;
    private final String status;
    private final int attempt;
    private final String message;

    public ExecutionResult(
            String provider,
            String currency,
            String status,
            int attempt,
            String message) {

        this.provider = provider;
        this.currency = currency;
        this.status = status;
        this.attempt = attempt;
        this.message = message;
    }

    public String getProvider() {
        return provider;
    }

    public String getCurrency() {
        return currency;
    }

    public String getStatus() {
        return status;
    }

    public int getAttempt() {
        return attempt;
    }

    public String getMessage() {
        return message;
    }
}