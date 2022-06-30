package org.iii.esd.mongo.vo.data.setup;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OtherSetupData implements ISetupData {

    /**
     * Current Transformer ratio
     */
    private BigDecimal ct;
    /**
     * Phase voltage Transformers ratio
     */
    private BigDecimal pt;

    public SetupData wrap() {
        return SetupData.builder().
                ct(ct).
                                pt(pt).
                                build();
    }

}