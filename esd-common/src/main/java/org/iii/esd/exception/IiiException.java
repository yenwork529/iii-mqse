package org.iii.esd.exception;

public class IiiException extends RuntimeException {

    private static final long serialVersionUID = -578532245087676972L;

    private int code;

    public IiiException() {
    }

    public IiiException(Error error) {
        super(error.getMsg());
        this.code = error.getCode();
    }

    public IiiException(String message) {
        super(message);
    }

    public int getCode() {
        return code;
    }

}