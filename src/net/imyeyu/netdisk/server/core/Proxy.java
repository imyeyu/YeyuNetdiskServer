package net.imyeyu.netdisk.server.core;

import java.io.File;

import com.google.gson.JsonElement;

import net.imyeyu.netdisk.server.Main;
import net.imyeyu.utils.Logger;

public class Proxy implements CoreAPI {

	private Logger log;
	private Core core;
	
	public Proxy() {
		this.log = Main.log;
		this.core = new Core();
	}

	public String getFileList(JsonElement value) {
		log.info("文件列表 -> " + value.getAsString());
		return core.getFileList(value);
	}

	public boolean newFolder(JsonElement value) {
		log.info("新建文件夹 -> " + value.toString());
		return core.newFolder(value);
	}

	public boolean renameFile(JsonElement value) {
		log.info("重命名 -> " + value.getAsString());
		return core.renameFile(value);
	}

	public void eachDeleteFiles(File obj) {
		log.info("删除对象 -> " + obj.toString());
		core.eachDeleteFiles(obj);
	}
}