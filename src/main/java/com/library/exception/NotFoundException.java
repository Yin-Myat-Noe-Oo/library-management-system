package com.library.exception;

/** Thrown when a requested book, member or loan does not exist. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String entity, Long id) {
        super(entity + " with id " + id + " was not found");
    }
}
