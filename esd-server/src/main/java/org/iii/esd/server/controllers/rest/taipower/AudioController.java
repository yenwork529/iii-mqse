package org.iii.esd.server.controllers.rest.taipower;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.mongo.document.SpinReserveProfile;
import org.iii.esd.mongo.service.SpinReserveService;

import static org.iii.esd.exception.Error.invalidParameter;
import static org.iii.esd.exception.Error.notFound;

@RestController
@Log4j2
public class AudioController {

    private final static String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";

    private final static String CONTENT_TYPE_MP3 = "audio/mp3";

    private final static String SLASH = "/";

    private final static String FOLDER_AUDIO = "audio";

    private final static String FILENAME_SR_PLAYBACK = "sr_playback.mp3";
    @Autowired
    ServletContext context;
    @Autowired
    private SpinReserveService spinReserveService;

    @PostMapping(value = "/resources/audio/{srId}")
    public void getAudioStream(final HttpServletResponse response, @PathVariable Long srId)
            throws IOException, ServletException {
        OutputStream out = response.getOutputStream();
        if (!isValidSrId(srId)) {
            response.setContentType(CONTENT_TYPE_JSON);
            out.write(new Gson().toJson(new ErrorResponse(invalidParameter, "Not a valid srId:" + srId)).getBytes());
            out.flush();
            return;
        }

        ServletOutputStream stream = null;
        BufferedInputStream buf = null;
        try {

            //    	  File mp3 = new ClassPathResource(resourcePath).getFile();
            //    	  response.setContentType("audio/mpeg");
            //    	  response.setContentType(CONTENT_TYPE_MP3);
            //    	  response.addHeader("Content-Disposition", "attachment; filename=" + FILENAME_SR_PLAYBACK);
            //    	  response.setContentLength((int) mp3.length());
            //    	  FileInputStream input = new FileInputStream(mp3);
            //    	  buf = new BufferedInputStream(input);
            //    	  int readBytes = 0;
            //    	  while ((readBytes = buf.read()) != -1)
            //    	    stream.write(readBytes);

            stream = response.getOutputStream();
            response.setContentType(CONTENT_TYPE_MP3);

            String resourcePath = FOLDER_AUDIO + SLASH + srId + SLASH + FILENAME_SR_PLAYBACK;
            ClassPathResource resource = new ClassPathResource(resourcePath);
            InputStream inputStream = resource.getInputStream();
            buf = new BufferedInputStream(inputStream);
            int readBytes = 0;
            while ((readBytes = buf.read()) != -1) {
                stream.write(readBytes);
            }
        } catch (Exception ex) {
            String errDetail = ex.getMessage();
            log.error(errDetail);
            response.setContentType(CONTENT_TYPE_JSON);
            out.write(new Gson().toJson(new ErrorResponse(notFound, errDetail)).getBytes());
            out.flush();
        } finally {
            if (stream != null) { stream.close(); }
            if (buf != null) { buf.close(); }
            if (out != null) { out.close(); }
        }
    }

    private boolean isValidSrId(Long srId) {
        if (srId == null) {
            return false;
        }
        Optional<SpinReserveProfile> spinReserveProfile = spinReserveService.findSpinReserveProfile(srId);
        if (spinReserveProfile.isPresent()) {
            return true;
        }
        return false;
    }
}
