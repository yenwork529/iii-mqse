package org.iii.esd.mongo.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 註冊用PAYLOAD
 *
 * @author iii
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RegisterRequest {
    long fieldId;
}
