package org.iii.esd.modbus;

public enum FunctionCode {
	
	/** FC01 Read DI */
    READ_COILS(1),
    /** FC03 Read AO */
    READ_HOLDING_REGISTERS(3),
    /** FC04 Read AI */
    READ_INPUT_REGISTERS(4),
    /** FC05 Write DI */
    WRITE_COILS(5),    
    /** FC06 Write AO */
    WRITE_SINGLE_REGISTER(6),
    /** FC15 Write DI */
    WRITE_MULTIPLE_COILS(15),   
    /** FC16 Write AO */
    WRITE_MULTIPLE_REGISTER(16)    
    ;

	private int code;
	
	private FunctionCode(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}	

}
