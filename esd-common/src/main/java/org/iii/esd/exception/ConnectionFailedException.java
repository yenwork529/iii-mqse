package org.iii.esd.exception;

public class ConnectionFailedException extends IiiException {

    private static final long serialVersionUID = -305021100282569161L;

    public ConnectionFailedException(Error error) {
        super(error);
    }
}
