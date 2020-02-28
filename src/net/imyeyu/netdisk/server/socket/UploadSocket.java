package net.imyeyu.netdisk.server.socket;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.imyeyu.netdisk.server.Main;

public class UploadSocket extends Thread {

	private Socket socket;
	private JsonParser jp;
	private OutputStream os;

	public UploadSocket(Socket socket) {
		this.socket = socket;
		this.jp = new JsonParser();
	}

	public void run() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			JsonObject jo = (JsonObject) jp.parse(br.readLine());
			JsonElement value = jo.get("value");
			if (jo.get("key").getAsString().equals("upload")) {
				if (jo.get("token").getAsString().equals(Main.TOKEN)) {
					upload(value);
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void upload(JsonElement value) throws Exception {
		// 唤醒
		JsonObject jo = (JsonObject) jp.parse(value.getAsString());
		(new File(Main.root + jo.get("toPath").getAsString())).mkdirs();
		File file = new File(Main.root + jo.get("toPath").getAsString() + jo.get("name").getAsString());
		Main.log.info("请求文件上传 -> " + file.getAbsolutePath());
		// 准备就绪
		os = socket.getOutputStream();
		response("ready");
		// 接收
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		FileOutputStream fos = new FileOutputStream(file);
		byte[] bytes = new byte[4096];
		int length = 0;
		while ((length = dis.read(bytes, 0, bytes.length)) != -1) {
			fos.write(bytes, 0, length);
			fos.flush();
		}
		Main.log.info("文件接收完成 -> " + file.getAbsolutePath());
		if (fos != null) fos.close();
		if (dis != null) dis.close();
		if (os != null) os.close();
		if (socket != null) socket.close();
	}

	private void response(Object data) throws IOException {
		if (os != null) os.write((data.toString() + "\r\n").getBytes("UTF-8"));
	}
}