package net.imyeyu.netdisk.server.socket;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.imyeyu.netdisk.server.Main;

public class DownloadSocket extends Thread {

	private Socket socket;
	private JsonParser jp;
	private InputStream is;
	private OutputStream os;

	public DownloadSocket(Socket socket) {
		this.socket = socket;
		this.jp = new JsonParser();
	}

	public void run() {
		try {
			is = socket.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			JsonObject jo = (JsonObject) jp.parse(br.readLine());
			JsonElement value = jo.get("value");
			if (jo.get("key").getAsString().equals("download")) {
				if (jo.get("token").getAsString().equals(Main.TOKEN)) {
					download(value);
				} else {
					os = socket.getOutputStream();
					response("code error");
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void download(JsonElement value) throws Exception {
		// 唤醒
		JsonObject jo = (JsonObject) jp.parse(value.getAsString());
		File file = new File(Main.root + jo.get("path").getAsString() + jo.get("name").getAsString());
		Main.log.info("请求文件下载 -> " + file.getAbsolutePath());
		// 准备就绪
		os = socket.getOutputStream();
		response("ready");
		// 发送
		FileInputStream fis = new FileInputStream(file);
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		byte[] bytes = new byte[1024];
		int length = 0;
		while ((length = fis.read(bytes, 0, bytes.length)) != -1) {
			dos.write(bytes, 0, length);
			dos.flush();
		}
		Main.log.info("文件发送完成 -> " + file.getAbsolutePath());
		if (fis != null) fis.close();
		if (dos != null) dos.close();
		if (os != null) os.close();
		if (socket != null) socket.close();
	}

	private void response(Object data) throws IOException {
		if (os != null) os.write((data.toString() + "\r\n").getBytes("UTF-8"));
	}
}