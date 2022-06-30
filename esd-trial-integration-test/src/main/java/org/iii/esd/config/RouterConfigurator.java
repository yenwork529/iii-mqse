package org.iii.esd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import org.iii.esd.api.constant.ApiConstant;
import org.iii.esd.common.Constant;
import org.iii.esd.dnp3.DnpCommandHandler;
import org.iii.esd.tester.EsdTestHandler;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class RouterConfigurator {

    @Bean
    public RouterFunction<ServerResponse> route(DnpCommandHandler dnp3Handler, EsdTestHandler testHandler) {
        return RouterFunctions
                .route(POST(ApiConstant.URL_ASP4_DO_WITH_TGID)
                        .and(accept(MediaType.APPLICATION_JSON)), dnp3Handler::handleBinaryOutputCommand)
                .andRoute(POST(ApiConstant.URL_ASP4_AO_WITH_TGID)
                        .and(accept(MediaType.APPLICATION_JSON)), dnp3Handler::handleAnalogOutputCommand)
                .andRoute(POST(ApiConstant.URL_ASP4_AI_WITH_TGID)
                        .and(accept(MediaType.APPLICATION_JSON)), dnp3Handler::handleAnalogInputCommand)
                .andRoute(POST(ApiConstant.URL_ASP4_DI_WITH_TGID)
                        .and(accept(MediaType.APPLICATION_JSON)), dnp3Handler::handleBinaryInputCommand)
                .andRoute(POST(Constant.URL_API_COMMAND_ALERT)
                        .and(accept(MediaType.APPLICATION_JSON)), testHandler::handleAlertCommand)
                .andRoute(POST(Constant.URL_API_COMMAND_BEGIN)
                        .and(accept(MediaType.APPLICATION_JSON)), testHandler::handleBeginCommand)
                .andRoute(POST(Constant.URL_API_COMMAND_END)
                .and(accept(MediaType.APPLICATION_JSON)), testHandler::handleEndCommand);

    }

}
