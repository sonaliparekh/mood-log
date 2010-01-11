package com.stuffthathappens.moodlog;

public class StorageException extends Exception {
    private static final long serialVersionUID = 1L;

    public StorageException(String msg) {
        super(msg);
    }

    public StorageException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
