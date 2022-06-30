package org.iii.esd.thirdparty.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.line.LineResponse;
import org.iii.esd.exception.Error;
import org.iii.esd.thirdparty.notify.vo.response.JandiResponse;
import org.iii.esd.thirdparty.notify.vo.response.WeatherResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class HttpService {

    @Autowired
    private RestTemplate restTemplate;

    public HttpHeaders getHeaders(String url) {
        return restTemplate.headForHeaders(url);
    }

    public void saveFile(String url, HttpMethod httpMethod, String saveDir, String filename) {
        restTemplate.execute(url, httpMethod, null, clientHttpResponse -> {
            File dir = new File(saveDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(saveDir.concat(File.separator).concat(filename));
            FileOutputStream fos = new FileOutputStream(file);
            StreamUtils.copy(clientHttpResponse.getBody(), fos);
            fos.close();
            return file;
        });
    }

    public <T extends ApiResponse> T formGet(String url, Class<T> clazz) {
        return exchange(HttpMethod.GET, MediaType.APPLICATION_FORM_URLENCODED, url, null, null, clazz);
    }

    public <T extends ApiResponse> T formPost(String url, MultiValueMap<String, String> map, Class<T> clazz) {
        return exchange(HttpMethod.POST, MediaType.APPLICATION_FORM_URLENCODED, url, null, map, clazz);
    }

    public <T extends ApiResponse, S> T formPost(String url, S req, Class<T> clazz) {
        return exchange(HttpMethod.POST, MediaType.APPLICATION_FORM_URLENCODED, url, null, convert(req), clazz);
    }

    public <T extends ApiResponse, S> T jsonPost(String url, S req, Class<T> clazz) {
        return exchange(HttpMethod.POST, MediaType.APPLICATION_JSON_UTF8, url, null, req, clazz);
    }

    public <T extends ApiResponse, S> T authorizationFormPost(String url, String token, MultiValueMap<String, String> map, Class<T> clazz) {
        return exchange(HttpMethod.POST, MediaType.APPLICATION_FORM_URLENCODED, url, token, map, clazz);
    }

    public <T extends ApiResponse, S> T authorizationFormPost(String url, String token, S req, Class<T> clazz) {
        return exchange(HttpMethod.POST, MediaType.APPLICATION_FORM_URLENCODED, url, token, convert(req), clazz);
    }

    public <T extends ApiResponse, S> T authorizationJsonPost(String url, String token, S req, Class<T> clazz) {
        return exchange(HttpMethod.POST, MediaType.APPLICATION_JSON_UTF8, url, token, req, clazz);
    }

    public <T extends ApiResponse> T authorizationGet(String url, String token, MultiValueMap<String, String> map, Class<T> clazz) {
        return exchange(HttpMethod.GET, null, url, token, map, clazz);
    }

    @SuppressWarnings("unchecked")
    private <T extends ApiResponse, S> T exchange(HttpMethod httpMethod, MediaType mediaType, String url, String token, S req,
            Class<T> clazz) {
        try {
            ResponseEntity<T> entity = restTemplate.exchange(
                    url,
                    httpMethod,
                    getHttpEntity(mediaType, token, req),
                    clazz);
            return entity.getBody();
        } catch (Exception e) {
            // log.error(e.toString());
            log.error(ExceptionUtils.getStackTrace(e));
            return (T) instanceAbstractResponse(clazz, e.getMessage());
        }
    }

    private <T extends ApiResponse> ErrorResponse instanceAbstractResponse(Class<T> clazz, String errorMessage) {
        String resource = "";
        T t;
        try {
            t = clazz.newInstance();
            if (t instanceof WeatherResponse) {
                resource = "Central Weather Bureau. ";
            } else if (t instanceof LineResponse) {
                resource = "Line. ";
            } else if (t instanceof JandiResponse) {
                resource = "Jandi. ";
            }
        } catch (InstantiationException | IllegalAccessException e) {
            log.error(e.getMessage());
        }
        return new ErrorResponse(Error.unexpectedError, resource.concat(errorMessage));

        //		try {
        //			response = ConstructorUtils.invokeConstructor(clazz, new Object[] {
        //				HttpStatus.INTERNAL_SERVER_ERROR,
        //				errorMessage
        //			});
        //			log.info(response);
        //		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException	| InstantiationException e) {
        //			log.error(e.getMessage());
        //			response =  new ErrorResponse(Error.unexpectedError);
        //		}
        //		return response;
    }

    @SuppressWarnings("unchecked")
    private <S> HttpEntity<?> getHttpEntity(MediaType mediaType, String token, S req) {
        if (req instanceof MultiValueMap) {
            MultiValueMap<String, String> map = ((MultiValueMap<String, String>) req);
            map.forEach((k, vl) -> {
                vl.forEach(v -> {
                    if (v != null) {
                        try {
                            String nv = URLDecoder.decode(v, "UTF-8");
                            vl.remove(v);
                            vl.add(nv);
                        } catch (UnsupportedEncodingException e) {
                            log.info(e.getMessage());
                        }
                    }
                });
            });
            return new HttpEntity<MultiValueMap<String, String>>(map, getHttpHeaders(mediaType, token));
        } else {
            //log.info(req);
            return new HttpEntity<S>(req, getHttpHeaders(MediaType.APPLICATION_JSON_UTF8, token));
        }
    }

    private HttpHeaders getHttpHeaders(MediaType mediaType, String token) {
        HttpHeaders headers = new HttpHeaders();
        if (mediaType != null) {
            headers.setContentType(mediaType);
        }
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return headers;
    }

    private MultiValueMap<String, String> convert(Object obj) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.setAll(new ObjectMapper().convertValue(obj, new TypeReference<Map<String, String>>() {}));
        return parameters;
    }

}