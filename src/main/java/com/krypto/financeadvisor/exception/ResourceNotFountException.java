package com.krypto.financeadvisor.exception;

public class ResourceNotFountException extends RuntimeException {
    public ResourceNotFountException(String message) {
        super(message);
    }

    public static ResourceNotFountException of(String entity, Long id){
        return new ResourceNotFountException(entity + " not found with id: " + id);
    }
}
