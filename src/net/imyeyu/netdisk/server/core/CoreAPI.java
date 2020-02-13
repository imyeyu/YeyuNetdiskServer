package net.imyeyu.netdisk.server.core;

import java.io.File;

import com.google.gson.JsonElement;

public interface CoreAPI {
	
	public String getFileList(JsonElement value);

	public boolean newFolder(JsonElement value);
	
	public boolean renameFile(JsonElement value);
	
	public void eachDeleteFiles(File obj);
}
