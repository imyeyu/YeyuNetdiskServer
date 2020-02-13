package net.imyeyu.netdisk.server.listener;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import net.imyeyu.netdisk.server.socket.StateSocket;

public class StateListener extends Thread {
	
	private int port = -1;
	
	public StateListener(int port) {
		this.port = port;
	}
	
	public void run() {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
			while (true) {
				Socket socket = serverSocket.accept();
				StateSocket stateSocket = new StateSocket(socket);
				stateSocket.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}