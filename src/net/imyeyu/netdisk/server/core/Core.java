package net.imyeyu.netdisk.server.core;

import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javafx.scene.image.Image;
import net.imyeyu.netdisk.server.Main;
import net.imyeyu.netdisk.server.bean.FileBean;
import net.imyeyu.netdisk.server.bean.FolderBean;
import net.imyeyu.netdisk.server.bean.PhotoInfo;
import net.imyeyu.netdisk.server.bean.PhotoList;
import net.imyeyu.netdisk.server.bean.PhotoList.Month;
import net.imyeyu.netdisk.server.util.Zip;
import net.imyeyu.utils.YeyuUtils;

public class Core implements CoreAPI {

	private Gson gson = new Gson();
	private String sep = File.separator;
	private JsonParser jp = new JsonParser();
	private Map<String, Object> config = Main.config;
	
	// 获取服务器配置
	public String getConfig() {
		Map<String, Object> result = new HashMap<>();
		result.put("compressImg", config.get("compressImg").toString());
		result.put("photo", sep + config.get("photo").toString());
		result.put("document", sep + config.get("document").toString());
		result.put("otherBackup", sep + config.get("otherBackup").toString());
		if (config.get("publicFile") != null) {
			result.put("publicFile", config.get("publicFile").toString());
		}
		return gson.toJson(result);
	}

	// 保存文件文本
	public void setText(JsonElement value) {
		JsonObject jo = (JsonObject) jp.parse(value.getAsString());
		YeyuUtils.file().stringToFile(new File(Main.root + jo.get("file").getAsString()), jo.get("data").getAsString());
	}

	// 获取文件文本
	public String getText(JsonElement value) {
		return YeyuUtils.file().fileToString(new File(Main.root + value.getAsString()), "UTF-8");
	}

	// 获取文件列表
	public String getFileList(JsonElement value) {
		String path = value.getAsString();
		if (config.get("publicFile") == null || !path.startsWith(config.get("publicFile").toString())) {
			path = Main.root + path;
		}
		List<FileBean> list = new ArrayList<>();
		FileBean file;
		File[] files = new File(path).listFiles();
		if (files == null) return "null";
		// 文件夹
		for (int i = 0, l = files.length; i < l; i++) {
			if (files[i].isHidden())
				continue;
			if (!files[i].isFile()) {
				file = new FileBean();
				file.setName("folder." + files[i].getName());
				file.setDate(files[i].lastModified());
				list.add(file);
			}
		}
		// 文件
		String name, format = "unknown";
		for (int i = 0, l = files.length; i < l; i++) {
			if (files[i].isHidden())
				continue;
			if (files[i].isFile()) {
				name = files[i].getName();
				format = name.lastIndexOf(".") != -1 ? name.substring(name.lastIndexOf(".") + 1) : format;
				file = new FileBean();
				file.setName(format + "." + files[i].getName());
				file.setDate(files[i].lastModified());
				file.setSize(files[i].length());
				list.add(file);
			}
		}
		return gson.toJson(list).toString();
	}

	// 获取文件夹列表
	public String getFolderList(JsonElement value) {
		List<FolderBean> list = new ArrayList<>();
		List<FolderBean> sub;
		FolderBean folder, subFolder;
		File[] folders = new File(Main.root + value.getAsString()).listFiles();
		File[] subFolders;
		for (int i = 0, l = folders.length; i < l; i++) {
			if (folders[i].isDirectory()) {
				folder = new FolderBean();
				folder.setName(folders[i].getName());
				sub = new ArrayList<>();
				subFolders = folders[i].listFiles();
				for (int j = 0; j < subFolders.length; j++) {
					if (subFolders[j].isDirectory()) {
						subFolder = new FolderBean();
						subFolder.setName(subFolders[j].getName());
						sub.add(subFolder);
					}
				}
				folder.setSub(sub);
				list.add(folder);
			}
		}
		return gson.toJson(list);
	}

	// 压缩文件
	public void zip(JsonElement value) {
		value = (JsonElement) jp.parse(value.getAsString());
		JsonObject jo = value.getAsJsonObject();
		// 压缩列表
		JsonArray list = jo.get("list").getAsJsonArray();
		String path = jo.get("path").getAsString();
		if (list.size() == 1) { // 单文件压缩
			File formFile = new File(Main.root + path + sep + list.get(0).getAsString());
			File toFile = new File(Main.root + path + sep + jo.get("name").getAsString() + ".zip");
			(new Zip(toFile)).zipFiles(formFile);
		} else { // 多文件压缩
			String tmpFolder = Main.root + path + sep + jo.get("name").getAsString() + sep;
			String fromFileName;
			File formFile;
			File tmpFile;
			for (int i = 0; i < list.size(); i++) {
				fromFileName = list.get(i).getAsString();
				formFile = new File(Main.root + fromFileName);
				tmpFile = new File(tmpFolder + fromFileName.substring(fromFileName.lastIndexOf("\\") + 1));
				formFile.renameTo(tmpFile);
			}
			tmpFile = new File(tmpFolder);
			File toFile = new File(Main.root + path + sep + jo.get("name").getAsString() + ".zip");
			(new Zip(tmpFile)).zipFiles(toFile);
		}
	}
	
	// 解压文件
	public void unZip(JsonElement value) {
		value = (JsonElement) jp.parse(value.getAsString());
		JsonObject jo = value.getAsJsonObject();
		String zip = Main.root + jo.get("zip").getAsString();
		String path = Main.root + jo.get("path").getAsString();
		(new Zip(path)).unZip(new File(zip));
	}

	// 新建文件夹
	public boolean newFolder(JsonElement value) {
		String path = value.getAsString();
		int i = 2;
		File file = new File(Main.root + path + "新建文件夹");
		while (file.exists()) {
			file = new File(Main.root + path + "新建文件夹 (" + i + ")");
			i++;
		}
		return file.mkdir();
	}

	// 重命名
	public boolean renameFile(JsonElement value) {
		JsonObject jo = (JsonObject) jp.parse(value.getAsString());
		String path = jo.get("path").getAsString();
		String oldFileName = jo.get("oldValue").getAsString();
		String newFileName = jo.get("newValue").getAsString();
		if (!oldFileName.equals(newFileName)) {
			File oldFile = new File(Main.root + path + oldFileName);
			File newFile = new File(Main.root + path + newFileName);
			if (oldFile.exists() && !newFile.exists()) {
				oldFile.renameTo(newFile);
				return true;
			}
		}
		return false;
	}

	// 移动文件
	public void moveFiles(JsonElement value) {
		JsonObject jo = (JsonObject) jp.parse(value.getAsString());
		String path = jo.get("path").getAsString();
		JsonArray list = jo.get("list").getAsJsonArray();
		String file;
		File fromFile;
		for (int i = 0; i < list.size(); i++) {
			file = list.get(i).getAsString();
			fromFile = new File(Main.root + file);
			fromFile.renameTo(new File(Main.root + path + file.substring(file.lastIndexOf("\\") + 1)));
		}
	}

	// 复制文件
	public void copyFiles(JsonElement value) throws Exception {
		JsonObject jo = (JsonObject) jp.parse(value.getAsString());
		String toPath = jo.get("path").getAsString();
		JsonArray list = jo.get("list").getAsJsonArray();
		// 复制到公开外链
		String publicPath = config.get("publicFile").toString();
		toPath = (toPath.indexOf(publicPath) == -1) ? Main.root + toPath : publicPath;
		String file;
		File fromFile;
		for (int i = 0; i < list.size(); i++) {
			file = list.get(i).getAsString();
			fromFile = new File(Main.root + sep + file);
			if (fromFile.isFile()) {
				Files.copy(fromFile.toPath(), new File(toPath + sep + file.substring(file.lastIndexOf("\\") + 1)).toPath());
			} else {
				copyDirectiory(fromFile.getAbsolutePath(), toPath + sep + file);
			}
		}
	}

	// 递归复制文件夹
	private void copyDirectiory(String sourceDir, String targetDir) throws Exception {
		(new File(targetDir)).mkdirs();
		File[] file = (new File(sourceDir)).listFiles();
		for (int i = 0; i < file.length; i++) {
			if (file[i].isFile()) {
				File sourceFile = file[i];
				File targetFile = new File(new File(targetDir).getAbsolutePath() + sep + file[i].getName());
				Files.copy(sourceFile.toPath(), targetFile.toPath());
				continue;
			}
			if (file[i].isDirectory()) {
				String dir1 = sourceDir + sep + file[i].getName();
				String dir2 = targetDir + sep + file[i].getName();
				copyDirectiory(dir1, dir2);
			}
		}
	}

	// 递归删除文件
	public void eachDeleteFiles(File obj) {
		if (!obj.exists()) return;
		if (!obj.isFile()) {
			for (File file : obj.listFiles()) {
				eachDeleteFiles(file);
			}
		}
		obj.delete();
	}

	// 获取照片列表
	public String getPhotoDateList() {
		String photoRoot = Main.root + sep + "照片";
		File folder = new File(photoRoot);
		folder.mkdirs();

		List<PhotoList> result = new ArrayList<>();

		File[] yearsFolder = folder.listFiles();
		File[] monthsFolder;
		File[] itemsFile;
		PhotoList photo;
		List<Month> months;
		List<String> items;
		Month month;
		for (int i = 0; i < yearsFolder.length; i++) {
			photo = new PhotoList();
			photo.setYear(yearsFolder[i].getName());

			// 月份
			monthsFolder = yearsFolder[i].listFiles();
			Arrays.sort(monthsFolder, new Comparator<File>() {
				public int compare(File o1, File o2) {
					int n1 = extractNumber(o1.getName());
					int n2 = extractNumber(o2.getName());
					return n1 - n2;
				}
			});
			months = new ArrayList<>();
			for (int j = 0; j < monthsFolder.length; j++) {
				month = new Month();
				month.setMonth(monthsFolder[j].getName());

				// 照片列表
				itemsFile = monthsFolder[j].listFiles();
				items = new ArrayList<>();
				for (int k = 0; k < itemsFile.length; k++) {
					items.add(itemsFile[k].getName());
				}
				month.setItems(items);

				months.add(month);
			}
			photo.setMonths(months);

			result.add(photo);
		}
		return gson.toJson(result);
	}

	// 排序（文件夹）
	private static int extractNumber(String name) {
		try {
			return Integer.parseInt(name.replaceAll("[^\\d]", ""));
		} catch (Exception e) {
			return 0;
		}
	}

	// 新建年份
	public void addYear(String year) {
		(new File(Main.root + sep + "照片" + sep + year)).mkdirs();
	}

	// 获取照片信息
	public String getPhotoInfo(JsonElement value) {
		PhotoInfo info = new PhotoInfo();

		File file = new File(Main.root + sep + "照片" + sep + value.getAsString());
		info.setName(file.getName());
		info.setPos(file.getAbsolutePath());
		info.setSize(String.valueOf(file.length()));
		
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(file);
			for (Directory dir : metadata.getDirectories()) {
				if (dir == null) continue;
				for (Tag tag : dir.getTags()) {
					String tagName = tag.getTagName();
					String desc = tag.getDescription();
					switch (tagName) {
						case "GPS Latitude": // 经度
							info.setLat(pointToLatlong(desc));
							break;
						case "GPS Longitude": // 纬度
							info.setLng(pointToLatlong(desc));
							break;
						case "GPS Altitude": // 海拔高度
							info.setAlt(desc);
							break;
						case "Date/Time Original": // 拍摄时间
							SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
							Date date = dateFormat.parse(desc);
							dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							info.setDate(dateFormat.format(date));
							break;
						case "Image Width": // 宽
							info.setWidth(desc.replaceAll("[^0-9]", "").trim());
							break;
						case "Image Height": // 高
							info.setHeight(desc.replaceAll("[^0-9]", "").trim());
							break;
					}
				}
				if (dir.containsTag(ExifSubIFDDirectory.TAG_FNUMBER)) { // 光圈
					info.setAperture(dir.getDescription(ExifSubIFDDirectory.TAG_FNUMBER));
				}
				if (dir.containsTag(ExifSubIFDDirectory.TAG_EXPOSURE_TIME)) { // 曝光时间
					info.setToe(dir.getString(ExifSubIFDDirectory.TAG_EXPOSURE_TIME));
				}
				if (dir.containsTag(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT)) { // ISO 速度
					info.setIso(dir.getString(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT));
				}
				if (dir.containsTag(ExifSubIFDDirectory.TAG_FOCAL_LENGTH)) { // 焦距
					info.setFocalLength(dir.getString(ExifSubIFDDirectory.TAG_FOCAL_LENGTH));
				}
				if (dir.containsTag(ExifSubIFDDirectory.TAG_MAKE)) { // 相机厂商
					info.setMake(dir.getString(ExifSubIFDDirectory.TAG_MAKE));
				}
				if (dir.containsTag(ExifSubIFDDirectory.TAG_MODEL)) { // 相机品牌
					info.setCamera(dir.getString(ExifSubIFDDirectory.TAG_MODEL));
				}
				if (dir.containsTag(ExifSubIFDDirectory.TAG_SOFTWARE)) { // 系统
					info.setOs(dir.getString(ExifSubIFDDirectory.TAG_SOFTWARE));
				}
			}
		} catch (Exception e) {
			Image img = new Image(file.getAbsolutePath());
			info.setWidth(String.valueOf(img.getWidth()));
			info.setHeight(String.valueOf(img.getHeight()));
		}
		return gson.toJson(info);
	}

	// 坐标转经纬度
	public static String pointToLatlong(String point) {
		double du = Double.parseDouble(point.substring(0, point.indexOf(" ") - 1).trim());
		double fen = Double.parseDouble(point.substring(point.indexOf(" "), point.indexOf("'")).trim());
		double miao = Double.parseDouble(point.substring(point.lastIndexOf(" "), point.indexOf("\"")).trim());
		double duStr = du + fen / 60 + miao / 60 / 60;
		return String.valueOf(duStr);
	}
}