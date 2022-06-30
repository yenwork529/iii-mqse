package org.iii.esd.mongo.client;

public class ServerApi {
    private final String value;

    public ServerApi(String path) {
        this.value = path;
    }

    public static ServerApi Of(String path) {
        return new ServerApi(path);
    }

    @Override
    public String toString() {
        return value;
    }
}
