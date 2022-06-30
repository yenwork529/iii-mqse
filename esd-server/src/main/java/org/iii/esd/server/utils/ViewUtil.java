package org.iii.esd.server.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.DataResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.ListResponse;
import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.exception.ApplicationException;
import org.iii.esd.exception.Error;
import org.iii.esd.exception.WebException;

@Log4j2
public final class ViewUtil {
    private ViewUtil() {}

    public static ApiResponse run(RunnableWithWebException runnable) {
        try {
            runnable.run();
            return new SuccessfulResponse();
        } catch (ApplicationException ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            return ex.getResponse();
        } catch (WebException ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            return ex.getResponse();
        } catch (Exception ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            return new ErrorResponse(Error.internalServerError, ex.getMessage());
        }
    }

    public static <T> ApiResponse get(CallableWithWebException<T> callable) {
        try {
            T data = callable.call();
            return new DataResponse<>(data);
        } catch (ApplicationException ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            return ex.getResponse();
        } catch (WebException ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            return ex.getResponse();
        } catch (Exception ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            return new ErrorResponse(Error.internalServerError, ex.getMessage());
        }
    }

    public static <T> ApiResponse getAll(CallableWithWebException<List<T>> callable) {
        try {
            List<T> list = callable.call();
            return new ListResponse<>(list);
        } catch (ApplicationException ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            return ex.getResponse();
        } catch (WebException ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            return ex.getResponse();
        } catch (Exception ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            return new ErrorResponse(Error.internalServerError, ex.getMessage());
        }
    }

    public static ResponseEntity<?> getFile(CallableWithWebException<byte[]> callable, String fileName, MediaType contentType) {
        try {
            byte[] fileContent = callable.call();
            int fileLength = fileContent.length;
            ByteArrayResource resource = new ByteArrayResource(fileContent);

            HttpHeaders headers = new HttpHeaders();
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentType(contentType);
            headers.setContentLength(fileLength);

            return ResponseEntity.ok()
                                 .headers(headers)
                                 .contentLength(fileLength)
                                 .contentType(contentType)
                                 .body(resource);
        } catch (ApplicationException ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            return ResponseEntity.ok(ex.getResponse());
        } catch (WebException ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            return ResponseEntity.ok(ex.getResponse());
        } catch (Exception ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            return ResponseEntity.ok(new ErrorResponse(Error.internalServerError, ex.getMessage()));
        }
    }

    public static ResponseEntity<?> getFile(CallableWithWebException<File> callable, MediaType contentType) {
        try {
            File fileRef = callable.call();
            long fileLength = fileRef.length();
            String fileName = fileRef.getName();
            InputStreamResource resource = new InputStreamResource(new FileInputStream(fileRef));

            HttpHeaders headers = new HttpHeaders();
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentType(contentType);
            headers.setContentLength(fileLength);

            return ResponseEntity.ok()
                                 .headers(headers)
                                 .contentLength(fileLength)
                                 .contentType(contentType)
                                 .body(resource);
        } catch (ApplicationException ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            return ResponseEntity.ok(ex.getResponse());
        } catch (WebException ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            return ResponseEntity.ok(ex.getResponse());
        } catch (Exception ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            return ResponseEntity.ok(new ErrorResponse(Error.internalServerError, ex.getMessage()));
        }
    }
}
