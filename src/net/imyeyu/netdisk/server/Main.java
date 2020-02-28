package net.imyeyu.netdisk.server;

import java.io.File;
import java.util.Map;

import net.imyeyu.netdisk.server.listener.DownloadListener;
import net.imyeyu.netdisk.server.listener.PublicListener;
import net.imyeyu.netdisk.server.listener.StateListener;
import net.imyeyu.netdisk.server.listener.UploadListener;
import net.imyeyu.utils.Configer;
import net.imyeyu.utils.Logger;
import net.imyeyu.utils.YeyuUtils;

public class Main {
	
	public static final Logger log = new Logger();
	
	public static String root;
	public static Map<String, Object> config;
	public static String TOKEN;
	
	public static void main(String[] args) {
		config = new Configer("net/imyeyu/netdisk/server/res/NetDiskServer.ini").get();
		
		log.info("正在启动服务..");

		// 获取监听端口
		final int PUBLIC_PORT = Integer.valueOf(config.get("portPublic").toString());
		final int STATE_PORT = Integer.valueOf(config.get("portState").toString());
		final int UPLOAD_PORT = Integer.valueOf(config.get("portUpload").toString());
		final int DOWNLOAD_PORT = Integer.valueOf(config.get("portDownload").toString());
		// 获取通信令牌
		TOKEN = config.get("token").toString();
		if (!Boolean.valueOf(config.get("eToken").toString())) {
			TOKEN = YeyuUtils.encode().generateBase(TOKEN);
		}
		// 根目录检查
		root = config.get("root").toString();
		String path = System.getProperty("user.dir");
		path = path.replaceAll("\\\\", "\\\\\\\\");
		root = root.replaceAll("%core%", path);
		File rootFolder = new File(root);
		if (!rootFolder.exists()) rootFolder.mkdirs();
		log.info("云盘根目录：" + root);
		// 启动监听核心
		new PublicListener(PUBLIC_PORT).start();
		new StateListener(STATE_PORT).start();
		new UploadListener(UPLOAD_PORT).start();
		new DownloadListener(DOWNLOAD_PORT).start();
		// 其他文件夹
		if (config.get("photo") != null) {
			(new File(Main.root + File.separator + config.get("photo").toString())).mkdirs();
		}
		if (config.get("document") != null) {
			(new File(Main.root + File.separator + config.get("document").toString())).mkdirs();
		}
		if (config.get("otherBackup") != null) {
			(new File(Main.root + File.separator + config.get("otherBackup").toString())).mkdirs();
		}
		
		log.info("通用请求端口：" + PUBLIC_PORT);
		log.info("状态请求端口：" + STATE_PORT);
		log.info("上传文件端口：" + UPLOAD_PORT);
		log.info("下载文件端口：" + DOWNLOAD_PORT);
	}
}