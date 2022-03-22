package com.minerva.MinervaDatabase.database.exceptions;

public class AliasNotFoundException extends Exception{
    public AliasNotFoundException() {
        super("Alias Not Found!");
    }

    public AliasNotFoundException(String message) {
        super(message);
    }

    public AliasNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
