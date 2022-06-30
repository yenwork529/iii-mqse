package org.iii;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nginx.clojure.java.NginxJavaRequest;
import nginx.clojure.java.NginxJavaRingHandler;

import static nginx.clojure.java.Constants.HEADERS;
import static nginx.clojure.java.Constants.PHASE_DONE;

public class EsdProxy implements NginxJavaRingHandler {
    @Override
    public Object[] invoke(Map<String, Object> request) throws IOException {
        NginxJavaRequest req = ((NginxJavaRequest) request);
        Map headers = (Map) req.get(HEADERS);
        String token = (String) headers.get("Authorization");

        if (Objects.isNull(token)) {
            System.out.println("token is null in EsdProxy");
        }

        System.out.println("token is " + token + " in EsdProxy");
        String[] jwtTokens = token.split("\\.");
        String meta = jwtTokens[1];

        ObjectMapper mapper = new ObjectMapper();
        JsonNode metaNode = mapper.readTree(meta);
        Integer qseCode = metaNode.get("qseCode").intValue();

        req.setVariable("proxy", String.format("/%d", qseCode));

        return PHASE_DONE;
    }

    @Override
    public String[] headersNeedPrefetch() {
        return NginxJavaRingHandler.super.headersNeedPrefetch();
    }

    @Override
    public String[] variablesNeedPrefetch() {
        return NginxJavaRingHandler.super.variablesNeedPrefetch();
    }

    @Override
    public String[] responseHeadersNeedPrefetch() {
        return NginxJavaRingHandler.super.responseHeadersNeedPrefetch();
    }
}
