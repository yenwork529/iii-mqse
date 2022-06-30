package org.iii.esd.exception;

public class LoadTypeNotMatchException extends IiiException {

    private static final long serialVersionUID = 3425437860843804633L;

    public LoadTypeNotMatchException(Error error) {
        super(error);
    }
}