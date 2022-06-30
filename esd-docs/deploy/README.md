# 部署機制說明

本系統的部署，因為主要使用 Docker，故有幾個步驟：

1. 推送上 Docker Registry (trial-web:54320, trial-testing:54320)
2. 在 `esd-java-deploy` 專案對應的分支，以及對應環境的目錄下
3. 執行 `docker-compose pull`
4. 再執行 `docker-compose down && docker-compose up -d` 即可

## Integrate

1. 執行 `docker-push.sh`

## MultiQSE

1. 執行 `docker-push-mqse.sh`

## SILO

1. 執行 `docker-push.sh`

## CHT

1. 執行 `docker-push-cht.sh`

## VPP

1. 執行 `docker-push-vpp.sh`

## 非傳

非傳使用 Windows Service Wrapper 進行部署，因此須參考在非傳主機上的對應 xml 設定，並參閱 Windows Service Wrapper 的 [官方文件](https://github.com/winsw/winsw)
