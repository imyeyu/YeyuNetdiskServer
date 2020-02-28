package net.imyeyu.netdisk.server.bean;

import java.util.List;

public class FolderBean {

	private String name;
	private List<FolderBean> sub;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<FolderBean> getSub() {
		return sub;
	}

	public void setSub(List<FolderBean> sub) {
		this.sub = sub;
	}
}