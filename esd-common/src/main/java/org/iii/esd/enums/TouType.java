package org.iii.esd.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TouType {

    /**
     * 表燈電價(Meter Rate Lighting)_住商型簡易時間電價二段式
     */
    TPMRL2S(120),
    /**
     * 表燈電價(Meter Rate Lighting)_住商型簡易時間電價三段式
     */
    TPMRL3S(130),
    /**
     * 表燈電價(Meter Rate Lighting)_標準型時間電價二段式
     */
    TPMRLB2S(121),
    /**
     * 低壓電力電價_二段式時間電價(契約容量未滿100 瓩者)
     */
    TPL2S(221),
    /**
     * 高壓電力電價_二段式時間電價(契約容量超過100 瓩者)
     */
    TPH2S(321),
    /**
     * 高壓電力電價_三段式時間電價尖峰時間固定(契約容量超過100 瓩者)
     */
    TPH3S(331),
    /**
     * 特高壓電力電價_二段式時間電價(契約容量超過1000 瓩者)
     */
    TPEH2S(421),
    /**
     * 特高壓電力電價_三段式時間電價尖峰時間固定(契約容量超過1000 瓩者)
     */
    TPEH3S(431),
    ;

    private int code;

    public static TouType getCode(int code) {
        for (TouType touType : values()) {
            if (touType.getCode() == code) {
                return touType;
            }
        }
        return null;
    }

}