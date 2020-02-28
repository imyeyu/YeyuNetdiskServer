package net.imyeyu.netdisk.server.core;

import java.io.File;

import com.google.gson.JsonElement;

public interface CoreAPI {
	
	public String getConfig();
	
	public void setText(JsonElement value);
	
	public String getText(JsonElement value);
	
	public String getFileList(JsonElement value);
	
	public String getFolderList(JsonElement value);
	
	public void zip(JsonElement value);
	
	public void unZip(JsonElement value);

	public boolean newFolder(JsonElement value);
	
	public boolean renameFile(JsonElement value);
	
	public void moveFiles(JsonElement value);
	
	public void copyFiles(JsonElement value) throws Exception;
	
	public void eachDeleteFiles(File obj);
	
	public void addYear(String year);
	
	public String getPhotoDateList();
	
	public String getPhotoInfo(JsonElement value) throws Exception;
}
