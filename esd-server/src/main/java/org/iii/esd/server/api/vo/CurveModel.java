package org.iii.esd.server.api.vo;

import java.util.List;

import lombok.Data;

@Data
public class CurveModel {

    private String name;

    private List<CurveModelDetail> data;
}
