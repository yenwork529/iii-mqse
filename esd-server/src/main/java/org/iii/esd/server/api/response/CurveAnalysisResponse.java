package org.iii.esd.server.api.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.server.api.vo.CurveModel;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurveAnalysisResponse extends SuccessfulResponse {

    private List<CurveModel> series;

}