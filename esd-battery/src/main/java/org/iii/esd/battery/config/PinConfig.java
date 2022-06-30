package org.iii.esd.battery.config;

public enum PinConfig {
	
	READ((byte)10),
	WRITE((byte)01),
	READ_WRITE((byte)11),
	;
	
	private Byte rw;
	
	private PinConfig(Byte rw) {
		this.rw = rw;
	}

	public Byte getRw() {
		return rw;
	}

}
