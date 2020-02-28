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

	public String getConfig() {
		return core.getConfig();
	}
	
	public void setText(JsonElement value) {
		log.info("保存文本 -> " + value.getAsString());
		core.setText(value);
	}

	public String getText(JsonElement value) {
		log.info("预览文本 -> " + value.getAsString());
		return core.getText(value);
	}

	public String getFileList(JsonElement value) {
		log.info("文件列表 -> " + value.getAsString());
		return core.getFileList(value);
	}

	public String getFolderList(JsonElement value) {
		log.info("文件夹列表 -> " + value.getAsString());
		return core.getFolderList(value);
	}

	public void zip(JsonElement value) {
		log.info("压缩文件 -> " + value.getAsString());
		core.zip(value);
	}

	public void unZip(JsonElement value) {
		log.info("解压文件 -> " + value.getAsString());
		core.unZip(value);
	}

	public boolean newFolder(JsonElement value) {
		log.info("新建文件夹 -> " + value.getAsString());
		return core.newFolder(value);
	}

	public boolean renameFile(JsonElement value) {
		log.info("重命名 -> " + value.getAsString());
		return core.renameFile(value);
	}

	public void moveFiles(JsonElement value) {
		log.info("移动文件 -> " + value.getAsString());
		core.moveFiles(value);
	}

	public void copyFiles(JsonElement value) throws Exception {
		log.info("复制文件 -> " + value.getAsString());
		core.copyFiles(value);
	}

	public void eachDeleteFiles(File file) {
		log.info("删除对象 -> " + file.getAbsolutePath());
		core.eachDeleteFiles(file);
	}

	public void addYear(String year) {
		log.info("照片管理器 -> 新建年份 -> " + year);
		core.addYear(year);
	}

	public String getPhotoDateList() {
		log.info("照片管理器 -> 获取日期");
		return core.getPhotoDateList();
	}

	public String getPhotoInfo(JsonElement value) throws Exception {
		log.info("照片管理器 -> 照片信息 -> " + value.getAsString());
		return core.getPhotoInfo(value);
	}
}