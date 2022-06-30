package org.iii.esd.server.wrap;

import org.iii.esd.api.vo.Settlement;
import org.iii.esd.mongo.document.integrate.SettlementPrice;

public class SettlementWrapper {
    public static Settlement unwrap(SettlementPrice entity) {
        return Settlement.builder()
                         .dateTime(entity.getTimestamp())
                         .dreg(entity.getAfcSettlementPrice())
                         .sr(entity.getSrSettlementPrice())
                         .sup(entity.getSupSettlementPrice())
                         .marginal(entity.getMarginalElectricPrice())
                         .build();
    }
}
