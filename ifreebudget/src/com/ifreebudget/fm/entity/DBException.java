package com.ifreebudget.fm.entity;

public class DBException extends Exception {
    private static final long serialVersionUID = 1L;

    public DBException(String message) {
        super(message);
    }

    public DBException(Throwable t) {
        super(t);
    }
}
