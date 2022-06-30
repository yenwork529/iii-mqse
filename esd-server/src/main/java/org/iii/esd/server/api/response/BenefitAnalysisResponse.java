package org.iii.esd.server.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.benefit.BenefitSumResult;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BenefitAnalysisResponse extends SuccessfulResponse {

    private BenefitSumResult benefit;

}