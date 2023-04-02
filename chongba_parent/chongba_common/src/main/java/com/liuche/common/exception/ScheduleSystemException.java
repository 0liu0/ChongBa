package com.liuche.common.exception;

public class ScheduleSystemException extends RuntimeException{

    private static final long serialVersionUID = -2138421869882902052L;
    public ScheduleSystemException(final String errorMessage, final Object... args) {
        super(String.format(errorMessage, args));
    }
    public ScheduleSystemException(final Throwable cause) {
        super(cause);
    }

}
