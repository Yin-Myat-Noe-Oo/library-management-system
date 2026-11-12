package com.library.exception;

/** Thrown when an operation breaks a library rule (e.g. no copies left, loan limit reached). */
public class LibraryException extends RuntimeException {

    public LibraryException(String message) {
        super(message);
    }
}
