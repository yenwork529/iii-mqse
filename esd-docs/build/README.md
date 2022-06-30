# 建置機制說明

## 基本建置

1. 確認系統 `JAVA_HOME` 設定
2. 確認系統 Maven 安裝
3. 執行 `mvn clean package -DskipTests`

## Docker image 建置指令

### Intergate 版

1. 執行 `docker-build-all.sh`

### MultiQSE 版

1. 執行 `docker-build-mqse.sh`

待 Integrate 版完全升級至 MultiQSE 版後，不再需要此操作

### CHT 版

1. 執行 `docker-build-cht.sh`

### VPP 版

1. 執行 `docker-build-vpp.sh`

### SILO 版

1. 執行 `docker-build-silo.sh`

### 測試用本機版

1. 執行 `local-build-service.sh server` 或 `local-build-service.sh auth`

