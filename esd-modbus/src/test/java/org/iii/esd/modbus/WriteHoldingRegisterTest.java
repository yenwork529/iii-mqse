package org.iii.esd.modbus;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;
import com.ghgande.j2mod.modbus.io.ModbusRTUTransport;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransaction;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransport;
import com.ghgande.j2mod.modbus.io.ModbusTransaction;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.WriteSingleRegisterRequest;
import com.ghgande.j2mod.modbus.net.ModbusMasterFactory;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;

/**
 * 
 * FC06
 *
 */
public class WriteHoldingRegisterTest {

	private static final Logger logger = LoggerFactory.getLogger(WriteHoldingRegisterTest.class);

	public static void main(String[] args) {
		
		Connect connect = new Connect(Protocal.TCP, "127.0.0.1", 1);
		int[] param = new int[] {3, 0x3E8, 1};

		AbstractModbusTransport transport = null;
		ModbusRequest req;
		ModbusTransaction trans;		

		int ref = param[0];
		int value = param[1];
		int repeat = param[2];
		int unit = connect.getUnit();

		try {
			try {
				transport = ModbusMasterFactory.createModbusMaster(connect);

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
			} catch (Exception ex) {
				logger.error(ex.getMessage());
				System.exit(1);
			}

			req = new WriteSingleRegisterRequest(ref, new SimpleRegister(value));

			req.setUnitID(unit);

			// 3. Prepare the transaction
			trans = transport.createTransaction();
			trans.setRequest(req);
			req.setHeadless(trans instanceof ModbusSerialTransaction);
			logger.info("Request: {}", req.getHexMessage());
			
			// 4. Execute the transaction repeat times
			for (int count = 0; count < repeat; count++) {
				trans.execute();
				logger.info("Response: {}", trans.getResponse().getHexMessage());
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		} finally {
			try {
				if (transport != null) {
					transport.close();
				}
			} catch (IOException ex) {
				logger.error(ex.getMessage());
			}
		}
		System.exit(0);
	}

}
