### 2021-1102 Discussion for Bid Calculation

* Goal
    * BidResBid
    * BidTxgBid

* BidResBid
    * created by ?
    * serviceFactor
    * capacityRevenue
    * efficacyRevenue
    * energyRevenue

* First, do
* Res.ServiceFactor

from SR event dispatcher

java
```
public void scanTypeADoneRevenue(SpinReserveProfile sr, DispatchEvent event) {
if (isStopped(event)) {
log.info("cal revenue now: {}", now);
trialDispatchService.calculateRevenue(sr, event);
}
}
```
調度結束後，Dispatcher 準備好 BidRes/TxgBid and BidRes/TxgData，呼叫


java
```
public Boolean energyRevenueCalculate(Long srId, Date startTime, Date endTime) {

        Boolean flag = false;

        //時段內srId的EnergyRevenue計算
        List<SpinReserveData> spinReserveDataList = this.getEnergyRevenue(srId,
                spinReserveService.findSpinReserveDataBySrIdAndNoticeTypeAndNoticeTime(srId, NoticeType.UNLOAD, startTime, endTime));

        if (spinReserveDataList != null && spinReserveDataList.size() > 0) {
            //Save spinReserveDataList
            spinReserveService.saveSpinReserveData(spinReserveDataList);
        }

        return flag;
    }
```
建議從 energyRevenueCalculate 開始，分成四種 resource。先做 DR。
但是要區分 Aux.Service (SR,SUP)

java
e.g.
```calcDrEngergyRevenue(txgProfile, from, to), or
calcCgenEngergyRevenue(txgProfile, from, to)

// only for UNLOAD type
integrateDataService.seekBidResBidBetween(resId, start, stop);
```


* 先從 SpinReserverData，抓取區間資料 w/ UNLOAD
  from SpinReserveData, get getEnergyPrice, getBaseline, getClipKW

java
```
List<ElectricData> electricDataList =
statisticsService.findElectricDataByFieldIdAndDataTypeAndTime(fieldID, DataType.T99, timestampStart, timestampEnd);
```
取得 electricalData by

java
```
List<DrResData> lst = loadResData(resProfile, from, to);
```
java
```fieldList.(fieldClippedKW);
//field baseline
fieldBaseLine = fieldList.getBaseline();
fieldList.setClippedKW(fieldClippedKW);

                //統計SRID執行期間平均達成容量
                srClippedKW = srClippedKW.add(fieldClippedKW);
            }

            //計算執行達成容量 srId clippedKW
            srClippedKW = srBaseLine.subtract(srLoadKW);
```
* 產出
java
```/**
* 電能費(元/ kWh)
  */
  private BigDecimal energyPrice;

/**
* 執行每分鐘平均降載容量
  */
  private BigDecimal clippedKW;
```
* 欄位來源表

BidTxgBid (UI要用，UI不可改寫這些欄位)

Name | Type | Owner(由哪裡產生) | Remark
---|---|---|---
serviceFactor<p>服務品質指標(%)|Big|Calc|case sr:if (txgData.getRevenueFactor().compareTo(new BigDecimal(95)) >= 0) {serviceFactor = new BigDecimal(100)
capacityRevenue<p>容量費用|Big|Calc|capacityRevenue = awardedCapacity.multiply(capacityPrice)
efficacyRevenue<p>效能費|Big|Calc|//場域的服務指標 以TXG的服務指標 進行效能費計算<p>listEfficacyRevenue = temp.multiply(serviceFactor.divide(new BigDecimal(100), 3, BigDecimal.ROUND_HALF_UP));
energyRevenue<p>電能費|Big|Calc|//場域的服務指標 以TXG的服務指標 進行效能費計算<p>resEnergyRevenue = temp.multiply(serviceFactor.divide(new BigDecimal(100), 3, BigDecimal.ROUND_HALF_UP))
serviceEnergy<p>提供電能量<p>**確認內容**|Big|Calc| //fieldEnergyCBLT51 -(fieldEnergyMeterT5-fieldEnergyMeterT1)*accFactor (kWh)<p>fieldServiceEnergy = fieldEnergyCBLT51.subtract(fieldAccFactor.multiply(fieldEnergyMeterT5.subtract(fieldEnergyMeterT1)).setScale(1, BigDecimal.ROUND_HALF_UP));

BidTxgInfo (UI要用，UI可改寫這些欄位)

Name | Type | Owner  | Remark
---|---|---|---|
awardedCapacity<p>得標容量| big | UI(RW)|
operatorStatus<p>運轉狀態 | int | UI(RW) |
ppaEnergyPrice<p>尖峰保證容量電能費價格(NT$/MWh) | big | ? |
ppaCapacity<p>尖峰保證容量(MW) | big | UI |
energyPrice<p>電能費報價(NT$/MWh) | big | UI(RW) |
capacity<p>可調度容量(MW) | big |  UI(RW)|
price<p>容量費單價(NT/MW) | big | UI(RW) |

BidTxgData (由 Dispatcher 產生，再由 Calc. 完成)

Name | Type | Owner | Remark
---|---|---|---
clipKW<p>卸載量(得標量)|Integer|Dispatcher |台電指令|
baseline<p>基準線(通知前5分鐘平均需量)|Big|Dispatcher|當下計算前五分鐘平均值
clippedKW<p>執行每分鐘平均降載容量|Big|Calc|fieldClippedKW = fieldAccActivePower.divide(activePowerNum, 1, BigDecimal.ROUND_HALF_UP)
revenueFactor<p>每分鐘平均執行率(%)|Big|calc|revenueFactor = revenueFactor.multiply(BigDecimal.valueOf(100))
serviceEnergy<P>服務提供之電能量|Big|calc.|txgServiceEnergy = txgEnergyCBLT51.subtract(txgAccFactor.multiply(txgEnergyMeterT5.subtract(txgEnergyMeterT1)).setScale(1, BigDecimal.ROUND_HALF_UP));
performanceEnergy<p>執行率期間提供之電能量|Big|calc.|fieldPerformanceEnergy = fieldEnergyCBLT32.subtract(fieldAccFactor.multiply(fieldEnergyMeterT3.subtract(fieldEnergyMeterT2)).setScale(1, BigDecimal.ROUND_HALF_UP));

BidResBid

Name | Type | Owner | Remark
---|---|---|---
serviceFactor<p>服務品質指標(%)|Big||
capacityRevenue<p>容量費用|Big||
efficacyRevenue<p>效能費 (NT)|Big||
energyRevenue<p>電能費|Big||
serviceEnergy<p>提供電能量(kWh-小時)|Big||



BidResInfo

Name | Type | Owner | Remark
---|---|---|---
awardedCapacity<p>得標容量(MW)|Big||
ppaCapacity<p>尖峰保證容量(MW)|Big||
capacity<p>可調度容量(MW)|Big||

BidResData

Name | Type | Owner | Remark
---|---|---|---
clipKW<p>卸載量(得標量)|Integer||
baseline<p>基準線(通知前5分鐘平均需量)|Big||
clippedKW<p>執行每分鐘平均降載容量|Big||
revenueFactor<p>每分鐘平均執行率(%)|Big||
serviceEnergy<p>服務提供之電能量(kWh)|Big||
performanceEnergy<p>執行率期間提供之電能量(kWh)|Big||
