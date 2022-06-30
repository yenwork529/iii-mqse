package org.iii.esd.server.api.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.iii.esd.api.response.SuccessfulResponse;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpinReserveResponse extends SuccessfulResponse {

    private Long srId;
    /**
     * 負載功率瞬時值總和
     */
    private double totalActivePower;
    /**
     * 參與者所轄各用戶計量表個別負載功率
     */
    private List<Double> activePowerList;
    /**
     * 1分鐘負載千瓦時總和
     */
    private double totalKW;
    /**
     * 參與者所轄各用戶計量表個別1分鐘負載千瓦時
     */
    private List<Double> kWList;

}