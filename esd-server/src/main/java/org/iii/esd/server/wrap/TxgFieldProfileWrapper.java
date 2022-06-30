package org.iii.esd.server.wrap;

import org.iii.esd.api.vo.integrate.Res;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;

public final class TxgFieldProfileWrapper {
    private TxgFieldProfileWrapper() {}

    public static TxgFieldProfile wrap(Res res) {
        return TxgFieldProfile.builder()
                              .txgId(res.getTxgId())
                              .resId(res.getResId())
                              .companyId(res.getCompanyId())
                              .name(res.getName())
                              .resCode(res.getResCode())
                              .resType(res.getResType())
                              .registerCapacity(res.getRegisterCapacity())
                              .accFactor(res.getAccFactor())
                              .tcEnable(res.getTcEnable())
                              .tcUrl(res.getTcUrl())
                              .devStatus(res.getDevStatus())
                              .build();
    }

    public static Res unwrap(TxgFieldProfile resProfile) {
        return Res.builder()
                  .txgId(resProfile.getTxgId())
                  .resId(resProfile.getResId())
                  .companyId(resProfile.getCompanyId())
                  .name(resProfile.getName())
                  .resCode(resProfile.getResCode())
                  .resType(resProfile.getResType())
                  .registerCapacity(resProfile.getRegisterCapacity())
                  .accFactor(resProfile.getAccFactor())
                  .tcEnable(resProfile.getTcEnable())
                  .tcUrl(resProfile.getTcUrl())
                  .tcLastUploadTime(resProfile.getTcLastUploadTime())
                  .devStatus(resProfile.getDevStatus())
                  .createTimestamp(resProfile.getCreateTime())
                  .updateTimestamp(resProfile.getUpdateTime())
                  .build();
    }

    public static void copy(Res res, TxgFieldProfile resProfile) {
        resProfile.setTxgId(res.getTxgId());
        resProfile.setCompanyId(res.getCompanyId());
        resProfile.setName(res.getName());
        resProfile.setResCode(res.getResCode());
        resProfile.setResType(res.getResType());
        resProfile.setAccFactor(res.getAccFactor());
        resProfile.setRegisterCapacity(res.getRegisterCapacity());
        resProfile.setTcUrl(res.getTcUrl());
    }
}
