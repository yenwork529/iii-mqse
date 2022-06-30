package org.iii.esd.thirdparty.socket;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class SendMessage {

	public void send(String address, int port) throws UnknownHostException, IOException {
    	log.debug(Thread.currentThread().getName());
    	log.debug(Thread.activeCount());
    	Socket socket = new Socket(InetAddress.getByName(address), port);
//    	socket.setKeepAlive(true);
//    	log.debug("isConnected:{}",socket.isConnected());
//    	log.debug("getKeepAlive:{}",socket.getKeepAlive());
//    	log.debug("isBound:{}",socket.isBound());
//    	log.debug("isClosed:{}",socket.isClosed());
//    	log.debug("isOutputShutdown:{}",socket.isOutputShutdown());
//    	log.debug("isInputShutdown:{}",socket.isInputShutdown());
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());  
        dos.writeLong(new Date().getTime());
        dos.flush();  
        dos.close();
        socket.close();
	}

}