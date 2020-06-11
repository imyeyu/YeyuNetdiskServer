package net.imyeyu.netdisk.server.core;

import java.io.File;
import java.io.IOException;

import com.google.gson.JsonElement;

public interface CoreAPI {
	
	public String getConfig();
	
	public void setText(JsonElement value);
	
	public String getText(JsonElement value);
	
	public String getFileList(JsonElement value);
	
	public String getFolderList(JsonElement value);
	
	public void zip(JsonElement value) throws Exception;
	
	public void unZip(JsonElement value) throws Exception;

	public void newFolder(JsonElement value);
	
	public void newText(JsonElement value) throws IOException;
	
	public boolean renameFile(JsonElement value);
	
	public void moveFiles(JsonElement value);
	
	public void copyFiles(JsonElement value) throws Exception;
	
	public void eachDeleteFiles(File obj);
	
	public void addYear(String year);
	
	public String getPhotoDateList();
	
	public String getPhotoInfo(JsonElement value);
	
	public String getMP4Info(JsonElement value) throws Exception;
}
