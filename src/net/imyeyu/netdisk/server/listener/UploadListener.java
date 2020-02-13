package net.imyeyu.netdisk.server.listener;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import net.imyeyu.netdisk.server.socket.UploadSocket;

public class UploadListener extends Thread {
	
	private int port = -1;
	
	public UploadListener(int port) {
		this.port = port;
	}
	
	public void run() {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
			while (true) {
				Socket socket = serverSocket.accept();
				UploadSocket requestSocket = new UploadSocket(socket);
				requestSocket.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}