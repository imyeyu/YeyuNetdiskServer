package net.imyeyu.netdisk.server.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
	
	private static final String SEP = File.separator;

	/**
	 * 压缩
	 * 
	 * @param srcPath     要压缩的源文件路径
	 * @param zipPath     压缩文件保存的路径。zipPath 不能是 srcPath 路径下的子文件夹
	 * @param zipFileName 压缩文件名
	 * @throws Exception
	 */
	public static void zip(List<File> files, String zipPath, String zipFileName) throws Exception {
		CheckedOutputStream cos = null;
		ZipOutputStream zos = null;
		try {
			File zipDir = new File(zipPath);
			if (!zipDir.exists() || !zipDir.isDirectory()) {
				zipDir.mkdirs();
			}
			String zipFilePath = zipPath + SEP + zipFileName;
			File zipFile = new File(zipFilePath);
			if (zipFile.exists()) {
				SecurityManager securityManager = new SecurityManager();
				securityManager.checkDelete(zipFilePath);
				zipFile.delete();
			}
			cos = new CheckedOutputStream(new FileOutputStream(zipFile), new CRC32());
			zos = new ZipOutputStream(cos);
			for (int i = 0; i < files.size(); i++) {
				zip(files.get(i).getPath(), files.get(i), zos);
			}
			zos.flush();
		} catch (Exception e) {
			throw e;
		} finally {
			if (zos != null) zos.close();
		}
	}

	/**
	 * 解压
	 * 
	 * @param zipFilePath 压缩包路径
	 * @param unZipPath   解压路径
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static void unzip(String zipFilePath, String unZipPath) throws Exception {
		File zipFile = new File(zipFilePath);
		File unzipFileDir = new File(unZipPath);
		if (!unzipFileDir.exists() || !unzipFileDir.isDirectory()) {
			unzipFileDir.mkdirs();
		}
		
		int l = 0;
		File entryFile = null, entryDir = null;
		byte[] buffer = new byte[4096];
		String entryFilePath = null, entryDirPath = null;
		ZipFile zip = new ZipFile(zipFile);
		ZipEntry entry = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.entries();
		while (entries.hasMoreElements()) {
			entry = entries.nextElement();
			entryFilePath = unZipPath + entry.getName();
			if (entryFilePath.indexOf("/") != -1) {
				(new File(entryFilePath.substring(0, entryFilePath.lastIndexOf("/")))).mkdirs();
			}
			entryDirPath = entryFilePath.lastIndexOf(SEP) != -1 ? entryFilePath.substring(0, entryFilePath.lastIndexOf(SEP)) : "";
			entryDir = new File(entryDirPath);
			if (!entryDir.exists() || !entryDir.isDirectory()) {
				entryDir.mkdirs();
			}
			entryFile = new File(entryFilePath);
			bos = new BufferedOutputStream(new FileOutputStream(entryFile));
			bis = new BufferedInputStream(zip.getInputStream(entry));
			while ((l = bis.read(buffer, 0, 4096)) != -1) {
				bos.write(buffer, 0, l);
			}
			bos.flush();
			bos.close();
		}
		zip.close();
	}

	/**
	 * 递归压缩文件夹
	 * 
	 * @param srcRootDir 压缩文件夹根目录的子路径
	 * @param file       当前递归压缩的文件或目录对象
	 * @param zos        压缩文件存储对象
	 * @throws Exception
	 */
	private static void zip(String srcRootDir, File file, ZipOutputStream zos) throws Exception {
		if (file == null) return;
		if (file.isFile()) {
			int l;
			byte data[] = new byte[4096];
			String subPath = file.getAbsolutePath();
			if (subPath.equals(srcRootDir)) {
				subPath = subPath.substring(subPath.lastIndexOf(SEP) + 1);
			} else {
				if (subPath.indexOf(srcRootDir) != -1) {
					subPath = subPath.substring(srcRootDir.lastIndexOf(SEP) + SEP.length());
				}
			}
			ZipEntry entry = new ZipEntry(subPath);
			zos.putNextEntry(entry);
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			while ((l = bis.read(data, 0, 4096)) != -1) {
				zos.write(data, 0, l);
			}
			bis.close();
			zos.closeEntry();
		} else {
			File[] childFileList = file.listFiles();
			for (int i = 0; i < childFileList.length; i++) {
				childFileList[i].getAbsolutePath().indexOf(file.getAbsolutePath());
				zip(srcRootDir, childFileList[i], zos);
			}
		}
	}
}