package org.iii.esd.modbus;

import java.math.BigDecimal;

import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.procimg.Register;

public class Test {

	public static void main(String[] args) {
		// java -jar  C:\esd-modbus-2.0.0.jar 192.168.1.3 16384
		// java -jar  C:\esd-modbus-2.0.0.jar 192.168.1.3 16418
		// java -jar  C:\esd-modbus-2.0.0.jar org.iii.esd.modbus.Test 192.168.1.3 16418
		Connect connect = new Connect(Protocal.TCP, args[0], 502, 1);
		if(args.length > 3) {
			cal(connect, Integer.valueOf(args[1]));
		}else {
			calTwoWords(connect, Integer.valueOf(args[1]));			
		}
	}
	
	private static void cal(Connect connect, int pin) {
		ModbusMasterService mms= new ModbusMasterService();
		ModbusResponse res = mms.sendCommand(connect, FunctionCode.READ_HOLDING_REGISTERS, pin, 1, 2);
		ReadMultipleRegistersResponse data = (ReadMultipleRegistersResponse) res;
		Register values[] = data.getRegisters();
		if(values!=null && values.length>0) {
			Register register = values[0];
			System.out.printf("short:%s%n",new BigDecimal(register.toShort()));
			System.out.printf("value:%s%n",new BigDecimal(register.getValue()));
		}	
	}
	
	private static void calTwoWords(Connect connect, int pin) {
		ModbusMasterService mms= new ModbusMasterService();
		ModbusResponse res = mms.sendCommand(connect, FunctionCode.READ_HOLDING_REGISTERS, pin, 2, 2);
		ReadMultipleRegistersResponse data = (ReadMultipleRegistersResponse) res;
		Register values[] = data.getRegisters();
		if(values!=null && values.length>0) {
			for (int i = 0; i < values.length; i++) {
				Register register = values[i];
				System.out.printf("short(%s):%s%n",i,new BigDecimal(register.toShort()));
				System.out.printf("value(%s):%s%n",i,new BigDecimal(register.getValue()));
			}
			BigDecimal twoWordsValue = calTwoWordsValue(values[0].getValue(), values[1].getValue());
			System.out.printf("twoWordsValue:%s%n",twoWordsValue);
			System.out.printf("ieee754:%s%n",ieee754(twoWordsValue));
		}	
	}
	
	public static BigDecimal calTwoWordsValue(int high, int low) {
		return new BigDecimal(2).pow(16).multiply(new BigDecimal(high)).add(new BigDecimal(low));
	}

	public static BigDecimal ieee754(BigDecimal value) {
		return new BigDecimal(""+Float.intBitsToFloat(value.intValue()));
	}

}
