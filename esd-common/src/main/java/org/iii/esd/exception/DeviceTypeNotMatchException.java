package org.iii.esd.exception;

public class DeviceTypeNotMatchException extends DeviceException {

    private static final long serialVersionUID = 2336173158372547173L;

    public DeviceTypeNotMatchException(Error error) {
        super(error);
    }
}
