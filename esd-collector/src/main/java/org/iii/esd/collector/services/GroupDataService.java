package org.iii.esd.collector.services;

import java.math.BigDecimal;
import java.util.List;

import org.iii.esd.mongo.document.integrate.DrResData;
import org.iii.esd.mongo.document.integrate.DrTxgData;
import org.springframework.stereotype.Service;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class GroupDataService {

    // public DrTxgData merge(String txgId, List<DrResData> flst){
    //     DrTxgData dx = new DrTxgData(txgId);
    //     dx.setTimestamp(flst.get(0).getTimestamp());
    //     BigDecimal g1M1kW = BigDecimal.ZERO;
    //     BigDecimal g1M1EnergyIMP = BigDecimal.ZERO;    
    //     BigDecimal g1M1EnergyEXP = BigDecimal.ZERO;
    
    //     for(DrResData f: flst){
    //         g1M1kW.add(f.getM1kW());
    //         g1M1EnergyIMP.add(f.getM1EnergyIMP());
    //         g1M1EnergyEXP.add(f.getM1EnergyEXP());
    //     }
    //     dx.setG1M1kW(g1M1kW);
    //     dx.setG1M1EnergyIMP(g1M1EnergyIMP);
    //     dx.setG1M1EnergyEXP(g1M1EnergyEXP);
    //     return dx;
    // }
}
