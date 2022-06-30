package org.iii.esd.mongo.domain;

import java.time.LocalDateTime;
import java.util.Date;

import org.iii.esd.utils.DatetimeUtils;

public class Today {

    private LocalDateTime now;

    public Today() {
        this.now = LocalDateTime.now();
    }

    public Date at(int hour, int minute, int second) {
        LocalDateTime at = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), hour, minute, second);
        return DatetimeUtils.toDate(at);
    }

    public Date start() {
        return at(0, 0, 0);
    }

    public Date end() {
        return at(23, 59, 59);
    }

    public Date now() {
        return DatetimeUtils.toDate(now);
    }

    public Date at(int hour, int minute) {
        return at(hour, minute, 0);
    }

    public Date at(int hour) {
        return at(hour, 0, 0);
    }
}
