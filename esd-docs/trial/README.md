# 試行平台調度執行流程

本文件說明新版調度平台與台電試行平台介接的執行流程，依序說明

1. 既有流程
2. 新版流程

DNP3 的 API 請參考 outstation 文件。

## 既有流程

既有調度流程的流程圖如下

![Notice Flow](../images/Notice%20Flow.png)

## 新版流程

新版調度平台流程共有三項，各項參考下表連結

 行為 | 狀態 | 說明
 ---- | ---- | ----
調度 | 自動 | [自動調度執行](./auto_notice/README.md)
調度 | 手動 | [手動調度執行](./manual_notice/README.md)
告警 |  - | [告警執行](./alert/README.md)
