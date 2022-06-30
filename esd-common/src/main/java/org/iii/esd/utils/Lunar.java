package org.iii.esd.utils;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Lunar implements Serializable {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1602338350199825098L;

    /**
     * 年.
     */
    private int year;

    /**
     * 月.
     */
    private int month;

    /**
     * 日.
     */
    private int day;

    /**
     * 該月是否為閏月.
     */
    private boolean leap;

}