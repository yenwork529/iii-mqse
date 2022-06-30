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
public class PMcalSpm implements PMcal {

    private List<BigDecimal> sbspmList;

    @Override
    public BigDecimal calculate() {
        if (sbspmList == null || sbspmList.size() == 0) { return null; }

        BigDecimal spm = Calculator.getAvgOfList(sbspmList);
        log.info("spm=" + spm);
        return spm;
    }

}
