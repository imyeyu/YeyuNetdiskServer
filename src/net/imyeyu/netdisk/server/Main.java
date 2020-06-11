package net.imyeyu.netdisk.server;

import java.io.File;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.imyeyu.netdisk.server.listener.DownloadListener;
import net.imyeyu.netdisk.server.listener.PublicListener;
import net.imyeyu.netdisk.server.listener.StateListener;
import net.imyeyu.netdisk.server.listener.UploadListener;
import net.imyeyu.utils.Configer;
import net.imyeyu.utils.Logger;
import net.imyeyu.utils.YeyuUtils;

public class Main {
	
	private static final String VERSION = "1.0.1";
	public static final Logger log = new Logger(true);
	
	public static String root;
	public static Map<String, Object> config;
	public static String TOKEN;
	
	public static void main(String[] args) {
		long setupTime = System.currentTimeMillis();
		showAnsiLogo();
		// 加载配置
		log.info("正在加载配置..");
		config = new Configer("net/imyeyu/netdisk/server/res/iNetdiskServer.ini").get();
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
		// 生成默认文件夹
		try {
			JsonObject jo;
			JsonArray ja = (new JsonParser()).parse(config.get("navList").toString()).getAsJsonArray();
			for (int i = 0; i < ja.size(); i++) {
				jo = ja.get(i).getAsJsonObject();
				for (Map.Entry<String, JsonElement> item : jo.entrySet()) {
					(new File(Main.root + File.separator + item.getValue().getAsString())).mkdirs();
				}
			}
		} catch (Exception e) {
			log.error("无法解析客户端导航列表");
			log.error(e);
		}
		// 获取监听端口
		final int PUBLIC_PORT = Integer.valueOf(config.get("portPublic").toString());
		final int STATE_PORT = Integer.valueOf(config.get("portState").toString());
		final int UPLOAD_PORT = Integer.valueOf(config.get("portUpload").toString());
		final int DOWNLOAD_PORT = Integer.valueOf(config.get("portDownload").toString());
		log.info("通用请求端口：" + PUBLIC_PORT);
		log.info("状态请求端口：" + STATE_PORT);
		log.info("上传文件端口：" + UPLOAD_PORT);
		log.info("下载文件端口：" + DOWNLOAD_PORT);
		// 启动监听核心
		log.info("正在启动服务..");
		StateListener stateListener = null;
		try {
			new PublicListener(PUBLIC_PORT).start();
			stateListener = new StateListener(STATE_PORT);
			stateListener.start();
			new UploadListener(UPLOAD_PORT).start();
			new DownloadListener(DOWNLOAD_PORT).start();
		} catch (Exception e) {
			log.error("核心异常，无法完全启动服务");
			log.error(e);
		}
		// 启动完成
		log.info("启动完成！版本：" + VERSION + "，耗时：" + (System.currentTimeMillis() - setupTime) + " 毫秒");
		// 监听控制台指令
		Scanner scanner = new Scanner(System.in);
		String command;
		while (true) {
			command = scanner.nextLine();
			log.info("使用命令 -> " + command);
			switch (command) {
				case "onlines":
					onlines(stateListener);
					break;
				case "clear":
					clear();
					break;
				case "restart":
				case "stop":
					log.info("正在关闭..");
					log.shutdown();
					scanner.close();
					log.info("已关闭服务端");
					if (command.equals("restart")) restart();
					System.exit(0);
					break;
				case "?":
				case "help":
					allCommand();
					break;
				default:
					log.info("未知命令 -> " + command);
					log.info("输入 ? 或 help 查看所有命令");
					break;
			}
		}
	}
	
	private static void onlines(StateListener stateListener) {
		if (stateListener != null && stateListener.isAlive()) {
			List<Socket> onlines = stateListener.getOnlines();
			log.info("在线 -> " + onlines.size());
			if (0 < onlines.size()) {
				log.info("在线列表：");
				for (int i = 0; i < onlines.size(); i++) {
					log.info("\t" + onlines.get(i).getInetAddress().getHostAddress() + " - " + onlines.get(i).getInetAddress().getHostName());
				}
			}
		} else {
			log.error("无法检测在线列表");
			log.error("StateListener object = " + stateListener);
			log.error("StateListener isAlive = " + stateListener.isAlive());
			log.error("StateListener isDaemon = " + stateListener.isDaemon());
		}
	}
	
	private static void clear() {
		try {
	    	new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	private static void restart() {
    	try {
			new ProcessBuilder("cmd", "/c", "start java -jar " + System.getProperty("java.class.path")).inheritIO().start().waitFor();
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	private static void allCommand() {
		log.info("onlines   -> 在线客户端");
		log.info("clear     -> 清空控制台");
		log.info("restart   -> 重新启动程序");
		log.info("stop      -> 结束程序");
		log.info("? 或 help -> 列出所有命令");
	}
	
	private static void showAnsiLogo() {
		log.info("\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0");
		log.info("\u3000\u3000\u3000\u3000\u3000\u3000\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0");
		log.info("\u3000\u3000\u3000\u3000\u3000\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0");
		log.info("\u3000\u3000\u3000\u3000\u25a0\u25a0\u25a0\u25a0\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u25a0\u25a0\u25a0\u25a0\u25a0");
		log.info("\u3000\u3000\u3000\u25a0\u25a0\u25a0\u25a0\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u25a0\u25a0\u25a0\u25a0");
		log.info("\u3000\u3000\u25a0\u25a0\u25a0\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u25a0\u25a0\u25a0");
		log.info("\u3000\u25a0\u25a0\u25a0\u25a0\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u25a0\u25a0\u25a0");
		log.info("\u3000\u25a0\u25a0\u25a0\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u25a0\u25a0\u25a0\u25a0");
		log.info("\u3000\u25a0\u25a0\u25a0\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u25a0\u25a0\u25a0\u25a0\u25a0");
		log.info("\u25a0\u25a0\u25a0\u3000\u3000\u3000\u3000\u3000\u25a0\u25a0\u25a0\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0");
		log.info("\u25a0\u25a0\u25a0\u3000\u3000\u3000\u3000\u3000\u25a0\u25a0\u25a0\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u25a0\u25a0\u25a0");
		log.info("\u25a0\u25a0\u25a0\u3000\u3000\u3000\u3000\u3000\u25a0\u25a0\u25a0\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u25a0\u25a0\u25a0");
		log.info("\u25a0\u25a0\u25a0\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u25a0\u25a0\u25a0");
		log.info("\u25a0\u25a0\u25a0\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u25a0\u25a0\u3000\u3000\u25a0\u25a0\u25a0");
		log.info("\u25a0\u25a0\u25a0\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u25a0\u25a0\u25a0");
		log.info("\u3000\u25a0\u25a0\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u591c\u96e8\u4e91\u76d8 - iNetDisk\u3000 \u3000\u3000\u3000\u25a0\u25a0\u25a0");
		log.info("\u3000\u25a0\u25a0\u25a0\u25a0\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u25a0\u25a0\u25a0");
		log.info("\u3000\u3000\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0");
		log.info("\u3000\u3000\u3000\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0");
		log.info("\u3000\u3000\u3000\u3000\u3000\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0\u25a0");
	}
}