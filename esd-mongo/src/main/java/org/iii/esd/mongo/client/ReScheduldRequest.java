package org.iii.esd.mongo.client;

import lombok.Data;

import org.iii.esd.mongo.document.ElectricData;

@Data
public class ReScheduldRequest {
    ElectricData ed;
}
