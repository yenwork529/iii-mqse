package org.iii.esd.server.controllers.rest.esd.system;

import javax.annotation.security.RolesAllowed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.iii.esd.api.RestConstants;
import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.DataResponse;

import static org.iii.esd.Constants.ROLE_AFCADMIN;
import static org.iii.esd.Constants.ROLE_AFCUSER;
import static org.iii.esd.Constants.ROLE_FIELDADMIN;
import static org.iii.esd.Constants.ROLE_FIELDUSER;
import static org.iii.esd.Constants.ROLE_QSEADMIN;
import static org.iii.esd.Constants.ROLE_QSEUSER;
import static org.iii.esd.Constants.ROLE_QSE_AFC_ADMIN;
import static org.iii.esd.Constants.ROLE_QSE_AFC_USER;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SIUSER;
import static org.iii.esd.Constants.ROLE_SYSADMIN;

@RestController
public class SystemEchoController {

    @GetMapping(RestConstants.REST_SYSTEM_WHOAMI)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSEUSER, ROLE_QSEADMIN,
            ROLE_FIELDADMIN, ROLE_FIELDUSER,
            ROLE_AFCADMIN, ROLE_AFCUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    public ApiResponse whoami(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        return new DataResponse<>(email);
    }

    @PostMapping(RestConstants.REST_SYSTEM_ECHO)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSEUSER, ROLE_QSEADMIN,
            ROLE_FIELDADMIN, ROLE_FIELDUSER,
            ROLE_AFCADMIN, ROLE_AFCUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    public ApiResponse whoami(@RequestBody Echo echo) {
        return new DataResponse<>(Echo.builder()
                                      .echo(echo.msg)
                                      .build());
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Echo {
        private String msg;
        private String echo;
    }
}
