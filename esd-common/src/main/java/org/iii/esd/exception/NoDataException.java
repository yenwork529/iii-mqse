package org.iii.esd.exception;

public class NoDataException extends RuntimeException {

    private long id;
    private String uuid;

    public NoDataException(long id) {
        super(String.format(Error.noData.getMsg(), id));
        this.id = id;
    }

    public NoDataException(long id, String template) {
        super(String.format(template, id));
        this.id = id;
    }

    public NoDataException(String uuid) {
        super(String.format(Error.noData.getMsg(), uuid));
        this.uuid = uuid;
    }

    public NoDataException(String uuid, String template) {
        super(String.format(template, uuid));
        this.uuid = uuid;
    }

    public long getId() {
        return this.id;
    }

    public String getUuid() {
        return this.uuid;
    }
}
