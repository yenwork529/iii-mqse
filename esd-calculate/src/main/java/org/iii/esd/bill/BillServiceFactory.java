package org.iii.esd.bill;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.iii.esd.enums.TouType;

@Service
public class BillServiceFactory {

    @Autowired
    Tpmrl2sBillService tpmrl2sBillService;

    @Autowired
    Tph3sBillService tph3sBillService;

    public IBillService GetInstance(TouType toutype) {
        if (toutype == TouType.TPH3S) {
            return tph3sBillService;
        } else if (toutype == TouType.TPMRL2S) {
            return tpmrl2sBillService;
        }
        throw new NotImplementedException("");
    }

}
