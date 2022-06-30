package org.iii.esd.server.controllers.rest.taipower;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static org.iii.esd.api.constant.ApiConstant.KEY_VARIABLE_KNOCK;
import static org.iii.esd.api.constant.ApiConstant.URL_ASP3_ECHO;

@RestController
public class EchoController {

    @GetMapping(URL_ASP3_ECHO)
    public Echo knock(@PathVariable(KEY_VARIABLE_KNOCK) String knock) {
        return new Echo(knock);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Echo {
        private String echo;
    }
}
