package org.iii.esd.server.wrap;

import org.iii.esd.api.vo.integrate.Txg;
import org.iii.esd.mongo.document.integrate.TxgProfile;

public final class TxgProfileWrapper {
    private TxgProfileWrapper() {}

    public static TxgProfile wrap(Txg txg) {
        return TxgProfile.builder()
                         .txgId(txg.getTxgId())
                         .qseId(txg.getQseId())
                         .companyId(txg.getCompanyId())
                         .name(txg.getName())
                         .txgCode(txg.getTxgCode())
                         .serviceType(txg.getServiceType())
                         .registerCapacity(txg.getRegisterCapacity())
                         .efficiencyPrice(txg.getEfficiencyPrice())
                         .enableStatus(txg.getEnableStatus())
                         .build();
    }

    public static Txg unwrap(TxgProfile txgProfile) {
        return Txg.builder()
                  .txgId(txgProfile.getTxgId())
                  .qseId(txgProfile.getQseId())
                  .companyId(txgProfile.getCompanyId())
                  .name(txgProfile.getName())
                  .txgCode(txgProfile.getTxgCode())
                  .serviceType(txgProfile.getServiceType())
                  .registerCapacity(txgProfile.getRegisterCapacity())
                  .efficiencyPrice(txgProfile.getEfficiencyPrice())
                  .enableStatus(txgProfile.getEnableStatus())
                  .createTimestamp(txgProfile.getCreateTime())
                  .updateTimestamp(txgProfile.getUpdateTime())
                  .build();
    }

    public static void copy(Txg txg, TxgProfile txgProfile) {
        txgProfile.setRegisterCapacity(txg.getRegisterCapacity());
        txgProfile.setEfficiencyPrice(txg.getEfficiencyPrice());
        txgProfile.setTxgCode(txg.getTxgCode());
        txgProfile.setName(txg.getName());
        txgProfile.setServiceType(txg.getServiceType());
        txgProfile.setTxgId(txg.getTxgId());
        txgProfile.setQseId(txg.getQseId());
        txgProfile.setCompanyId(txg.getCompanyId());
        txgProfile.setEnableStatus(txg.getEnableStatus());
    }
}
