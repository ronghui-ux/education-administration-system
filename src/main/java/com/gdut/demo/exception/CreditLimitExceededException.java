package com.gdut.demo.exception;

/**
 * 当选课导致超过学分上限时抛出。
 */
public class CreditLimitExceededException extends RuntimeException {
    public CreditLimitExceededException() { super(); }
    public CreditLimitExceededException(String message) { super(message); }
    public CreditLimitExceededException(String message, Throwable cause) { super(message, cause); }
}
