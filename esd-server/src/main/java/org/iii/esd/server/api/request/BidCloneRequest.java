package org.iii.esd.server.api.request;

import java.util.Date;

import lombok.Data;

@Data
public class BidCloneRequest {

    private Date baseDate;

    private Date targetDate;

    private Integer offset;
}
