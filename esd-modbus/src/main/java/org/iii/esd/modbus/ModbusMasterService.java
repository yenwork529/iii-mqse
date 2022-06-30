package org.iii.esd.modbus;

import java.io.IOException;

import org.iii.esd.exception.ConnectionFailedException;
import org.iii.esd.exception.Error;
import org.springframework.stereotype.Service;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;
import com.ghgande.j2mod.modbus.io.ModbusRTUTransport;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransaction;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransport;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.io.ModbusTransaction;
import com.ghgande.j2mod.modbus.msg.ExceptionResponse;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.msg.ReadCoilsRequest;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.WriteCoilRequest;
import com.ghgande.j2mod.modbus.msg.WriteMultipleCoilsRequest;
import com.ghgande.j2mod.modbus.msg.WriteMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.WriteSingleRegisterRequest;
import com.ghgande.j2mod.modbus.net.ModbusMasterFactory;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.BitVector;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ModbusMasterService {

	/**
	 * 
	 * @param connect
	 * @param fc
	 * @param ref 起始腳位
	 * @param count	讀取個數 FC01 FC03 FC04 必填
	 * @param value	寫入數值 FC05 FC06 FC15 FC16 必填
	 * @param repeat	
	 * @return ModbusResponse
	 */
	public ModbusResponse sendCommand(Connect connect, FunctionCode fc, int ref, Integer count, int repeat, Integer... value) 
			throws ConnectionFailedException {
		AbstractModbusTransport transport = null;
		ModbusResponse res = null;
		try {
			transport = getTransport(connect);
			if(transport!=null) {
				try {
					ModbusRequest req = getRequest(fc, ref, count, value);
					req.setUnitID(connect.getUnit());

					for (int i = 0; i < repeat; i++) {
						ModbusTransaction trans = getTransaction(transport, req);
						try {
							trans.execute();
						} catch (ModbusException ex) {
							log.error(ex.getMessage());
							if(i==repeat-1) {
								throw new ConnectionFailedException(Error.connectionFailed);
							}
							continue;
						}
						res = trans.getResponse();
						if (res instanceof ExceptionResponse) {
							ExceptionResponse exception = (ExceptionResponse) res;
							log.error(exception.toString());
							continue;
						}
						if (res != null) {
							log.debug("Response: {}", res.getHexMessage());
							break;
						} else {
							log.warn("No Response to {} request", fc.name());
						}
					}					
				} catch (Exception ex) {
					log.error(ex.getClass()+ " "+ ex.getMessage());
					throw new ConnectionFailedException(Error.connectionFailed);
					//System.exit(1);
				}				
			}else {
				throw new ConnectionFailedException(Error.connectionFailed);
			}
		} catch (InterruptedException ex) {
			log.error(ex.getMessage());
			throw new ConnectionFailedException(Error.connectionFailed);
		} finally {
			try {
				if (transport != null) {
					transport.close();
				}
			} catch (IOException ex) {
				log.error(ex.getMessage());
			}
		}
		return res;
	}

	private AbstractModbusTransport getTransport(Connect connect) throws InterruptedException {
		AbstractModbusTransport transport = ModbusMasterFactory.createModbusMaster(connect);
		if (transport instanceof ModbusSerialTransport) {
			transport.setTimeout(500);
			if (System.getProperty("com.ghgande.j2mod.modbus.baud") != null) {
				((ModbusSerialTransport) transport)
						.setBaudRate(Integer.parseInt(System.getProperty("com.ghgande.j2mod.modbus.baud")));
			} else {
				((ModbusSerialTransport) transport).setBaudRate(19200);
			}

			// Some serial devices are slow to wake up.
			Thread.sleep(2000);
		} 
		if (transport instanceof ModbusRTUTransport) {
			String baud = System.getProperty("com.ghgande.j2mod.modbus.baud");
			if (baud != null) {
				((ModbusRTUTransport) transport).setBaudRate(Integer.parseInt(baud));
			}
		}
		return transport;
	}
	
	private ModbusRequest getRequest(FunctionCode fc, int ref, Integer count, Integer... value) {
		switch (fc) {
			case READ_COILS:
				return new ReadCoilsRequest(ref, count);
			case READ_HOLDING_REGISTERS:
				return new ReadMultipleRegistersRequest(ref, count);
			case READ_INPUT_REGISTERS:
				return new ReadInputRegistersRequest(ref, count);
			case WRITE_COILS:
				return new WriteCoilRequest(ref, value[0] != 0);
			case WRITE_SINGLE_REGISTER:
				return new WriteSingleRegisterRequest(ref, new SimpleRegister(value[0]));
			case WRITE_MULTIPLE_COILS:
				BitVector bitVector = new BitVector(value.length);
				for (int i = 0; i < value.length; i++) {
					bitVector.setBit(i, value[i] != 0);
				}
				return new WriteMultipleCoilsRequest(ref, bitVector);
			case WRITE_MULTIPLE_REGISTER:
				Register[] regs = new Register[value.length];
				for (int i = 0; i < value.length; i++) {
					regs[i] = new SimpleRegister(value[i]);
				}
				return new WriteMultipleRegistersRequest(ref, regs);
			default:
				log.warn("Function Code is undefined");
				return null;
		}
	}

	private ModbusTransaction getTransaction(AbstractModbusTransport transport, ModbusRequest req) {
		ModbusTransaction trans = transport.createTransaction();
		trans.setRequest(req);
		trans.setRetries(1);
		req.setHeadless(trans instanceof ModbusSerialTransaction);
		log.debug("Request: {}", req.getHexMessage());
		if (trans instanceof ModbusSerialTransaction) {
			// 10ms interpacket delay.
			((ModbusSerialTransaction) trans).setTransDelayMS(10);
		}		
		if (trans instanceof ModbusTCPTransaction) {
			((ModbusTCPTransaction) trans).setReconnecting(true);
		}		
		return trans;
	}

}