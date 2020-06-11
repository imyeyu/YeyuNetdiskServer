package net.imyeyu.netdisk.server.socket;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.imyeyu.netdisk.server.Main;
import net.imyeyu.netdisk.server.bean.ServerStatus;
import net.imyeyu.netdisk.server.listener.StateListener;
import net.imyeyu.utils.Logger;

public class StateSocket extends Thread {
	
	private Logger log;

	private Socket socket;
	private Sigar sigar = new Sigar();
	private StateListener listener;

	public StateSocket(StateListener listener, Socket socket) {
		this.listener = listener;
		this.socket = socket;
		this.log = Main.log;
		
	}

	public void run() {
		try {
			InputStream is = socket.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			JsonParser jp = new JsonParser();
			JsonObject jo = (JsonObject) jp.parse(br.readLine());
			OutputStream os = socket.getOutputStream();
			if (jo.get("token").getAsString().equals(Main.TOKEN)) {
				while (true) {
					os.write((new Gson().toJson(getServerStatus()) + "\r\n").getBytes("UTF-8"));
					sleep(1000);
				}
			} else {
				log.info("验证失败，IP -> " + socket.getInetAddress() + "，客户端 TOKEN -> " + jo.get("token").getAsString());
			}
			br.close();
			os.close();
			socket.close();
		} catch (SocketException e) {
			log.warning("State socket 断开连接");
		} catch (Exception e) {
			e.printStackTrace();
		}
		listener.remove(socket);
	}

	private ServerStatus getServerStatus() {
		ServerStatus status = new ServerStatus();
		try {
			Mem mem = sigar.getMem();
			CpuPerc cp = sigar.getCpuPerc();
			FileSystem fs = sigar.getFileSystemList()[1];
			FileSystemUsage usage = sigar.getFileSystemUsage(fs.getDirName());

			status.setCpuUse(cp.getCombined());
			status.setMemUse(mem.getUsed());
			status.setMemMax(mem.getTotal());
			status.setDiskUse(usage.getUsed());
			status.setDiskMax(usage.getTotal());
		} catch (SigarException e) {
			e.printStackTrace();
		}
		return status;
	}
}