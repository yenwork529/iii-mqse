package org.iii.esd.afc.performance;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.iii.esd.afc.utils.Calculator;

@Log4j2
@Component
@Data
@Scope("prototype")
public class PMcalAspm implements PMcal {

    private List<BigDecimal> spmList;

    @Override
    public BigDecimal calculate() {
        if (spmList == null || spmList.size() == 0) { return null; }

        BigDecimal aspm = Calculator.getAvgOfList(spmList);
        log.info("aspm=" + aspm);
        return aspm;
    }

}
