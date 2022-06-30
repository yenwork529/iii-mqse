package org.iii.esd.nsysudata.vo.response;

import org.iii.esd.api.response.SuccessfulResponse;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MdResponse extends SuccessfulResponse {
	
    private Data latestValue;

	@Getter
	@Setter
	public static class Data {
		private Value value;
		private String at;

		@Getter
		@Setter
		public static class Value {
			/**
			 * 即時功率(kW)
			 */
			private Double inst_kw;
			/**
			 * 即時虛功率(kVAR)
			 */
			private Double inst_kvar;
			/**
			 * 即時視在功率(kVA)
			 */
			private Double inst_kva;
			/**
			 * 即時功率因數(pf)
			 */
			private Double inst_pf;
			/**
			 * 累計用電量(kWh)
			 */
			private Double del_total_kwh;
			/**
			 * 累計虛功量(kVARh)
			 */
			private Double del_total_kvarh;
			/**
			 * A相電壓(V)
			 */
			private Double phase_a_vol_v;
			/**
			 * B相電壓(V)
			 */
			private Double phase_b_vol_v;
			/**
			 * C相電壓(V)
			 */
			private Double phase_c_vol_v;
			/**
			 * A相電流(A)
			 */
			private Double phase_a_cur_a;
			/**
			 * B相電流(A)
			 */
			private Double phase_b_cur_a;
			/**
			 * C相電流(A)
			 */
			private Double phase_c_cur_a;
			
			private String received_date;
		}
	}
}