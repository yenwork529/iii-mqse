package org.iii.esd.api.vo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.iii.esd.mongo.document.ElectricData;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class SpinReserveHistoryData {

    public static final String KEY_UPDATE_TIME = "updateTime";
    public static final String KEY_CURR_PERFORMANCE = "currPerf";
    public static final String KEY_PREVIOUS_PERFORMANCE = "prePerf";
    public static final String KEY_NEXT_TARGET = "nextTarget";
    public static final int SCALE_PERFORMANCE = 4;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date time;

    private BigDecimal meter1;

    private BigDecimal meter2;

    private BigDecimal meter3;

    private BigDecimal meter4;

    private BigDecimal m0;

    private BigDecimal m1;

    private BigDecimal m2;

    private BigDecimal m3;

    private BigDecimal m10;

    private BigDecimal msoc;

    private BigDecimal acPower;

    @JsonInclude(Include.NON_NULL)
    private BigDecimal awarded;

    @JsonInclude(Include.NON_NULL)
    private BigDecimal clip;

    @JsonInclude(Include.NON_NULL)
    private BigDecimal base;

    @JsonInclude(Include.NON_NULL)
    private BigDecimal target;

    @JsonInclude(Include.NON_NULL)
    private BigDecimal abandon;

    @JsonInclude(Include.NON_NULL)
    private Integer bidContractCapacity;

    @JsonInclude(Include.NON_NULL)
    private BigDecimal nextTarget;

    @JsonInclude(Include.NON_NULL)
    private BigDecimal currPerf;

    @JsonInclude(Include.NON_NULL)
    private BigDecimal totalkWh;

    public SpinReserveHistoryData(ElectricData electricData) {
        this.time = electricData.getTime();
        this.m0 = electricData.getM0kW();
        this.m1 = electricData.getM1kW();
        this.m2 = electricData.getM2kW();
        this.m3 = electricData.getM3kW();
        this.m10 = electricData.getM10kW();
        this.msoc = electricData.getMsoc();
        this.acPower = electricData.getActivePower();
    }

    public SpinReserveHistoryData(ResElectricData electricData) {
        this.time = electricData.getTime();
        this.m0 = electricData.getM0kW();
        this.m1 = electricData.getM1kW();
        this.m2 = electricData.getM2kW();
        this.m3 = electricData.getM3kW();
        this.m10 = electricData.getM10kW();
        this.msoc = electricData.getMsoc();
        this.acPower = electricData.getActivePower();
        this.totalkWh = electricData.getTotalkWh();
        this.meter1 = electricData.getMeter1kW();
        this.meter2 = electricData.getMeter2kW();
        this.meter3 = electricData.getMeter3kW();
        this.meter4 = electricData.getMeter4kW();
    }

    public static SpinReserveHistoryData getInstanceByTimeAndBidContractCapacity(Date time, Integer bidContractCapacity) {
        return SpinReserveHistoryData.builder()
                                     .time(time)
                                     .bidContractCapacity(bidContractCapacity)
                                     .build();
    }

    public SpinReserveHistoryData(Date time, Integer bidContractCapacity) {
        this.time = time;
        this.bidContractCapacity = bidContractCapacity;
    }

    public SpinReserveHistoryData(Date time, BigDecimal base, BigDecimal target) {
        this.time = time;
        this.base = base;
        this.target = target;
    }

    public SpinReserveHistoryData(Date time, BigDecimal base, BigDecimal target, BigDecimal clip) {
        this.time = time;
        this.base = base;
        this.target = target;
        this.clip = clip;
    }

    public static SpinReserveHistoryData getInstanceByTimeAndBase(Date time, BigDecimal base) {
        return SpinReserveHistoryData.builder()
                                     .time(time)
                                     .base(base)
                                     .build();
    }

    public static SpinReserveHistoryData getInstanceByTimeAndTargetClip(Date time, BigDecimal target, BigDecimal clip) {
        return SpinReserveHistoryData.builder()
                                     .time(time)
                                     .target(target)
                                     .clip(clip)
                                     .build();
    }

    public static SpinReserveHistoryData getInstanceByTimeAndBaseTargetClip(
            Date time, BigDecimal base, BigDecimal target, BigDecimal clip) {
        return SpinReserveHistoryData.builder()
                                     .time(time)
                                     .base(base)
                                     .target(target)
                                     .clip(clip)
                                     .build();
    }

    public SpinReserveHistoryData(Date time, BigDecimal awarded) {
        this.time = time;
        this.awarded = awarded;
    }

    public static SpinReserveHistoryData getInstanceByTimeAndAwarded(Date time, BigDecimal awarded) {
        return SpinReserveHistoryData.builder()
                                     .time(time)
                                     .awarded(awarded)
                                     .build();
    }

    public static SpinReserveHistoryData getInstanceByTimeAndAbandon(Date time, BigDecimal abandon) {
        return SpinReserveHistoryData.builder()
                                     .time(time)
                                     .abandon(abandon)
                                     .build();
    }

    public SpinReserveHistoryData merge(SpinReserveHistoryData other) {
        if (!Objects.isNull(this.time) && this.time.equals(other.time)) {
            this.base = Optional.ofNullable(other.base).orElse(this.base);
            this.target = Optional.ofNullable(other.target).orElse(this.target);
            this.bidContractCapacity = Optional.ofNullable(other.bidContractCapacity).orElse(this.bidContractCapacity);
            this.awarded = Optional.ofNullable(other.awarded).orElse(this.awarded);
            this.clip = Optional.ofNullable(other.clip).orElse(this.clip);
            this.abandon = Optional.ofNullable(other.abandon).orElse(this.abandon);
            this.nextTarget = Optional.ofNullable(other.nextTarget).orElse(this.nextTarget);
            this.currPerf = Optional.ofNullable(other.currPerf).orElse(this.currPerf);
            this.totalkWh = Optional.ofNullable(other.totalkWh).orElse(this.totalkWh);
        }

        return this;
    }

}