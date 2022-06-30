# Use Cases

整個 ESD 的 Use Cases 如下：

![Use Cases](../images/Spinning%20Reserve%20System%20Use%20Case.png)

SR 各項目說明如下

## SR Platform

### Dispatch Command

* Actor: 輔助服務商
* Desc: 輔助服務商可以在 SR 平台上，發送 Ready/Quit 指令給 ThinClient

### Monitoring Loading

* Actor: 輔助服務商
* Desc: 輔助服務商可以在 SR 平台上，監控即時負載情況

### Upload Bidding Record

* Actor: 輔助服務商
* Desc: 輔助服務商可以在 SR 平台上，上傳競標價格

### View Historical Loading

* Actor: 輔助服務商
* Desc: 輔助服務商可以在 SR 平台上，查詢歷史負載資料

### Upload Statistics to TPC

* Actor: 輔助服務商
* Desc: 輔助服務商可以透過 SR 平台，將績效資料發送到 TPC

### Receive SR Commands

* Actor: 輔助服務商
* Desc: 輔助服務商可以藉由 SR 接收 TPC 的調度指令

### Sync Configuration

* Actor: ThinClient
* Desc: ThinClient 可以在 SR 平台上，同步 SR 的設定與指令回來，更新自己的資料庫

## SR ThinClient

## Upload Raw Data

* Actor: GW
* Desc: GW 可以透過 ThinClient，上傳從電表搜集來的資料
