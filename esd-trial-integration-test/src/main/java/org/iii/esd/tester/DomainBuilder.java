package org.iii.esd.tester;

import java.time.Instant;

import org.iii.esd.api.request.trial.DnpAoRequest;
import org.iii.esd.api.request.trial.DnpDoRequest;
import org.iii.esd.api.request.trial.DnpSrRequest;
import org.iii.esd.api.request.trial.DnpSupRequest;

import static org.iii.esd.common.Constant.SERVICE_TYPE_SUP;
import static org.iii.esd.util.TimestampUtils.getAoTimestamp;
import static org.iii.esd.util.TimestampUtils.truncateToMinute;

public class DomainBuilder {
    public static DnpDoRequest buildAlert(int serviceType) {
        if (SERVICE_TYPE_SUP == serviceType) {
            return buildSupAlert();
        } else {
            return buildSrAlert();
        }
    }

    private static DnpDoRequest buildSupAlert() {
        return DnpDoRequest.builder()
                           .sr(DnpSrRequest.DoRequest.builder()
                                                     .consumeNotEnough(false)
                                                     .begin(false)
                                                     .end(false)
                                                     .build())
                           .sup(DnpSupRequest.DoRequest.builder()
                                                       .consumeNotEnough(true)
                                                       .begin(false)
                                                       .end(false)
                                                       .build())
                           .build();
    }

    private static DnpDoRequest buildSrAlert() {
        return DnpDoRequest.builder()
                           .sr(DnpSrRequest.DoRequest.builder()
                                                     .consumeNotEnough(true)
                                                     .begin(false)
                                                     .end(false)
                                                     .build())
                           .sup(DnpSupRequest.DoRequest.builder()
                                                       .consumeNotEnough(false)
                                                       .begin(false)
                                                       .end(false)
                                                       .build())
                           .build();
    }

    public static DnpDoRequest buildBegin(int serviceType) {
        if (SERVICE_TYPE_SUP == serviceType) {
            return buildSupBegin();
        } else {
            return buildSrBegin();
        }
    }

    private static DnpDoRequest buildSupBegin() {
        return DnpDoRequest.builder()
                           .sr(DnpSrRequest.DoRequest.builder()
                                                     .consumeNotEnough(false)
                                                     .begin(false)
                                                     .end(false)
                                                     .build())
                           .sup(DnpSupRequest.DoRequest.builder()
                                                       .consumeNotEnough(false)
                                                       .begin(true)
                                                       .end(false)
                                                       .build())
                           .build();
    }

    private static DnpDoRequest buildSrBegin() {
        return DnpDoRequest.builder()
                           .sr(DnpSrRequest.DoRequest.builder()
                                                     .consumeNotEnough(false)
                                                     .begin(true)
                                                     .end(false)
                                                     .build())
                           .sup(DnpSupRequest.DoRequest.builder()
                                                       .consumeNotEnough(false)
                                                       .begin(false)
                                                       .end(false)
                                                       .build())
                           .build();
    }

    public static DnpDoRequest buildEnd(int serviceType) {
        if (SERVICE_TYPE_SUP == serviceType) {
            return buildSupEnd();
        } else {
            return buildSrEnd();
        }
    }

    private static DnpDoRequest buildSupEnd() {
        return DnpDoRequest.builder()
                           .sr(DnpSrRequest.DoRequest.builder()
                                                     .consumeNotEnough(false)
                                                     .begin(false)
                                                     .end(false)
                                                     .build())
                           .sup(DnpSupRequest.DoRequest.builder()
                                                       .consumeNotEnough(false)
                                                       .begin(false)
                                                       .end(true)
                                                       .build())
                           .build();
    }

    public static DnpDoRequest buildSrEnd() {
        return DnpDoRequest.builder()
                           .sr(DnpSrRequest.DoRequest.builder()
                                                     .consumeNotEnough(false)
                                                     .begin(false)
                                                     .end(true)
                                                     .build())
                           .sup(DnpSupRequest.DoRequest.builder()
                                                       .consumeNotEnough(false)
                                                       .begin(false)
                                                       .end(false)
                                                       .build())
                           .build();
    }

    public static DnpAoRequest buildBeginParam(BeginRequest begin) {
        if (SERVICE_TYPE_SUP == begin.getServiceType()) {
            return buildSupBeginParam(begin.getStartTime(), begin.getStopTime(), begin.getCapacity());
        } else {
            return buildSrBeginParam(begin.getStartTime(), begin.getStopTime(), begin.getCapacity());
        }
    }

    public static DnpAoRequest buildSupBeginParam(Instant startTime, Instant stopTime, Long capacity) {
        return DnpAoRequest.builder()
                           .sup(DnpSupRequest.AoRequest.builder()
                                                       .beginTime(getAoTimestamp(truncateToMinute(Instant.now())))
                                                       .startTime(getAoTimestamp(truncateToMinute(startTime)))
                                                       .stopTime(getAoTimestamp(truncateToMinute(stopTime)))
                                                       .capacity(capacity)
                                                       .build())
                           .build();
    }

    public static DnpAoRequest buildSrBeginParam(Instant startTime, Instant stopTime, Long capacity) {
        return DnpAoRequest.builder()
                           .sr(DnpSrRequest.AoRequest.builder()
                                                     .beginTime(getAoTimestamp(truncateToMinute(Instant.now())))
                                                     .startTime(getAoTimestamp(truncateToMinute(startTime)))
                                                     .stopTime(getAoTimestamp(truncateToMinute(stopTime)))
                                                     .capacity(capacity)
                                                     .build())
                           .build();
    }

    public static DnpAoRequest buildEndParam(EndRequest end) {
        if (SERVICE_TYPE_SUP == end.getServiceType()) {
            return buildSupEndParam(end.getStopTime());
        } else {
            return buildSrEndParam(end.getStopTime());
        }
    }

    public static DnpAoRequest buildSupEndParam(Instant stopTime) {
        return DnpAoRequest.builder()
                           .sup(DnpSupRequest.AoRequest.builder()
                                                       .endTime(getAoTimestamp(truncateToMinute(Instant.now())))
                                                       .stopTime(getAoTimestamp(truncateToMinute(stopTime)))
                                                       .build())
                           .build();
    }

    public static DnpAoRequest buildSrEndParam(Instant stopTime) {
        return DnpAoRequest.builder()
                           .sr(DnpSrRequest.AoRequest.builder()
                                                     .endTime(getAoTimestamp(truncateToMinute(Instant.now())))
                                                     .stopTime(getAoTimestamp(truncateToMinute(stopTime)))
                                                     .build())
                           .build();
    }

}
