package org.iii.esd.modbus;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;
import com.ghgande.j2mod.modbus.io.ModbusRTUTransport;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransaction;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransport;
import com.ghgande.j2mod.modbus.io.ModbusTransaction;
import com.ghgande.j2mod.modbus.msg.ExceptionResponse;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.net.ModbusMasterFactory;
import com.ghgande.j2mod.modbus.procimg.Register;

/**
 * FC03
 *
 */
public class ReadHoldingRegistersTest {

	private static final Logger logger = LoggerFactory.getLogger(ReadHoldingRegistersTest.class);

	public static void main(String[] args) {
//		Connect connect = new Connect(Protocal.TCP, "127.0.0.1", 1);
//		int[] param = new int[] {0, 15, 1};
		
//		Connect connect = new Connect(Protocal.UDP, "127.0.0.1", 1);
//		int[] param = new int[] {0, 15, 1};
//		
//		Connect connect = new Connect(Protocal.TCP, "60.250.56.120", 1);
//		int[] param = new int[] {40001, 15, 1};
//		
		Connect connect = new Connect(Protocal.UDP, "140.117.88.233", 1);
		int[] param = new int[] {1070, 15, 1};
		
		
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
						((ModbusSerialTransport) transport).setBaudRate(9600);
					}
				}

				// There are a number of devices which won't initialize immediately
				// after being opened. Take a moment to let them come up.
				Thread.sleep(500);
				if (transport instanceof ModbusRTUTransport) {
					String baud = System.getProperty("com.ghgande.j2mod.modbus.baud");
					if (baud != null) {
						((ModbusRTUTransport) transport).setBaudRate(Integer.parseInt(baud));
					}
				}
			} catch (Exception ex) {
				logger.error(ex.toString());
				System.exit(1);
			}

			// 3. Create the command.
			req = new ReadMultipleRegistersRequest(ref, count);
			req.setUnitID(unit);

			// 4. Prepare the transaction
			trans = transport.createTransaction();
			trans.setRequest(req);
			req.setHeadless(trans instanceof ModbusSerialTransaction);

			logger.info("Request: {}", req.getHexMessage());

			// 5. Execute the transaction repeat times

			for (int i = 0; i < repeat; i++) {
				try {
					trans.execute();
				} catch (ModbusException x) {
					logger.error(x.getMessage());
					continue;
				}
				ModbusResponse res = trans.getResponse();
				if (res != null) {
					logger.info("Response: {}", res.getHexMessage());
				} else {
					logger.info("No response to READ HOLDING request");
				}
				if (res instanceof ExceptionResponse) {
					ExceptionResponse exception = (ExceptionResponse) res;
					logger.error(exception.toString());
					continue;
				}

				if (!(res instanceof ReadMultipleRegistersResponse)) {
					continue;
				}

				ReadMultipleRegistersResponse data = (ReadMultipleRegistersResponse) res;
				Register values[] = data.getRegisters();

				logger.info("Data: {}", Arrays.toString(values));
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}

		try {
			// 6. Close the connection
			if (transport != null) {
				transport.close();
			}
		} catch (IOException e) {
			// Do nothing.
		}
		System.exit(0);
	}

}
