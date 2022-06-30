package org.iii.esd.mongo.document;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import org.iii.esd.enums.PolicyDevice;
import org.iii.esd.enums.PolicyService;
import org.iii.esd.mongo.vo.Level1;
import org.iii.esd.mongo.vo.Level2;
import org.iii.esd.mongo.vo.Level3;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(collection = "PolicyProfile")
public class PolicyProfile extends SequenceDocument {

    /**
     * 策略名稱
     */
    private String name;
    /**
     * 資料更新時間
     */
    private Date updateTime = new Date();
    /**
     * 資料建立時間
     */
    private Date createTime;
    /**
     * 調度模式
     */
    @Field("l1")
    private Level1 dispatching;
    /**
     * 調度應用項目
     */
    @Field("l2")
    private Level2 item;
    /**
     * 運轉參數
     */
    @Field("l3")
    private Level3 param;

    public PolicyProfile(Long id) {
        super(id);
    }

    /**
     * 預設策略(全座) 基本策略要做餘電釋放，比較好看到底程是執行到哪
     */
    public static PolicyProfile Default() {
        PolicyProfile policy = new PolicyProfile();
        Level1 l1 = new Level1(1, 1);
        policy.setDispatching(l1);
        int[][] items = new int[4][6];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 6; j++) {
                items[i][j] = 1;
            }
        }
        Level2 l2 = new Level2(items);
        policy.setItem(l2);
        Level3 l3 = new Level3(1, 2);
        policy.setParam(l3);
        return policy;
    }

    /**
     * 預設策略(銷峰)
     */
    public static PolicyProfile PeakClippingDefault() {
        PolicyProfile policy = new PolicyProfile();
        Level1 l1 = new Level1(1, 1);
        policy.setDispatching(l1);
        int[][] items = new int[4][6];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 6; j++) {
                items[i][j] = 1;
            }
        }
        items[PolicyDevice.ESS.getValue()][PolicyService.C.getValue()] = 0;
        Level2 l2 = new Level2(items);
        policy.setItem(l2);
        Level3 l3 = new Level3(1, 1);
        policy.setParam(l3);
        return policy;
    }

}