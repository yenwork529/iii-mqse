package org.iii.esd.api.response.esd.system;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.api.vo.Name;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaipowerListResponse extends SuccessfulResponse {

    private List<Name> afclist;

    private List<Name> suplist;

    private List<Name> srlist;

    private List<Name> dregList;

    private List<Name> sregList;

    private List<Name> edregList;
}