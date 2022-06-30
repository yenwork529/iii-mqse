package org.iii.esd.exception;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import lombok.Getter;

import org.iii.esd.api.response.ErrorResponse;

public class ApplicationException extends RuntimeException {
    @Getter
    private Error error;

    @Getter
    private List<Object> params;

    public ApplicationException(Error error) {
        super();
        this.error = error;
        this.params = Collections.emptyList();
    }

    public ApplicationException(Error error, Object... params) {
        super();
        this.error = error;
        this.params = Arrays.asList(params);
    }

    public static Supplier<ApplicationException> ofNoData(String id) {
        return () -> new ApplicationException(Error.noData, id);
    }

    public static Supplier<ApplicationException> ofNoData(Long id) {
        return () -> new ApplicationException(Error.noData, id);
    }

    public static Supplier<ApplicationException> of(Error error, Object... params) {
        return () -> new ApplicationException(error, params);
    }

    public ErrorResponse getResponse() {
        return new ErrorResponse(this.error, params.toArray());
    }
}
