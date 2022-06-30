package org.iii.esd.server.wrap;

import org.iii.esd.api.vo.AutomaticFrequencyControl;
import org.iii.esd.mongo.document.AutomaticFrequencyControlProfile;
import org.iii.esd.mongo.document.SiloCompanyProfile;

public class AutomaticFrequencyControlWrapper {

    public static AutomaticFrequencyControlProfile wrap(AutomaticFrequencyControl automaticFrequencyControl) {
        return merge(new AutomaticFrequencyControlProfile(), automaticFrequencyControl);
    }

    public static AutomaticFrequencyControlProfile merge(AutomaticFrequencyControlProfile automaticFrequencyControlProfile,
            AutomaticFrequencyControl automaticFrequencyControl) {
        automaticFrequencyControlProfile.setId(automaticFrequencyControl.getId());
        automaticFrequencyControlProfile.setName(automaticFrequencyControl.getName());
        if (automaticFrequencyControl.getCompanyId() != null) {
            automaticFrequencyControlProfile.setSiloCompanyProfile(new SiloCompanyProfile(automaticFrequencyControl.getCompanyId()));
        }
        automaticFrequencyControlProfile.setEnableStatus(automaticFrequencyControl.getEnableStatus());
        return automaticFrequencyControlProfile;
    }

}