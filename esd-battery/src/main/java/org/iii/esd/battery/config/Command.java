package org.iii.esd.battery.config;

public enum Command {
	
	CHARGE("charge","setCharge", 1),
	DISCHARGE("discharge","setDischarge", 2),
	STANDBY("standby",null, 0),
	;
	
	private String action;
	
	private String setPower;
	
	private int status;

	private Command(String action, String setPower, int status) {
		this.action = action;
		this.setPower = setPower;
		this.status = status;
	}	

	public String getAction() {
		return action;
	}

	public String getSetPower() {
		return setPower;
	}

	public int getStatus() {
		return status;
	}
	
	public static Command getCommand(int status) {
	    for (Command command : values()) {
	    	if (command.getStatus() == status ) {
	    		return command;
	    	}
	    }
		return null;
	}

	public static Command getCommand(String action) {
	    for (Command command : values()) {
	    	if (command.getAction().equals(action) ) {
	    		return command;
	    	}
	    }
		return null;
	}
	
}