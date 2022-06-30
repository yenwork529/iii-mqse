package org.iii.esd.exception;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import lombok.Getter;

import org.iii.esd.api.response.ErrorResponse;

@Getter
public class WebException extends Exception {
    private Error error;
    private List<Object> params;

    public WebException(Error error) {
        super();
        this.error = error;
        this.params = Collections.emptyList();
    }

    public WebException(Error error, Object... params) {
        super();
        this.error = error;
        this.params = Arrays.asList(params);
    }

    public static Supplier<WebException> of(Error error, Object... params) {
        return () -> new WebException(error, params);
    }

    public ErrorResponse getResponse() {
        return new ErrorResponse(this.error, params.toArray());
    }
}
