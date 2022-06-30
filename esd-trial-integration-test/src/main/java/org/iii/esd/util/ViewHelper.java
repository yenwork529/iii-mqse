package org.iii.esd.util;

import java.net.URI;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.SuccessfulResponse;

@Log4j2
public final class ViewHelper {
    private ViewHelper() {}

    public static URI buildUrl(String serverUrl, String operation, String qseId, String tgId) {
        return UriComponentsBuilder.fromUriString("http://" + serverUrl + operation)
                                   .build(qseId, tgId);
    }

    public static ApiResponse post(URI uri, Object request) {
        WebClient client = WebClient.create();
        return client.post()
                     .uri(uri)
                     .contentType(MediaType.APPLICATION_JSON)
                     .accept(MediaType.APPLICATION_JSON)
                     .body(BodyInserters.fromObject(request))
                     .retrieve()
                     .bodyToMono(ObjectNode.class)
                     .map(ViewHelper::parseResponse)
                     .block();
    }

    private static ApiResponse parseResponse(ObjectNode json) {
        log.info("json response: {}", json.toString());

        String status = json.get("status").textValue();

        if (status.equals("ok")) {
            return SuccessfulResponse.getInstance();
        } else {
            int code = json.get("err")
                           .get("code")
                           .intValue();
            return ErrorResponse.getInstance(code);
        }
    }
}
