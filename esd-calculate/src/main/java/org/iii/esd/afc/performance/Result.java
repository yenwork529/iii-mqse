package org.iii.esd.afc.performance;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.iii.esd.api.response.SuccessfulResponse;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Result extends SuccessfulResponse {

    private int count;

    private List<Sbspm> sbspmList;

    private List<Spm> spmList;

    private List<Aspm> aspmList;
}
