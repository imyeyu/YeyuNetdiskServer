package net.imyeyu.netdisk.server.socket;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.imyeyu.netdisk.server.Main;
import net.imyeyu.netdisk.server.core.Factory;

public class PublicSocket extends Thread {

	private Socket socket;
	private OutputStream os;

	public PublicSocket(Socket socket) {
		this.socket = socket;
	}

	public void run() {
		try {
			InputStream is = socket.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			JsonParser jp = new JsonParser();
			JsonObject jo = (JsonObject) jp.parse(br.readLine());
			JsonElement value = jo.get("value");
			os = socket.getOutputStream();
			if (jo.get("token").getAsString().equals(Main.TOKEN)) {
				switch (jo.get("key").getAsString()) {
					case "path":
						response(Factory.getApi().getFileList(value));
						break;
					case "upload":
						break;
					case "newFolder":
						response(Factory.getApi().newFolder(value));
						break;
					case "rename":
						response(Factory.getApi().renameFile(value));
						break;
					case "delete":
						value = (JsonElement) jp.parse(value.getAsString());
						JsonArray list = value.getAsJsonArray();
						for (int i = 0; i < list.size(); i++) {
							Factory.getApi().eachDeleteFiles(new File(Main.root + list.get(i).getAsString()));
							response(i + 1);
						}
						break;
				}
			}
			response("finish");
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void response(Object data) throws IOException {
		if (os != null) {
			os = socket.getOutputStream();
			os.write((data.toString() + "\r\n").getBytes("UTF-8"));
		}
	}
}