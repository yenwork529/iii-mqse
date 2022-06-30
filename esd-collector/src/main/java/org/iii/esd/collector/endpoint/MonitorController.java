package org.iii.esd.collector.endpoint;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.collector.services.MeterDetector;
import org.iii.esd.thirdparty.service.HttpService;
import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
public class MonitorController {

    @Autowired
    MeterDetector meterDetector;

    public class ReportResponse extends SuccessfulResponse {
        public Object report;
    }

    @RequestMapping(value = "/devreport", method = { RequestMethod.GET }, produces = { "application/json" })
    public ApiResponse debughelo( //
            // @PathVariable String time //
            ) //
    {
        ReportResponse rr = new ReportResponse();
        rr.report = meterDetector.getReport();

        return rr;
    }
}
