package net.imyeyu.netdisk.server.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.imyeyu.netdisk.server.Main;
import net.imyeyu.netdisk.server.bean.FileBean;

public class Core implements CoreAPI {

	// 获取文件列表
	public String getFileList(JsonElement value) {
		List<FileBean> list = new ArrayList<FileBean>();
		FileBean file;
		File[] files = new File(Main.root + value.getAsString()).listFiles();
		// 文件夹
		for (int i = 0, l = files.length; i < l; i++) {
			if (files[i].isHidden())
				continue;
			if (!files[i].isFile()) {
				file = new FileBean();
				file.setName("folder." + files[i].getName());
				file.setDate(files[i].lastModified());
				list.add(file);
			}
		}
		// 文件
		String name, format = "unknown";
		for (int i = 0, l = files.length; i < l; i++) {
			if (files[i].isHidden())
				continue;
			if (files[i].isFile()) {
				name = files[i].getName();
				format = name.lastIndexOf(".") != -1 ? name.substring(name.lastIndexOf(".") + 1) : format;
				file = new FileBean();
				file.setName(format + "." + files[i].getName());
				file.setDate(files[i].lastModified());
				file.setSize(files[i].length());
				list.add(file);
			}
		}
		return new Gson().toJson(list).toString();
	}

	// 新建文件夹
	public boolean newFolder(JsonElement value) {
		String path = value.getAsString();
		int i = 2;
		File file = new File(Main.root + path + "新建文件夹");
		while (file.exists()) {
			file = new File(Main.root + path + "新建文件夹 (" + i + ")");
			i++;
		}
		return file.mkdir();
	}

	// 重命名
	public boolean renameFile(JsonElement value) {
		JsonParser jp = new JsonParser();
		JsonObject jo = (JsonObject) jp.parse(value.getAsString());
		String path = jo.get("path").getAsString();
		String oldFileName = jo.get("oldValue").getAsString();
		String newFileName = jo.get("newValue").getAsString();
		if (!oldFileName.equals(newFileName)) {
			File oldFile = new File(Main.root + path + oldFileName);
			File newFile = new File(Main.root + path + newFileName);
			if (oldFile.exists() && !newFile.exists()) {
				oldFile.renameTo(newFile);
				return true;
			}
		}
		return false;
	}

	// 递归删除文件
	public void eachDeleteFiles(File obj) {
		if (!obj.exists()) return;
		if (!obj.isFile()) {
			for (File file : obj.listFiles()) {
				eachDeleteFiles(file);
			}
		}
		obj.delete();
	}
}