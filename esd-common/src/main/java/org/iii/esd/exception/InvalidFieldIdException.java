package org.iii.esd.exception;

public class InvalidFieldIdException extends IiiException {

    private static final long serialVersionUID = -343961540955107843L;

    public InvalidFieldIdException() {
        super(Error.invalidFieldId);
    }

}
