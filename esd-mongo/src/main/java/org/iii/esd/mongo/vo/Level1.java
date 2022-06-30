package org.iii.esd.mongo.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Level1 {
    /**
     * 調度模式(1:個別調度 2:聚合調度)
     */
    private int mode;
    /**
     * 調度日(1:非例假日(不含周六) 2:非例假日+周六)
     */
    private int dayType;
}