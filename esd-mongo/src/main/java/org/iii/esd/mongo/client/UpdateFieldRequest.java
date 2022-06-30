package org.iii.esd.mongo.client;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class UpdateFieldRequest {

    /**
     * 強制給資料，可能場域重新開機什麼都沒有，需要Server全部重新提供，
     * 或者可以分種類設定flag強制下載
     */
    boolean force = false;
}
