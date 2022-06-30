package org.iii.esd.thirdparty.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ReceiveServer {

	private int port;

    public ReceiveServer(int port) {
		this.port = port;
	}

	public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
            	final Socket socket = serverSocket.accept();
                new ReceiveThread(socket).start();
            }
//        	Executor executor = Executors.newFixedThreadPool(2);
//          while (true) {
//               Socket socket = serverSocket.accept();
//               executor.execute(new ReceiveThread(socket));
//          }	
        } catch (IOException e) {
        	log.error(e.getMessage());
        }
        
    }

}
