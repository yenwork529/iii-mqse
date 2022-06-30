package org.iii.esd.server.controllers.rest.esd.system;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.security.RolesAllowed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.iii.esd.api.enums.ServiceType;
import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.esd.system.TaipowerListResponse;
import org.iii.esd.api.vo.Name;
import org.iii.esd.exception.Error;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.document.integrate.UserProfile;
import org.iii.esd.mongo.service.integrate.IntegrateRelationService;
import org.iii.esd.mongo.service.integrate.TxgFieldService;
import org.iii.esd.mongo.service.integrate.TxgService;
import org.iii.esd.mongo.service.integrate.UserService;
import org.iii.esd.server.controllers.rest.AbstractRestController;
import org.iii.esd.server.controllers.rest.esd.aaa.AuthorizationHelper;

import static org.iii.esd.Constants.QSE_AUTHOR;
import static org.iii.esd.Constants.ROLE_QSEADMIN;
import static org.iii.esd.Constants.ROLE_QSEUSER;
import static org.iii.esd.Constants.ROLE_QSE_AFC_ADMIN;
import static org.iii.esd.Constants.ROLE_QSE_AFC_USER;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SIUSER;
import static org.iii.esd.Constants.ROLE_SYSADMIN;
import static org.iii.esd.Constants.SYS_AUTHOR;
import static org.iii.esd.Constants.TXG_AUTHOR;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_TAIPOWER_LIST;
import static org.iii.esd.mongo.util.ModelHelper.asNonNull;

@RestController
@Log4j2
@Api(tags = "Taipower",
        description = "台電輔助服務")
public class TaipowerController extends AbstractRestController {

    @Autowired
    private TxgService txgService;
    @Autowired
    private TxgFieldService resService;
    @Autowired
    private UserService userService;
    @Autowired
    private IntegrateRelationService relationService;
    @Autowired
    private AuthorizationHelper authorHelper;

    @GetMapping(REST_SYSTEM_TAIPOWER_LIST)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_QSEADMIN, ROLE_QSEUSER,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "list",
            notes = "台電輔助服務名稱列表")
    public ApiResponse list(Authentication authentication) {
        return getAuthorizedTaipowerList(authentication);
        // return getLegacyTaipowerList();
    }

    private ApiResponse getAuthorizedTaipowerList(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        UserProfile user = userService.findByEmail(email);
        Long author = user.getRoleId();

        if (QSE_AUTHOR.contains(author) || SYS_AUTHOR.contains(author)) {
            return getLegacyTaipowerList();
        } else if (TXG_AUTHOR.contains(author)) {
            return getTaipowerListByTxgId(user.getOrgId().getId());
        } else {
            return new ErrorResponse(Error.unauthorized);
        }
    }

    private ApiResponse getTaipowerListByTxgId(String txgId) {
        TxgProfile myTxg = relationService.seekTxgProfileFromTxgId(txgId);
        ServiceType serviceType = ServiceType.ofCode(myTxg.getServiceType());
        List<Name> myName = Collections.singletonList(NAME_BUILDER.apply(myTxg));
        TaipowerListResponse.TaipowerListResponseBuilder builder = TaipowerListResponse.builder()
                                                                                       .afclist(Collections.emptyList())
                                                                                       .dregList(Collections.emptyList())
                                                                                       .sregList(Collections.emptyList())
                                                                                       .srlist(Collections.emptyList())
                                                                                       .suplist(Collections.emptyList())
                                                                                       .edregList(Collections.emptyList());

        switch (serviceType) {
            case SR:
                return builder.srlist(myName).build();
            case SUP:
                return builder.suplist(myName).build();
            case dReg:
                return builder.dregList(myName).build();
            case sReg:
                return builder.sregList(myName).build();
            case AFC:
                return builder.afclist(myName).build();
            case E_dReg:
                return builder.edregList(myName).build();
            default:
                return new ErrorResponse(Error.internalServerError);
        }
    }

    private static Function<TxgProfile, Name> NAME_BUILDER = txg -> Name.builder()
                                                                        .id(txg.getTxgId())
                                                                        .name(txg.getName())
                                                                        .build();
    private static Function<ServiceType, Predicate<TxgProfile>> SERVICE_TYPE_MATCHER =
            serviceType ->
                    txg -> Objects.equals(serviceType.getCode(), txg.getServiceType());

    private TaipowerListResponse getLegacyTaipowerList() {
        List<TxgProfile> allTxgs = asNonNull(relationService.seekTxgProfiles());

        return TaipowerListResponse.builder()
                                   .srlist(allTxgs.stream()
                                                  .filter(SERVICE_TYPE_MATCHER.apply(ServiceType.SR))
                                                  .map(NAME_BUILDER)
                                                  .collect(Collectors.toList()))
                                   .afclist(allTxgs.stream()
                                                   .filter(SERVICE_TYPE_MATCHER.apply(ServiceType.AFC))
                                                   .map(NAME_BUILDER)
                                                   .collect(Collectors.toList()))
                                   .suplist(allTxgs.stream()
                                                   .filter(SERVICE_TYPE_MATCHER.apply(ServiceType.SUP))
                                                   .map(NAME_BUILDER)
                                                   .collect(Collectors.toList()))
                                   .dregList(allTxgs.stream()
                                                    .filter(SERVICE_TYPE_MATCHER.apply(ServiceType.dReg))
                                                    .map(NAME_BUILDER)
                                                    .collect(Collectors.toList()))
                                   .sregList(allTxgs.stream()
                                                    .filter(SERVICE_TYPE_MATCHER.apply(ServiceType.sReg))
                                                    .map(NAME_BUILDER)
                                                    .collect(Collectors.toList()))
                                   .build();
    }

}