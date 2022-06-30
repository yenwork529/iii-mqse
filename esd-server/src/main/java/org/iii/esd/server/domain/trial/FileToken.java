package org.iii.esd.server.domain.trial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileToken {
    private String token;
    private String timestamp;
    private String type;
    private Long length;
}
