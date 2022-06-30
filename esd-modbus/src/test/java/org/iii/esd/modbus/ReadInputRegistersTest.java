package org.iii.esd.modbus;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransaction;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransport;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.io.ModbusTransaction;
import com.ghgande.j2mod.modbus.msg.ExceptionResponse;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersResponse;
import com.ghgande.j2mod.modbus.net.ModbusMasterFactory;
import com.ghgande.j2mod.modbus.procimg.InputRegister;

/**
 * FC04
 * 
 *
 */
public class ReadInputRegistersTest {

	private static final Logger logger = LoggerFactory.getLogger(ReadInputRegistersTest.class);

	public static void main(String[] args) {
		Connect connect = new Connect(Protocal.TCP, "127.0.0.1", 1);
		int[] param = new int[] {0, 15, 1};
		
		
		AbstractModbusTransport transport = null;
		ModbusRequest req;
		ModbusTransaction trans;
		int ref = param[0];
		int count = param[1];
		int repeat = param[2];
		int unit = connect.getUnit();

		try {
			try {
				// 2. Open the connection.
				transport = ModbusMasterFactory.createModbusMaster(connect);

				if (transport instanceof ModbusSerialTransport) {
					transport.setTimeout(500);
					if (System.getProperty("com.ghgande.j2mod.modbus.baud") != null) {
						((ModbusSerialTransport) transport)
								.setBaudRate(Integer.parseInt(System.getProperty("com.ghgande.j2mod.modbus.baud")));
					} else {
						((ModbusSerialTransport) transport).setBaudRate(19200);
					}
				}

				// There are a number of devices which won't initialize immediately
				// after being opened. Take a moment to let them come up.
				Thread.sleep(2000);

			} catch (Exception ex) {
				logger.error(ex.getMessage());
				System.exit(1);
			}

			// 5. Execute the transaction repeat times

			for (int k = 0; k < repeat; k++) {
				logger.info("Request {}", k);

				// 3. Create the command.
				req = new ReadInputRegistersRequest(ref, count);
				req.setUnitID(unit);

				// 4. Prepare the transaction
				trans = transport.createTransaction();
				trans.setRequest(req);
				trans.setRetries(1);
				req.setHeadless(trans instanceof ModbusSerialTransaction);

				logger.info("Request: {}", req.getHexMessage());

				if (trans instanceof ModbusSerialTransaction) {
					// 10ms interpacket delay.
					((ModbusSerialTransaction) trans).setTransDelayMS(10);
				}

				if (trans instanceof ModbusTCPTransaction) {
					((ModbusTCPTransaction) trans).setReconnecting(true);
				}

				try {
					trans.execute();
				} catch (ModbusException ex) {
					logger.error(ex.getMessage());
					continue;
				}
				ModbusResponse res = trans.getResponse();
				if (res != null) {
					logger.info("Response: {}", res.getHexMessage());
				} else {
					logger.info("No response to READ INPUT request");
				}

				if (res == null) {
					continue;
				}

				if (res instanceof ExceptionResponse) {
					ExceptionResponse exception = (ExceptionResponse) res;
					logger.error(exception.toString());
					continue;
				}

				if (!(res instanceof ReadInputRegistersResponse)) {
					continue;
				}

				ReadInputRegistersResponse data = (ReadInputRegistersResponse) res;
				InputRegister values[] = data.getRegisters();

				logger.info("Data: {}", Arrays.toString(values));
			}
		} catch (Exception ex) {
			logger.error(ex.toString());
		}

		try {
			// 6. Close the connection
			if (transport != null) {
				transport.close();
			}
		} catch (IOException ex) {
			logger.error(ex.toString());
		}
		System.exit(0);
	}
}
