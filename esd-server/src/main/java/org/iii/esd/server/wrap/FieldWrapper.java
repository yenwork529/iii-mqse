package org.iii.esd.server.wrap;

import org.iii.esd.api.vo.Field;
import org.iii.esd.mongo.document.AutomaticFrequencyControlProfile;
import org.iii.esd.mongo.document.SiloCompanyProfile;
import org.iii.esd.mongo.document.DemandResponseProfile;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.PolicyProfile;
import org.iii.esd.mongo.document.SpinReserveProfile;

public class FieldWrapper {

    public static FieldProfile wrap(Field field) {
        return merge(new FieldProfile(), field);
    }

    public static FieldProfile merge(FieldProfile fieldProfile, Field field) {
        fieldProfile.setId(field.getId());
        fieldProfile.setName(field.getName());
        if (field.getCompanyId() != null) {
            fieldProfile.setSiloCompanyProfile(new SiloCompanyProfile(field.getCompanyId()));
        }
        //		if(field.getAfcId()!=null) {
        //			fieldProfile.setAutomaticFrequencyControlProfile(new AutomaticFrequencyControlProfile(field.getAfcId()));
        //		}
        //		if(field.getDrId()!=null) {
        //			fieldProfile.setDemandResponseProfile(new DemandResponseProfile(field.getDrId()));
        //		}
        //		if(field.getSrId()!=null) {
        //			fieldProfile.setSpinReserveProfile(new SpinReserveProfile(field.getSrId()));
        //		}
        fieldProfile.setAutomaticFrequencyControlProfile(
                field.getAfcId() != null ? new AutomaticFrequencyControlProfile(field.getAfcId()) : null);
        fieldProfile.setDemandResponseProfile(field.getDrId() != null ? new DemandResponseProfile(field.getDrId()) : null);
        fieldProfile.setSpinReserveProfile(field.getSrId() != null ? new SpinReserveProfile(field.getSrId()) : null);
        fieldProfile.setSrIndex(field.getSrIndex());
        fieldProfile.setTouType(field.getTouType());
        fieldProfile.setOyod(field.getOyod());
        fieldProfile.setTyodc(field.getTyodc());
        fieldProfile.setStationId(field.getStationId());
        fieldProfile.setIsReserve(field.getIsReserve());
        fieldProfile.setTcEnable(field.getTcEnable());
        fieldProfile.setTcIp(field.getTcIp());
        fieldProfile.setTargetType(field.getTargetType());
        if (field.getPolicyId() != null) {
            fieldProfile.setPolicyProfile(new PolicyProfile(field.getPolicyId()));
        }
        return fieldProfile;
    }

}
