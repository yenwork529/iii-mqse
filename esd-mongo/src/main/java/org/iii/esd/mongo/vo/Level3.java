package org.iii.esd.mongo.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Level3 {
    /**
     * 平日DR墊基選項 1:所有DR降載時段(10:00-12:00, 13:00-17:00) 2:特定時段(13:00-15:00)
     */
    private int timeslotType;
    /**
     * 餘電釋放選項 1:平日停用/DR日啟用 2:平日啟用/DR日啟用(不可與DR墊基(1)同時存在)
     */
    private int releaseType;
}