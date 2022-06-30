package org.iii.esd.server.domain.trial;

import lombok.Getter;

import org.iii.esd.exception.EnumInitException;

public enum ReportType {

    HISTORY("history"),
    PERFORMANCE("performance");

    @Getter
    private final String reportName;

    ReportType(String name) {
        this.reportName = name;
    }

    public static ReportType ofReportName(String name) {
        for (ReportType value : values()) {
            if (value.getReportName().equalsIgnoreCase(name)) {
                return value;
            }
        }

        throw new EnumInitException(ReportType.class, name);
    }
}
