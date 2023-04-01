package com.liuche.exception;

public class TaskNotExistException extends RuntimeException{
    private static final long serialVersionUID = 1463995109499516408L;

    public TaskNotExistException(final String errorMessage, final Object... args) {
        super(String.format(errorMessage, args));
    }
    public TaskNotExistException(final Throwable cause) {
        super(cause);
    }
    

}
