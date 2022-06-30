package org.iii.esd.server.wrap;

import org.iii.esd.api.vo.SpinReserve;
import org.iii.esd.mongo.document.SiloCompanyProfile;
import org.iii.esd.mongo.document.SpinReserveProfile;

public class SpinReserveWrapper {

    public static SpinReserveProfile wrap(SpinReserve spinReserve) {
        return merge(new SpinReserveProfile(), spinReserve);
    }

    public static SpinReserveProfile merge(SpinReserveProfile spinReserveProfile, SpinReserve spinReserve) {
        spinReserveProfile.setId(spinReserve.getId());
        spinReserveProfile.setName(spinReserve.getName());
        if (spinReserve.getCompanyId() != null) {
            spinReserveProfile.setSiloCompanyProfile(new SiloCompanyProfile(spinReserve.getCompanyId()));
        }
        spinReserveProfile.setDnpURL(spinReserve.getDnpURL());
        spinReserveProfile.setEnableStatus(spinReserve.getEnableStatus());
        spinReserveProfile.setBidContractCapacity(spinReserve.getBidContractCapacity());
        return spinReserveProfile;
    }

}