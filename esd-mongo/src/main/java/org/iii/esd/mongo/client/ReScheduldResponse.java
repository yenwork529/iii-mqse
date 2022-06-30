package org.iii.esd.mongo.client;

import java.util.List;

import lombok.Data;

import org.iii.esd.mongo.document.ElectricData;

@Data
public class ReScheduldResponse {
    List<ElectricData> schedules;
}
