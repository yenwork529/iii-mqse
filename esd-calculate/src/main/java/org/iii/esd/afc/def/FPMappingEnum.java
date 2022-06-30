package org.iii.esd.afc.def;

import static org.iii.esd.afc.def.RangeEnum.RANGE_o_x;
import static org.iii.esd.afc.def.RangeEnum.RANGE_t_u;
import static org.iii.esd.afc.def.RangeEnum.RANGE_u_o;
import static org.iii.esd.afc.def.RangeEnum.RANGE_v_w;
import static org.iii.esd.afc.def.RangeEnum.RANGE_x_y;
import static org.iii.esd.afc.def.RangeEnum.UNDEFINED_RANGE;
import static org.iii.esd.afc.def.ZoneEnum.DEAD_BAND;
import static org.iii.esd.afc.def.ZoneEnum.FIRST_BAND_CHARGE;
import static org.iii.esd.afc.def.ZoneEnum.FIRST_BAND_DISCHARGE;
import static org.iii.esd.afc.def.ZoneEnum.FULL_BAND_INPUT;
import static org.iii.esd.afc.def.ZoneEnum.FULL_BAND_OUTPUT;
import static org.iii.esd.afc.def.ZoneEnum.UNDEFINED_ZONE;

public enum FPMappingEnum {

    /**
     * AFC頻率與功率對照表
     * zone: 系統頻率區域
     * range: 輸出功率區間
     **/
    REF_POINT_AB_tu(FULL_BAND_OUTPUT, RANGE_t_u),  //全輸出反應頻率&輸出功率區間
    REF_POINT_BC_uo(FIRST_BAND_DISCHARGE, RANGE_u_o),  //第一段反應頻率(放電)&輸出功率區間
    REF_POINT_CD_vw(DEAD_BAND, RANGE_v_w),  //不動帶&輸出功率區間
    REF_POINT_DE_ox(FIRST_BAND_CHARGE, RANGE_o_x),  //第一段反應頻率(充電)&輸出功率區間
    REF_POINT_EF_xy(FULL_BAND_INPUT, RANGE_x_y),  //全輸入反應頻率&輸出功率區間
    UNDEFINED_MAPPING(UNDEFINED_ZONE, UNDEFINED_RANGE),  //未定義
    ;

    private ZoneEnum zone;
    private RangeEnum range;

    private FPMappingEnum(ZoneEnum zone, RangeEnum range) {
        this.zone = zone;
        this.range = range;
    }

    public static FPMappingEnum of(ZoneEnum zone) {
        for (FPMappingEnum entity : values()) {
            if (entity.getZone() == zone) { return entity; }
        }
        return UNDEFINED_MAPPING;
    }

    @Deprecated
    public static FPMappingEnum of(Double frequency, Double powerRatio) {
        for (FPMappingEnum entity : values()) {
            if (entity.getZone() == ZoneEnum.of(frequency) && entity.getRange() == RangeEnum.of(powerRatio)) { return entity; }
        }
        return UNDEFINED_MAPPING;
    }

    public ZoneEnum getZone() {
        return zone;
    }

    public RangeEnum getRange() {
        return range;
    }
}
