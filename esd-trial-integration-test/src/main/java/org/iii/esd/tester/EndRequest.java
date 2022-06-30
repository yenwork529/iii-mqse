package org.iii.esd.tester;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class EndRequest {
    private String serverUrl;
    private String tgId;
    private String qseId;
    private Integer serviceType;
    private Instant stopTime;
}
