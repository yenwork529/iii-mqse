package org.iii.esd.thirdparty.socket;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;

import org.iii.esd.Constants;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ReceiveThread extends Thread {

	private Socket socket;
	 
    public ReceiveThread(Socket socket) {
        this.socket = socket;
    }
   
    public void run() {
		try {
			// TimeOut時間
			socket.setSoTimeout(5000);
//			log.info(socket.getRemoteSocketAddress());
//			socket.setKeepAlive(true);
//			log.info("S isConnected:{}", socket.isConnected());
//			log.info("S getKeepAlive:{}", socket.getKeepAlive());
//			log.info("S isBound:{}", socket.isBound());
//			log.info("S isClosed:{}", socket.isClosed());
//			log.info("S isOutputShutdown:{}", socket.isOutputShutdown());
//			log.info("S isInputShutdown:{}", socket.isInputShutdown());
//			log.info(Thread.currentThread().getName());
//			int count = Thread.activeCount();
//			log.info(count);
//			Thread th[] = new Thread[count];
//			Thread.enumerate(th);
//			for (int i = 0; i < count; i++) {
//				log.info(i + ": " + th[i]);
//			}
//			log.info(socket.getInetAddress().getHostAddress());
//			log.info(socket.getInetAddress().getHostAddress());
//			log.info(socket.getLocalAddress());
//			log.info(socket.getLocalSocketAddress());

			DataInputStream dis = new DataInputStream(socket.getInputStream());
			log.debug(Constants.TIMESTAMP_FORMAT.format(new Date(dis.readLong())));
			dis.close();
			socket.close();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
    }

}