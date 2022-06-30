# Modules

本系統的模組考下圖：

![ESD Packages](../images/ESD%20Package.png)

各模組說明如下：

## 服務模組

### esd-server

* 說明：SR server 的後端服務
* 執行形式：web server & cron jobs
* port：58001

### esd-auth

* 說明：SR server 的登入服務
* 執行形式：web server & cron jobs
* port：57000

### esd-client

* 說明：SR ThinClient 服務
* 執行形式：web server & cron jobs
* port：8010

### esd-client-afc

* 說明：AFC ThinClient 服務
* 執行形式：web server & cron jobs
* port：58015

### esd-monitor

* 說明：watchdog 服務
* 執行形式：web server & cron jobs
* port：58030

### esd-nsysudata

* 說明：收取中山資料的服務
* 執行形式：cron jobs
* port：58020

## 功能模組

### esd-common

* 說明：全網站共用常數和Exception

### esd-common-api

* 說明：主要提供esd-client，esd-server和esd-auth模組API相關物件

### esd-jwt

* 說明：提供 jwt 相關的建立、剖析等功能

### esd-thirdparty

* 說明：提供line, jandi, 氣象局, soda等外部網站API介接

### esd-mongo

* 說明：提供資料庫存取與 domain definition

### esd-calculate

* 說明：計算場域最佳契約容量、效益等

### esd-forecast

* 說明：負載預測，PV預測

### esd-modbus

* 說明：低階 modbus 通訊，透過 TCP/RUP 等機制介接

### esd-battery

* 說明：提供與電池(儲能模組)的連接、呼叫機制
