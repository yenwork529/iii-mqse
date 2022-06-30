package org.iii.esd.server.controllers.rest;

import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import org.iii.esd.api.vo.Payload;
import org.iii.esd.jwt.security.TokenProvider;

import static org.iii.esd.Constants.ROLE_FIELDADMIN;
import static org.iii.esd.Constants.ROLE_FIELDUSER;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SIUSER;
import static org.iii.esd.Constants.ROLE_SYSADMIN;

public abstract class AbstractRestController {

    @Autowired
    private TokenProvider tokenProvider;

    protected Payload getPayload(HttpServletRequest request) {
        return tokenProvider.getPayload(request);
    }

    protected Boolean checkRolePermission(HttpServletRequest request, Long roleId) {
        Payload payload = getPayload(request);
        List<Integer> roles = payload.getRoles();

        if (roleId != null && roleId < Collections.min(roles)) {
            return true;
        }
        return false;
    }

    protected Boolean checkCompanyPermission(HttpServletRequest request, Long companyId) {
        Payload payload = getPayload(request);
        List<Integer> roles = payload.getRoles();
        Long pCompanyId = payload.getSiloCompanyId();

        if (checkSysAdmin(payload)) {
            return false;
        }

        if (roles.contains(Integer.valueOf(ROLE_SIADMIN)) || roles.contains(Integer.valueOf(ROLE_SIUSER))) {
            if (companyId != null && pCompanyId != null && !pCompanyId.equals(companyId)) {
                return true;
            }
        }
        return false;
    }


    protected Boolean checkFieldPermission(HttpServletRequest request, Long fieldId) {
        Payload payload = getPayload(request);
        List<Integer> roles = payload.getRoles();
        Long pFieldId = payload.getSiloFieldId();

        if (checkSysAdmin(payload)) {
            return false;
        }

        if (roles.contains(Integer.valueOf(ROLE_FIELDADMIN)) || roles.contains(Integer.valueOf(ROLE_FIELDUSER))) {
            if (pFieldId != null && !pFieldId.equals(fieldId)) {
                return true;
            }
        }
        return false;
    }

    protected Boolean checkSrPermission(HttpServletRequest request, Long srId) {
        Payload payload = getPayload(request);
        List<Integer> roles = payload.getRoles();
        Long pSrId = payload.getSrId();

        if (checkSysAdmin(payload)) {
            return false;
        }

        if (roles.contains(Integer.valueOf(ROLE_SIADMIN)) || roles.contains(Integer.valueOf(ROLE_SIUSER))) {
            if (srId != null && pSrId != null && !pSrId.equals(srId)) {
                return true;
            }
        }
        return false;
    }

    protected Boolean checkSysAdmin(Payload payload) {
        return payload.getRoles().contains(Integer.valueOf(ROLE_SYSADMIN));
    }

}