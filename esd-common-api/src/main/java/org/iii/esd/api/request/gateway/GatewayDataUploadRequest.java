package org.iii.esd.api.request.gateway;

import org.iii.esd.api.vo.gateway.Resource;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Getter
@Setter
public class GatewayDataUploadRequest<T> {

	private String kind;
	
	private Resource<T> payload;

    public GatewayDataUploadRequest(String kind){
        this.kind = kind;
    }

}