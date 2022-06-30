package org.iii.esd.api.vo;

import java.util.List;

import org.iii.esd.mongo.document.SpinReserveProfile;
import org.iii.esd.mongo.document.integrate.TxgProfile;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class SpinReserveMonitorData {

    private Long srId;

    private String txgId;

    private String srName;

    private List<MonitorData> list;

    public SpinReserveMonitorData(SpinReserveProfile spinReserveProfile, List<MonitorData> list) {
        if (spinReserveProfile != null) {
            this.srId = spinReserveProfile.getId();
            this.srName = spinReserveProfile.getName();
        }
        this.list = list;
    }

    public SpinReserveMonitorData(TxgProfile txgProfile, List<MonitorData> list) {
        if (txgProfile != null) {
            this.txgId = txgProfile.getTxgId();
            this.srName = txgProfile.getName();
        }

        this.list = list;
    }
}