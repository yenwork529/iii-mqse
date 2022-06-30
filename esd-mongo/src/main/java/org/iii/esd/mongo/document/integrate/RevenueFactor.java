package org.iii.esd.mongo.document.integrate;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import org.iii.esd.mongo.document.UuidDocument;

import static org.iii.esd.utils.DatetimeUtils.toDate;
import static org.iii.esd.utils.DatetimeUtils.toLocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document(collection = "RevenueFactor")
public class RevenueFactor extends UuidDocument {

    public static final RevenueFactor DEFAULT = new RevenueFactor();

    private String orgId;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date timestamp;

    private Long timeticks;

    private BigDecimal revenueFactor;

    public static String buildId(String orgId, long timeticks) {
        return orgId + String.format("_%d", timeticks);
    }

    public RevenueFactor(String orgId, Long ticks) {
        this.orgId = orgId;
        this.timeticks = (ticks / 60000L) * 60000L;
        this.timestamp = new Date(this.timeticks);
        super.setId(buildId(orgId, this.timeticks));
    }

    public RevenueFactor(String orgId, Date timestamp) {
        this.orgId = orgId;
        this.timestamp = toDate(toLocalDateTime(timestamp).truncatedTo(ChronoUnit.MINUTES));
        this.timeticks = this.timestamp.getTime();
        super.setId(buildId(orgId, this.timeticks));
    }
}
