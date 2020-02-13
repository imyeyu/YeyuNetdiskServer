package net.imyeyu.netdisk.server.core;

public class Factory {

	public static CoreAPI getApi() {
		return new Proxy();
	}
}