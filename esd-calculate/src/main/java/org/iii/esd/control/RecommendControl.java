/**
 *
 */
package org.iii.esd.control;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 推薦控制量以及相關數值及操作
 *
 * @author iii
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RecommendControl {
    /***
     * 建議M3控制量
     */
    BigDecimal m3kW = BigDecimal.ZERO;
    /***
     * 建議M7控制量
     */
    BigDecimal m7kW = BigDecimal.ZERO;
    /***
     * 所屬15分鐘時段的排程TDMD
     */
    BigDecimal tdmd = BigDecimal.ZERO;
    /***
     * 電池放電即時控制的目標值
     */
    BigDecimal thdc = BigDecimal.ZERO;
    /***
     * 電池保留餘量出現問題，是否需要重新排程
     */
    boolean isSocRsvInvoked = false;

}
