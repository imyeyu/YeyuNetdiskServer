package net.imyeyu.netdisk.server.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Zip {

	private File targetFile;
	private String path;

	public Zip(File target) {
		targetFile = target;
		if (targetFile.exists()) targetFile.delete();
	}

	public Zip(String path) {
		this.path = path;
		(new File(path)).mkdirs();
	}

	/**
	 * 解压文件
	 * 
	 * @param path 解压路径
	 */
	public void unZip(File srcFile) {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(srcFile);
			Enumeration<?> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				if (entry.isDirectory()) {
					File dir = new File(path + File.separator + entry.getName());
					dir.mkdirs();
				} else {
					File targetFile = new File(path + File.separator + entry.getName());
					if (!targetFile.getParentFile().exists()) {
						targetFile.getParentFile().mkdirs();
					}
					targetFile.createNewFile();
					InputStream is = zipFile.getInputStream(entry);
					FileOutputStream fos = new FileOutputStream(targetFile);
					int len;
					byte[] buf = new byte[4096];
					while ((len = is.read(buf)) != -1) {
						fos.write(buf, 0, len);
					}
					fos.close();
					is.close();
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("unzip error from ZipUtils", e);
		} finally {
			if (zipFile != null) {
				try {
					zipFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 压缩文件
	 * 
	 * @param srcfile
	 */
	public void zipFiles(File srcfile) {
		ZipOutputStream out = null;
		try {
			out = new ZipOutputStream(new FileOutputStream(targetFile));
			if (srcfile.isFile()) {
				zipFile(srcfile, out, "");
			} else {
				File[] list = srcfile.listFiles();
				for (int i = 0; i < list.length; i++) {
					compress(list[i], out, "");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 压缩文件夹里的文件
	 * 
	 * @param file
	 * @param out
	 * @param basedir
	 */
	private void compress(File file, ZipOutputStream out, String basedir) {
		if (file.isDirectory()) {
			this.zipDirectory(file, out, basedir);
		} else {
			this.zipFile(file, out, basedir);
		}
	}

	/**
	 * 压缩单个文件
	 * 
	 * @param srcfile
	 */
	private void zipFile(File srcfile, ZipOutputStream out, String basedir) {
		if (!srcfile.exists()) return;
		byte[] buf = new byte[1024];
		FileInputStream in = null;
		try {
			int len;
			in = new FileInputStream(srcfile);
			out.putNextEntry(new ZipEntry(basedir + srcfile.getName()));
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) out.closeEntry();
				if (in != null) in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 压缩文件夹
	 * 
	 * @param dir
	 * @param out
	 * @param basedir
	 */
	private void zipDirectory(File dir, ZipOutputStream out, String basedir) {
		if (!dir.exists()) return;
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			compress(files[i], out, basedir + dir.getName() + "/");
		}
	}
}