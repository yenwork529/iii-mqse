package org.iii.esd.api.vo.gateway;

import java.util.Date;
import java.util.Map;

import org.iii.esd.api.converter.CustomJsonDateDeserializer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Getter;
import lombok.Setter;

/**
 * TQ或是MD定義 GW資料上傳格式<br/>
 *  ex:<br/>
 *	"value": {<br/>
 *	           "del_total_kvarh": 27052290,<br/>
 *	           "del_total_kwh": 130666140,<br/>
 *	           "inst_kva": 5176,<br/>
 *	           "inst_kvar": 976,<br/>
 *	           "inst_kw": 5038,<br/>
 *	           "inst_pf": 0.973,<br/>
 *	           "line_ab_vol_v": 22774,<br/>
 *	           "line_bc_vol_v": 22808,<br/>
 *	           "line_ca_vol_v": 22790,<br/>
 *	           "phase_a_cur_a": 130.19,<br/>
 *	           "phase_a_vol_v": 0,<br/>
 *	           "phase_b_cur_a": 0,<br/>
 *	           "phase_b_vol_v": 0,<br/>
 *	           "phase_c_cur_a": 132.12,<br/>
 *	           "phase_c_vol_v": 0,<br/>
 *	           "received_date": “2015-03-30T06:56:10.000Z”<br/>
 *	       },<br/>
 *	       "at": "2015-03-30T06:56:00.000Z"<br/>
 *	  }<br/>
 */
@Getter
@Setter
public class ChannelData {

    private Map<String, Object> value;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonDeserialize(using = CustomJsonDateDeserializer.class)
    private Date at;

}