package org.iii.esd.server.api.request;

import java.util.Date;
import java.util.HashMap;

import lombok.Data;

// 前置排程 & 歷史資料查詢 & 效益計算使用MODEL
@Data
public class ScheduleModelResquest {
    // 起始時間
    public Date start;
    // 結束時間
    public Date end;
    // 場域ID
    public long fieldId;
    // 原始資料
    public int data_type;
    // 調度資料
    public int sdata_type;
    // 曲線類型
    public int curve_type;
    // 一周設定，哪幾日
    public boolean[] weeks;
    // 排程是否完成
    public boolean complete;
    // 是否啟用自放電補償
    public boolean enabled_comp_kw;
    // 是否啟用效率補償
    public boolean enabled_comp_ef;
    // 是否重置可控設備
    public boolean reset_m6;
    // 1: 基本電費
    // 2: 尖峰/半尖峰/DR
    // 3: 離峰
    public int[] ratio_type;
    //  畫圖用
    public int max_size;
    // 是否要初始化
    public boolean init;
    // 躉售價格
    public double wholesale;
    // 畫圖用，參考曲線
    public HashMap<String, HashMap<String, Boolean>> ref_curve;
    // 炭交易價格
    public double carbon_trading_prices;
    // 炭排量
    public double carbon_emissions;
    // 尖峰降低價格
    public double peak_load_reduce_price;
    // 計算維護成本
    // 1 是 PV
    // 2 是 ESS
    public int[] maintain;
    // 計算ESS循環成本
    public boolean use_circle_cost;
    // 契約容量費率遞進值 10 20 30 
    public double bc_ratio;
}