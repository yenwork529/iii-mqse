package org.iii.esd.api.response;

import java.util.List;
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
public class ListResponse<T> extends SuccessfulResponse {

    private List<T> list;

    private Map<String, Object> meta;

    public ListResponse(List<T> list) {
        this.list = list;
    }

}
