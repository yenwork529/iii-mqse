package org.iii.esd.api.response;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataResponse<T> extends SuccessfulResponse {

    private T data;

    private Map<String, Object> meta;

    public DataResponse(T data) {
        this.data = data;
    }
}