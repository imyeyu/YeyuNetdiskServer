package net.imyeyu.netdisk.server.listener;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import net.imyeyu.netdisk.server.Main;
import net.imyeyu.netdisk.server.socket.StateSocket;

public class StateListener extends Thread {
	
	private int port = -1;
	private List<Socket> onlines;
	
	public StateListener(int port) {
		this.port = port;
		this.onlines = new ArrayList<>();
	}
	
	public void run() {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
			while (true) {
				for (int i = 0; i < onlines.size(); i++) {
					if (onlines.get(i).isClosed()) onlines.remove(i);
				}
				Socket socket = serverSocket.accept();
				onlines.add(socket);
				Main.log.info("新的客户端连接 IP -> " + socket.getInetAddress().getHostAddress() + " - " + socket.getInetAddress().getHostName());
				StateSocket stateSocket = new StateSocket(this, socket);
				stateSocket.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<Socket> getOnlines() {
		return onlines;
	}
	
	public void remove(Socket socket) {
		this.onlines.remove(socket);
	}
}