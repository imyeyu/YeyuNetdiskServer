package net.imyeyu.netdisk.server.socket;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javafx.scene.image.Image;
import net.coobird.thumbnailator.Thumbnails;
import net.imyeyu.netdisk.server.Main;
import net.imyeyu.netdisk.server.core.Factory;

/**
 * 公共请求接口
 * <br />
 * 接收大部分客户端请求内容
 * 
 * @author Yeyu
 *
 */
public class PublicSocket extends Thread {

	private Socket socket;
	private OutputStream os;
	private Map<String, Object> config = Main.config;

	public PublicSocket(Socket socket) {
		this.socket = socket;
	}

	public void run() {
		try {
			InputStream is = socket.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			JsonParser jp = new JsonParser();
			JsonObject jo = (JsonObject) jp.parse(br.readLine());
			JsonElement value = jo.get("value");
			os = socket.getOutputStream();
			if (jo.get("token").getAsString().equals(Main.TOKEN)) {
				switch (jo.get("key").getAsString()) {
					case "getConfig":        // 获取服务器配置
						response(Factory.getApi().getConfig());
						break;
					case "setText":          // 保存文本
						Factory.getApi().setText(value);
						break;
					case "getText":          // 获取文本
						response(Factory.getApi().getText(value));
						break;
					case "getImg":           // 获取图片
						Main.log.info("预览图片 -> " + value.getAsString());
						responseImg(value.getAsString());
						break;
					case "list":             // 获取文件列表
						response(Factory.getApi().getFileList(value));
						break;
					case "folder":           // 获取文件夹列表
						response(Factory.getApi().getFolderList(value));
						break;
					case "zip":              // 压缩
						try {
							Factory.getApi().zip(value);
						} catch (Exception e) {
							response("fail");
						}
						break;
					case "unzip":            // 解压
						try {
							Factory.getApi().unZip(value);
						} catch (Exception e) {
							response("fail");
						}
						break;
					case "newFolder":        // 新建文件夹
						Factory.getApi().newFolder(value);
						break;
					case "newText":          // 新建文本文档
						Factory.getApi().newText(value);
						break;
					case "rename":           // 重命名
						response(Factory.getApi().renameFile(value));
						break;
					case "move":             // 移动
						Factory.getApi().moveFiles(value);
						break;
					case "copy":             // 复制
						Factory.getApi().copyFiles(value);
						break;
					case "delete":           // 删除
						value = (JsonElement) jp.parse(value.getAsString());
						JsonArray list = value.getAsJsonArray();
						String path = Main.root;
						String isPublic = list.get(0).getAsString();
						if (config.get("publicFile") != null && isPublic.indexOf(config.get("publicFile").toString()) != -1) path = "";
						for (int i = 0; i < list.size(); i++) {
							Factory.getApi().eachDeleteFiles(new File(path + list.get(i).getAsString()));
							response(i + 1);
						}
						break;
					case "getPhotoDateList": // 获取照片日期
						response(Factory.getApi().getPhotoDateList());
						break;
					case "addYear":          // 新增年份
						Factory.getApi().addYear(value.getAsString());
						break;
					case "getImgPM":         // 照片管理器获取图片缩略图
						responseImgPM(value.getAsString());
						break;
					case "getPhotoInfo":     // 获取照片信息
						response(Factory.getApi().getPhotoInfo(value));
						break;
					case "getMP4Info":       // 获取 MP4 元数据
						response(Factory.getApi().getMP4Info(value));
						break;
				}
			}
			response("finish");
			br.close();
			is.close();
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void response(Object data) throws IOException {
		if (!socket.isClosed() && os != null) {
			os = socket.getOutputStream();
			os.write((data.toString() + "\r\n").getBytes("UTF-8"));
		}
	}
	
	// 照片管理器获取缩略图
	private void responseImgPM(String path) throws Exception {
		if (Boolean.valueOf(config.get("compressImg").toString())) { // 压缩
			response("size1"); // 压缩图片，不需要 size 参数，字符串前有 size 即可
			InputStream is = socket.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			if (br.readLine().equals("ready")) {
				os = socket.getOutputStream();
				File file = new File(Main.root + File.separator + config.get("photo").toString() + File.separator + path);
				FileInputStream isFIS = new FileInputStream(file);
				Main.log.info("照片管理器 -> 压缩照片 -> " + path);
				FileInputStream outFIS = new FileInputStream(file);
				Image img = new Image(isFIS);
				if (img.getWidth() < img.getHeight()) {
					Thumbnails.of(outFIS).width(128).toOutputStream(os);
				} else {
					Thumbnails.of(outFIS).height(128).toOutputStream(os);
				}
				System.gc();
				isFIS.close();
				outFIS.close();
				is.close();
				br.close();
			}
		} else { // 非压缩
			Main.log.info("照片管理器 -> 发送原图 -> " + path);
			responseImg(File.separator + config.get("photo").toString() + File.separator + path);
		}
	}
	
	// 获取原图
	private void responseImg(String path) throws Exception {
		File file = new File(Main.root + path);
		response("size" + file.length());
		InputStream is = socket.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		if (br.readLine().equals("ready")) {
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			FileInputStream fis = new FileInputStream(file);
			byte[] buffer = new byte[4096];
			int l = 0;
			while ((l = fis.read(buffer)) != -1) {
				dos.write(buffer, 0, l);
			}
			fis.close();
			dos.flush();
			dos.close();
			br.close();
		}
	}
}