package org.iii.esd.modbus;

import com.ghgande.j2mod.modbus.Modbus;

import lombok.Data;

@Data
public class Connect {
	
	private Protocal protocal;
	private String ip;
	private int port = Modbus.DEFAULT_PORT;
	private int unit = 0;
	private int repeat = 1;
	
	public Connect(Protocal protocal, String ip, int unit) {
		this.protocal= protocal;
		this.ip = ip;
		this.unit = unit;
	}

	public Connect(Protocal protocal, String ip, int port, int unit) {
		this(protocal, ip, unit);
		this.port = port;
	}	

	public Connect(Protocal protocal, String ip, int port, int unit, int repeat) {
		this(protocal, ip, port, unit);
		this.repeat = repeat;
	}

}