package org.iii.esd.server;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import org.iii.esd.api.RestConstants;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    public static final String DEFAULT_INCLUDE_PATTERN = RestConstants.FUNCTION_ESD + "/**";
    //public static final String DEFAULT_INCLUDE_PATTERN = RestConstants.FUNCTION_ESD+"/.*";
    public static final String JWT = "JWT";

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                //              .pathMapping("/")
                .forCodeGeneration(true)
                .useDefaultResponseMessages(true)
                //              .directModelSubstitute(java.time.ZonedDateTime.class, Date.class)
                //              .directModelSubstitute(java.time.LocalDateTime.class, Date.class)
                //				.globalResponseMessage(RequestMethod.GET,Arrays.asList(
                //						new ResponseMessageBuilder().code(500).message("500 message").responseModel(new ModelRef("Error")).build(),
                //						new ResponseMessageBuilder().code(403).message("Forbidden!!!!!").build()
                //				))
                .securitySchemes(Arrays.asList(securityScheme()))
                .securityContexts(Arrays.asList(securityContext()))
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.iii.esd.server.controllers.rest"))
                .paths(PathSelectors.ant(DEFAULT_INCLUDE_PATTERN))
                //.paths(PathSelectors.regex(DEFAULT_INCLUDE_PATTERN))
                //.paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("能源調度系統 RESTful APIs")
                .description("相關說明")
                .version("1.0")
                //.termsOfServiceUrl("https://140.92.88.138")
                //.contact(new Contact("ESD Admin", "esd.iii.org.tw", "admin@iii.org.tw"))
                //.license("Apache 2.0")
                ///.licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html")
                .build();
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                              .securityReferences(defaultAuth())
                              //.forPaths(PathSelectors.regex(DEFAULT_INCLUDE_PATTERN))
                              .forPaths(PathSelectors.ant(DEFAULT_INCLUDE_PATTERN))
                              .build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Arrays.asList(new SecurityReference(JWT, authorizationScopes));
    }

    private SecurityScheme securityScheme() {
        return new ApiKey(JWT, HttpHeaders.AUTHORIZATION, "header");
    }

    //    @Bean
    //    public SecurityConfiguration security() {
    //        return SecurityConfigurationBuilder.builder()
    //            .clientId(CLIENT_ID)
    //            .clientSecret(CLIENT_SECRET)
    //            .scopeSeparator(" ")
    //            .useBasicAuthenticationWithAccessCodeGrant(true)
    //            .build();
    //    }
    //
    //	private SecurityScheme securityScheme() {
    //		GrantType grantType = new AuthorizationCodeGrantBuilder()
    //				.tokenEndpoint(new TokenEndpoint(AUTH_SERVER + "/token", "oauthtoken"))
    //				.tokenRequestEndpoint(new TokenRequestEndpoint(AUTH_SERVER + "/authorize", CLIENT_ID, CLIENT_SECRET))
    //				.build();
    //
    //		SecurityScheme oauth = new OAuthBuilder().name("spring_oauth").grantTypes(Arrays.asList(grantType))
    //				.scopes(Arrays.asList(scopes())).build();
    //		return oauth;
    //	}
    //
    //	private AuthorizationScope[] scopes() {
    //		AuthorizationScope[] scopes = {
    //				new AuthorizationScope("read", "for read operations"),
    //				new AuthorizationScope("write", "for write operations"),
    //				new AuthorizationScope("foo", "Access foo API") };
    //		return scopes;
    //	}
    //
    //	private SecurityContext securityContext() {
    //		return SecurityContext.builder()
    //				.securityReferences(Arrays.asList(new SecurityReference("spring_oauth", scopes())))
    //				.forPaths(PathSelectors.regex("/foos.*")).build();
    //	}

}