package org.iii.esd.tester;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import org.iii.esd.api.response.SuccessfulResponse;

import static org.springframework.web.reactive.function.BodyInserters.fromObject;

@Component
@Log4j2
public class EsdTestHandler {

    @Autowired
    private EsdTestService testService;

    public Mono<ServerResponse> handleAlertCommand(ServerRequest request) {
        return request.bodyToMono(AlertRequest.class)
                      .flatMap(cmd -> {
                          log.info("send alert: {}", cmd);
                          testService.sendAlert(cmd);

                          return ServerResponse.ok()
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .body(fromObject(SuccessfulResponse.getInstance()));
                      });
    }

    public Mono<ServerResponse> handleBeginCommand(ServerRequest request) {
        return request.bodyToMono(BeginRequest.class)
                      .flatMap(cmd -> {
                          log.info("send begin: {}", cmd);
                          testService.sendBegin(cmd);

                          return ServerResponse.ok()
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .body(fromObject(SuccessfulResponse.getInstance()));
                      });
    }

    public Mono<ServerResponse> handleEndCommand(ServerRequest request) {
        return request.bodyToMono(EndRequest.class)
                      .flatMap(cmd -> {
                          log.info("send end: {}", cmd);
                          testService.sendEnd(cmd);

                          return ServerResponse.ok()
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .body(fromObject(SuccessfulResponse.getInstance()));
                      });
    }
}
