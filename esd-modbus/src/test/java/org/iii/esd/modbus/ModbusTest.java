package org.iii.esd.modbus;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.msg.ReadCoilsResponse;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersResponse;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.msg.WriteCoilResponse;
import com.ghgande.j2mod.modbus.msg.WriteMultipleCoilsResponse;
import com.ghgande.j2mod.modbus.msg.WriteMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.msg.WriteSingleRegisterResponse;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.util.BitVector;

import lombok.extern.log4j.Log4j2;

//@RunWith(SpringRunner.class)
@SpringBootTest(classes=ModbusMasterService.class)
@Log4j2
public class ModbusTest {

	@Autowired
	private ModbusMasterService modbusMasterService;

	@Test
	public void ReadHoldingRegistersTest() {
		Connect connect = new Connect(Protocal.TCP, "127.0.0.1", 1);
		ModbusResponse res = modbusMasterService.sendCommand(connect, FunctionCode.READ_HOLDING_REGISTERS, 
				0, 15, 2);
		ReadMultipleRegistersResponse data = (ReadMultipleRegistersResponse) res;
		InputRegister values[] = data.getRegisters();
		log.info("Data: {}", Arrays.toString(values));
	}
	
	@Test
	public void ReadInputRegistersTest() {
		Connect connect = new Connect(Protocal.TCP, "127.0.0.1", 1);
		ModbusResponse res = modbusMasterService.sendCommand(connect, FunctionCode.READ_INPUT_REGISTERS, 
				0, 15, 1);
		ReadInputRegistersResponse data = (ReadInputRegistersResponse) res;
		InputRegister[] values = data.getRegisters();
		log.info("Data: {}", Arrays.toString(values));
	}
	
	@Test
	public void ReadCoilsTest() {
		Connect connect = new Connect(Protocal.TCP, "127.0.0.1", 1);
		ModbusResponse res = modbusMasterService.sendCommand(connect, FunctionCode.READ_COILS, 
				0, 15, 1);
		ReadCoilsResponse data = (ReadCoilsResponse) res;
		BitVector values = data.getCoils();
		log.info("Data: {}", Arrays.toString(values.getBytes()));
		log.info("Data: {}", values.toString());
	}
	
	@Test
	public void WriteCoilsTest() {
		Connect connect = new Connect(Protocal.TCP, "127.0.0.1", 1);
		ModbusResponse res = modbusMasterService.sendCommand(connect, FunctionCode.WRITE_COILS, 
				6, null, 1, 1);
		WriteCoilResponse data = (WriteCoilResponse) res;
		boolean values = data.getCoil();
		log.info("Data: {}", values);
	}

	@Test
	public void WriteHoldingRegisterTest() {
		Connect connect = new Connect(Protocal.TCP, "127.0.0.1", 1);
		ModbusResponse res = modbusMasterService.sendCommand(connect, FunctionCode.WRITE_SINGLE_REGISTER, 
				6, null, 1, 100);
		WriteSingleRegisterResponse data = (WriteSingleRegisterResponse) res;
		byte[] values = data.getMessage();
		log.info("Data: {}", Arrays.toString(values));
		log.info("ref: {}", data.getReference());
		log.info("value: {}", data.getRegisterValue());
	}
	
	@Test
	public void WriteMultipleCoilsTest() {
		Connect connect = new Connect(Protocal.TCP, "127.0.0.1", 1);
		ModbusResponse res = modbusMasterService.sendCommand(connect, FunctionCode.WRITE_MULTIPLE_COILS, 
				10, null, 1, new Integer[] {1,0,1,1,1,0});
		WriteMultipleCoilsResponse data = (WriteMultipleCoilsResponse) res;
		byte[] values = data.getMessage();
		log.info("BitCount: {}", data.getBitCount());
		log.info("Data: {}", Arrays.toString(values));
		log.info("ref: {}", data.getReference());
	}		
	
	@Test
	public void WriteMultipleHoldingRegisterTest() {
		Connect connect = new Connect(Protocal.TCP, "127.0.0.1", 1);
		ModbusResponse res = modbusMasterService.sendCommand(connect, FunctionCode.WRITE_MULTIPLE_REGISTER, 
				1, null, 1, new Integer[] {1,200,3000,4,50});
		WriteMultipleRegistersResponse data = (WriteMultipleRegistersResponse) res;
		byte[] values = data.getMessage();
		log.info("Data: {}", Arrays.toString(values));
		log.info("ref: {}", data.getReference());
		log.info("count: {}", data.getWordCount());
	}	
	
	public static void main(String[] args) {
        ModbusTCPMaster master = new ModbusTCPMaster("60.250.56.120", 502, 1000, true, false);
        try {
            master.connect();
            for (int i = 0; i < 1; i++) {
                int ref = 40001;
                // Function code 03
                Register[] regs = master.readMultipleRegisters(1, ref, 30);
                for (Register reg : regs) {
                	log.info("Reg: {} Val: {}", ref+1, reg.getValue());
                    ref++;
                }
            }
        } catch (Exception e) {
        	log.error(e.getMessage());
        } finally {
            master.disconnect();
        }
	}

}