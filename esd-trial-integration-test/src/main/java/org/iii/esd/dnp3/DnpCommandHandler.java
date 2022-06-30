package org.iii.esd.dnp3;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import org.iii.esd.api.request.trial.DnpAoRequest;
import org.iii.esd.api.request.trial.DnpDiRequest;
import org.iii.esd.api.request.trial.DnpDoRequest;
import org.iii.esd.api.response.SuccessfulResponse;

import static org.iii.esd.api.constant.ApiConstant.KEY_VARIABLE_QSEID;
import static org.iii.esd.api.constant.ApiConstant.KEY_VARIABLE_TGID;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;

@Component
@Log4j2
public class DnpCommandHandler {
    public Mono<ServerResponse> handleAnalogInputCommand(ServerRequest request) {
        final long tgId = Long.parseLong(request.pathVariable(KEY_VARIABLE_TGID));
        final long qseId = Long.parseLong(request.pathVariable(KEY_VARIABLE_QSEID));
        return request.bodyToMono(ObjectNode.class)
                      .flatMap(cmd -> {
                          log.info("to txg {} of qse {} with {}", tgId, qseId, cmd.toString());
                          return ServerResponse.ok()
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .body(fromObject(SuccessfulResponse.getInstance()));
                      });
    }

    public Mono<ServerResponse> handleAnalogOutputCommand(ServerRequest request) {
        final long tgId = Long.parseLong(request.pathVariable(KEY_VARIABLE_TGID));
        final long qseId = Long.parseLong(request.pathVariable(KEY_VARIABLE_QSEID));
        return request.bodyToMono(DnpAoRequest.class)
                      .flatMap(cmd -> {
                          log.info("to txg {} of qse {} with {}", tgId, qseId, cmd.toString());
                          return ServerResponse.ok()
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .body(fromObject(SuccessfulResponse.getInstance()));
                      });
    }

    public Mono<ServerResponse> handleBinaryInputCommand(ServerRequest request) {
        final long tgId = Long.parseLong(request.pathVariable(KEY_VARIABLE_TGID));
        final long qseId = Long.parseLong(request.pathVariable(KEY_VARIABLE_QSEID));
        return request.bodyToMono(DnpDiRequest.class)
                      .flatMap(cmd -> {
                          log.info("to txg {} of qse {} with {}", tgId, qseId, cmd.toString());
                          return ServerResponse.ok()
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .body(fromObject(SuccessfulResponse.getInstance()));
                      });
    }

    public Mono<ServerResponse> handleBinaryOutputCommand(ServerRequest request) {
        final long tgId = Long.parseLong(request.pathVariable(KEY_VARIABLE_TGID));
        final long qseId = Long.parseLong(request.pathVariable(KEY_VARIABLE_QSEID));
        return request.bodyToMono(DnpDoRequest.class)
                      .flatMap(cmd -> {
                          log.info("to txg {} of qse {} with {}", tgId, qseId, cmd.toString());
                          return ServerResponse.ok()
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .body(fromObject(SuccessfulResponse.getInstance()));
                      });
    }
}
