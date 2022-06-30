package org.iii.esd.server.wrap;

import java.util.Date;

import org.iii.esd.api.vo.ModbusMeter;
import org.iii.esd.mongo.document.AutomaticFrequencyControlLog;

public class AfcLogWrapper {

    public static AutomaticFrequencyControlLog wrap(ModbusMeter modbusMeter, Long afcId) {
        return merge(new AutomaticFrequencyControlLog(afcId), modbusMeter);
    }

    public static AutomaticFrequencyControlLog merge(AutomaticFrequencyControlLog afcLog, ModbusMeter modbusMeter) {
        afcLog.setTimestamp(modbusMeter.getReportTime());
        afcLog.setUpdateTime(new Date());
        afcLog.setActualFrequency(modbusMeter.getActualFrequency());
        afcLog.setVoltageA(modbusMeter.getVoltageA());
        afcLog.setVoltageB(modbusMeter.getVoltageB());
        afcLog.setVoltageC(modbusMeter.getVoltageC());
        afcLog.setCurrentA(modbusMeter.getCurrentA());
        afcLog.setCurrentB(modbusMeter.getCurrentB());
        afcLog.setCurrentC(modbusMeter.getCurrentC());
        afcLog.setActivePower(modbusMeter.getActivePower());
        afcLog.setActivePowerA(modbusMeter.getActivePowerA());
        afcLog.setActivePowerB(modbusMeter.getActivePowerB());
        afcLog.setActivePowerC(modbusMeter.getActivePowerC());
        afcLog.setKVAR(modbusMeter.getKvar());
        afcLog.setKVARA(modbusMeter.getKvarA());
        afcLog.setKVARB(modbusMeter.getKvarB());
        afcLog.setKVARC(modbusMeter.getKvarC());
        afcLog.setPowerFactor(modbusMeter.getPowerFactor());
        afcLog.setPowerFactorA(modbusMeter.getPowerFactorA());
        afcLog.setPowerFactorB(modbusMeter.getPowerFactorB());
        afcLog.setPowerFactorC(modbusMeter.getPowerFactorC());
        afcLog.setStatus(modbusMeter.getStatus());
        afcLog.setRelayStatus(modbusMeter.getRelayStatus());
        afcLog.setSoc(modbusMeter.getSoc());
        afcLog.setFrequency(modbusMeter.getFrequency());
        afcLog.setEssPower(modbusMeter.getEssPower());
        afcLog.setEssPowerRatio(modbusMeter.getEssPowerRatio());
        return afcLog;
    }

}