package com.ghgande.j2mod.modbus.net;

import static org.iii.esd.modbus.Protocal.RTU;
import static org.iii.esd.modbus.Protocal.TCP;
import static org.iii.esd.modbus.Protocal.UDP;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.iii.esd.modbus.Connect;

import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;
import com.ghgande.j2mod.modbus.io.ModbusRTUTransport;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransport;
import com.ghgande.j2mod.modbus.util.SerialParameters;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ModbusMasterFactory {

	public static AbstractModbusTransport createModbusMaster(Connect connect,
			AbstractSerialConnection serialConnection) {
		return createCustomModbusMaster(connect, serialConnection);
	}

	public static AbstractModbusTransport createModbusMaster(Connect connect) {
		return createCustomModbusMaster(connect, null);
	}

	private static AbstractModbusTransport createCustomModbusMaster(Connect connect,
			AbstractSerialConnection serialConnection) {
		//String parts[] = address.split(" *: *");
		if (connect==null) {
			throw new IllegalArgumentException("missing connection information");
		}

		if (RTU.equals(connect.getProtocal())) {
			/*
			 * Create a ModbusSerialListener with the default Modbus values of 19200 baud,
			 * no parity, using the specified device. If there is an additional part after
			 * the device name, it will be used as the Modbus unit number.
			 */
			SerialParameters parms = new SerialParameters();
			parms.setPortName(connect.getIp());
			parms.setBaudRate(9600);
			parms.setDatabits(8);
			parms.setParity(AbstractSerialConnection.NO_PARITY);
			parms.setStopbits(1);
			parms.setFlowControlIn(AbstractSerialConnection.FLOW_CONTROL_DISABLED);
			parms.setEcho(false);
			try {
				ModbusRTUTransport transport = new ModbusRTUTransport();
				if (serialConnection == null) {
					transport.setCommPort(SerialConnection.getCommPort(parms.getPortName()));
				} else {
					transport.setCommPort(serialConnection);
				}
				transport.setEcho(false);
				return transport;
			} catch (IOException ex) {
				log.error(ex.toString());
				return null;
			}
		} else if (TCP.equals(connect.getProtocal())) {
			/*
			 * Create a ModbusTCPListener with the default interface value. The second
			 * optional value is the TCP port number and the third optional value is the
			 * Modbus unit number.
			 */
			try {
				Socket socket = new Socket(connect.getIp(), connect.getPort());
				log.debug("connecting to {}", socket.toString());
				return new ModbusTCPTransport(socket);
			} catch (UnknownHostException ex) {
				log.error(ex.toString());
				return null;
			} catch (IOException ex) {
				log.error(ex.toString());
				return null;
			}
		} else if (UDP.equals(connect.getProtocal())) {
			/*
			 * Create a ModbusUDPListener with the default interface value. The second
			 * optional value is the TCP port number and the third optional value is the
			 * Modbus unit number.
			 */
			UDPMasterTerminal terminal;
			try {
				terminal = new UDPMasterTerminal(InetAddress.getByName(connect.getIp()));
				terminal.setPort(connect.getPort());
				terminal.activate();
			} catch (UnknownHostException ex) {
				log.error(ex.toString());
				return null;
			} catch (Exception ex) {
				log.error(ex.toString());
				return null;
			}
			return terminal.getTransport();
		} else {
			throw new IllegalArgumentException("unknown type " + connect.getIp());
		}
	}
}