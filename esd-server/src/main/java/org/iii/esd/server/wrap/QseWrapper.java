package org.iii.esd.server.wrap;

import org.iii.esd.api.vo.integrate.Qse;
import org.iii.esd.mongo.document.integrate.QseProfile;

public final class QseWrapper {
    private QseWrapper() {}

    public static QseProfile wrap(Qse vo) {
        return QseProfile.builder()
                         .qseId(vo.getQseId())
                         .companyId(vo.getCompanyId())
                         .name(vo.getName())
                         .qseCode(vo.getQseCode())
                         .dnpUrl(vo.getDnpUrl())
                         .vpnLanIp(vo.getVpnLanIp())
                         .vpnWanIp(vo.getVpnWanIp())
                         .build();
    }

    public static Qse unwrap(QseProfile entity) {
        return Qse.builder()
                  .id(entity.getId())
                  .qseId(entity.getQseId())
                  .companyId(entity.getCompanyId())
                  .name(entity.getName())
                  .qseCode(entity.getQseCode())
                  .dnpUrl(entity.getDnpUrl())
                  .vpnLanIp(entity.getVpnLanIp())
                  .vpnWanIp(entity.getVpnWanIp())
                  .createTimestamp(entity.getCreateTime())
                  .updateTimestamp(entity.getUpdateTime())
                  .build();
    }
}
