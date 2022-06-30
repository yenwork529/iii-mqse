# HowTos

## HowTo 1: 建置新用戶與場域

建置新用戶與場域，流程可參考下圖：

![Create New Company and Field](../images/Create%20Company%20and%20Field%20Activity.png)

流程說明如下：

### Create Company

建立公司的方法，是直接在資料庫 (ESD) 的 Company collection 直接新增一筆即可。

### Create SR Profile

建立 SR Profile，可以在 SR 平台上進行，或透過 API 的方式呼叫。如此一來，程式會自動建立新的 seq，
就不須自己手動建立。

### Create Field Profile

建立場域 Profile，目前沒有 UI，必須透過 API 來操作

### Install ThinClient in local

在公司、場域端安裝 ThinClient 及其資料庫

### Create Field Profile at ThinClient

在 ThinClient 端建立場域 Profile

### Send DeviceProfile from GW

等待 GW 將 DeviceProfile 傳送上來

### Update DeviceProfile with FieldID

在 ThinClient 與 Server 都要更新 DeviceProfile collection 中的 FieldID 值：

```json
{
    "fieldId" : DBRef("FieldProfile", NumberLong(13)),
    "enableStatus" : "enable",
}
```

## HowTo 2: 故障期間資料補值

故障補值有兩個情況，一個是從 ThinClient 送值上 server，一個是從 server 送值上 TPC，請參考 Postman 的兩項 requests：

1. POST for send fix to TPC (ESD Management)
2. POST for fixdata (ESD ThinClient)

