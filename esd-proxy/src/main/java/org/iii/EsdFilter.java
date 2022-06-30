package org.iii;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import nginx.clojure.java.NginxJavaHeaderFilter;
import nginx.clojure.java.NginxJavaRequest;

import static nginx.clojure.java.Constants.*;


public class EsdFilter implements NginxJavaHeaderFilter {

    @Override
    public Object[] doFilter(int status, Map<String, Object> request, Map<String, Object> responseHeaders) throws IOException {
        NginxJavaRequest req = ((NginxJavaRequest) request);
        Object token = req.get("Authorization");

        if(Objects.isNull(token)){
            System.out.println("token is null in EsdFilter");
        }else {
            System.out.println("token is " + token.toString() + " in EsdFilter");
        }

        return PHASE_DONE;

    }

    @Override
    public String[] headersNeedPrefetch() {
        return NginxJavaHeaderFilter.super.headersNeedPrefetch();
    }

    @Override
    public String[] variablesNeedPrefetch() {
        return NginxJavaHeaderFilter.super.variablesNeedPrefetch();
    }

    @Override
    public String[] responseHeadersNeedPrefetch() {
        return NginxJavaHeaderFilter.super.responseHeadersNeedPrefetch();
    }
}
