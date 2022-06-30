package org.iii.esd.thirdparty.config;

public enum TwilioCloudModeEnum {

	BY_URI("U", "Getting an audio file by URI"),
	BY_APP("A", "Getting an audio file by Application-sid"),
	UNDEFINED("X", "Undefined"),
	;
	
	private TwilioCloudModeEnum(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}
	
	public static TwilioCloudModeEnum of(String code) {
		for (TwilioCloudModeEnum entity: values()) {
			if (entity.getCode().equalsIgnoreCase(code))
				return entity;
		}
		return UNDEFINED;
	}
	
	private String code;
	
	private String desc;

	public String getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
}
