package org.iii.esd.exception;

public class DeviceException extends IiiException {

    private static final long serialVersionUID = 384861172949912174L;

    public DeviceException(Error error) {
        super(error);
    }

}