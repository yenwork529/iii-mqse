package org.iii.esd.modbus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransport;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.io.ModbusTransaction;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ReadCoilsRequest;
import com.ghgande.j2mod.modbus.msg.ReadCoilsResponse;
import com.ghgande.j2mod.modbus.net.ModbusMasterFactory;

/**
 * FC01
 *
 */
public class ReadCoilsTest {
	private static final Logger logger = LoggerFactory.getLogger(ReadCoilsTest.class);

	public static void main(String[] args) {
		
		Connect connect = new Connect(Protocal.TCP, "127.0.0.1", 1);
		int[] param = new int[] {0, 15, 1};
		
		AbstractModbusTransport transport = null;
		ModbusRequest req;
		ModbusTransaction trans;
		ReadCoilsResponse res;
		
		int ref = param[0];
		int count = param[1];
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
				}

				// There are a number of devices which won't initialize immediately
				// after being opened. Take a moment to let them come up.
				Thread.sleep(2000);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
				System.exit(1);
			}

			req = new ReadCoilsRequest(ref, count);
			req.setUnitID(unit);
			logger.info("Request: {}", req.getHexMessage());

			// 4. Prepare the transaction
			trans = transport.createTransaction();
			trans.setRequest(req);

			if (trans instanceof ModbusTCPTransaction) {
				((ModbusTCPTransaction) trans).setReconnecting(true);
			}

			// 5. Execute the transaction repeat times
			int k = 0;
			do {
				trans.execute();
				res = (ReadCoilsResponse) trans.getResponse();
				logger.info("Response: {}", res.getHexMessage());
				logger.info("Digital Inputs Status={}", res.getCoils().toString());
				k++;
			} while (k < repeat);
			transport.close();
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
		System.exit(0);
	}
}
