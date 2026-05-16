/*
 * $HeadURL: $
 * $Id: $
 * Copyright (c) 2013 by Wodensoft System, all rights reserved.
 */
package com.riversoft.platform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.riversoft.core.exception.ExceptionType;
import com.riversoft.core.exception.SystemRuntimeException;
import com.riversoft.license.api.DefaultMagicImpl;
import com.riversoft.license.api.Identifier;
import com.riversoft.license.api.Magic;

/**
 * 平台信息,类似于JDK的{@link System}类
 * 
 * @author Woden
 * 
 */
public final class Platform {

	public static class Component implements Comparable<Component> {
		String description;
		int level;
		String name;
		String version;

		@Override
		public int compareTo(Component o) {
			return this.level - o.getLevel();
		}

		/**
		 * @return the description
		 */
		public String getDescription() {
			return description;
		}

		/**
		 * @return the level
		 */
		public int getLevel() {
			return level;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return the version
		 */
		public String getVersion() {
			return version;
		}

		/**
		 * @param description
		 *            the description to set
		 */
		public void setDescription(String description) {
			this.description = description;
		}

		/**
		 * @param level
		 *            the level to set
		 */
		public void setLevel(int level) {
			this.level = level;
		}

		/**
		 * @param name
		 *            the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * @param version
		 *            the version to set
		 */
		public void setVersion(String version) {
			this.version = version;
		}

	}

	/**
	 * 单例
	 */
	private static Platform instance;

	private static Logger logger = LoggerFactory.getLogger(Platform.class);

	/**
	 * 校验系统是否暂停
	 * 
	 * @return
	 */
	public static boolean checkPause() {
		return instance.pauseFlag;
	}

	/**
	 * @return the components
	 */
	public static List<Component> getComponents() {
		return instance.components;
	}

	/**
	 * 获取附件默认保存目录
	 * 
	 * @return
	 */
	public static File getDefaultAttachmentPath() {
		return instance.attachmentPath;
	}

	/**
	 * 获取Download默认目录
	 *
	 * @return
	 */
	public static File getDefaultDownloadPath() {
		return instance.downloadPath;
	}

	public static Identifier getIdentifier() {
		return getMagic().currentIdentifier();
	}

	/**
	 * 获取日志目录
	 * 
	 * @return
	 */
	public static File getLogPath() {
		return instance.logPath;
	}

	public static Magic getMagic() {
		return instance.magic;
	}

	/**
	 * 获取平台根目录
	 * 
	 * @return
	 */
	public static File getRoot() {
		return instance.root;
	}

	/**
	 * 获取临时目录
	 * 
	 * @return
	 */
	public static File getTempPath() {
		return instance.tempPath;
	}

	/**
	 * 获取当前平台版本
	 * 
	 * @return
	 */
	public static String getVersion() {
		return instance.version;
	}

	static String resolvePlatformVersion(Attributes mainAttribs) {
		if (mainAttribs != null) {
			String version = mainAttribs.getValue("Implementation-Version");
			if (StringUtils.isNotBlank(version)) {
				return version;
			}
		}
		String version = System.getProperty("bpmt.version");
		if (StringUtils.isNotBlank(version)) {
			return version;
		}
		version = System.getenv("BPMT_VERSION");
		if (StringUtils.isNotBlank(version)) {
			return version;
		}
		return "snapshot";
	}

	/**
	 * 初始化
	 * 
	 * @param root
	 */
	public synchronized static void init(File root) {
		instance = new Platform(root);
	}

	/**
	 * 是否支持自动下载更新
	 * 
	 * @return
	 */
	public static boolean isAutoUpdate() {
		return instance.autoUpdate;
	}

	/**
	 * 暂停平台
	 * 
	 */
	public static void pause() {
		instance.pauseFlag = true;
	}

	/**
	 * 平台恢复运行
	 */
	public static void run() {
		instance.pauseFlag = false;
	}

	/**
	 * 默认附件路径
	 */
	private File attachmentPath;

	/**
	 * 默认download目录路径
	 */
	private File downloadPath;

	/**
	 * 是否开启自下载动更新
	 */
	private boolean autoUpdate = false;

	/**
	 * 组件列表
	 */
	private List<Component> components;

	/**
	 * 默认日志目录
	 */
	private File logPath;

	private Magic magic;

	// 平台暂停标识
	private boolean pauseFlag = false;

	/**
	 * 平台根路径
	 */
	private File root;

	/**
	 * 临时文件夹
	 */
	private File tempPath;

	/**
	 * 当前的BPMT版本
	 */
	private String version;

	private Platform(File root) {
		logger.info("========== BPMT平台启动  开始 ==========");
		if (root != null) {
			this.root = root;
		} else {
			this.root = new File(System.getProperty("java.io.tmpdir"));// 使用临时路径
		}
		logger.info("平台根目录:" + this.root.getAbsolutePath());

		this.attachmentPath = new File(this.root, "attachment");
		if (!this.attachmentPath.exists()) {
			this.attachmentPath.mkdirs();
		}
		logger.info("附件根目录:" + attachmentPath.getAbsolutePath());

		this.downloadPath = new File(this.root, "download");
		if (!this.downloadPath.exists()) {
			this.downloadPath.mkdirs();
		}
		logger.info("Download根目录:" + downloadPath.getAbsolutePath());

		this.logPath = new File(this.root, "logs");
		if (!this.logPath.exists()) {
			this.logPath.mkdirs();
		}
		logger.info("日志根目录:" + logPath.getAbsolutePath());

		this.tempPath = new File(this.root, "tmp");
		if (!this.tempPath.exists()) {
			this.tempPath.mkdirs();
		}
		logger.info("临时目录:" + tempPath.getAbsolutePath());

		loadComponentsInfo();

		injectMagic();

		logger.info("========== BPMT平台启动  完成 ==========");
	}

	private void injectMagic() {
		ServiceLoader<Magic> sl = ServiceLoader.load(Magic.class);
		Iterator<Magic> magics = sl.iterator();

		if (magics.hasNext()) {
			Magic magic = magics.next();
			this.magic = magic;
			if (magic.currentIdentifier().isRegister()) {
				logger.info("平台注册ID:" + magic.currentIdentifier().getIdentifier());
			} else {
				logger.info("平台未注册.");
			}

			if (magics.hasNext()) {
				throw new SystemRuntimeException(ExceptionType.DEFAULT, "系统存在多个身份标识,系统文件被破坏?");
			}
		} else {
			this.magic = new DefaultMagicImpl();
			logger.info("平台未注册.");
		}

	}

	private void loadComponentsInfo() {
		components = new ArrayList<>();
		Enumeration<URL> resEnum;
		try {
			resEnum = Thread.currentThread().getContextClassLoader().getResources(JarFile.MANIFEST_NAME);
			while (resEnum.hasMoreElements()) {
				try {
					URL url = resEnum.nextElement();
					InputStream is = url.openStream();
					if (is != null) {
						Manifest manifest = new Manifest(is);
						Attributes mainAttribs = manifest.getMainAttributes();

						String buildlevel = mainAttribs.getValue("X-River-Build-Order");
						if (!StringUtils.isEmpty(buildlevel)) {
							String name = mainAttribs.getValue("X-River-CA");
							String description = mainAttribs.getValue("Implementation-Title");
							String version = mainAttribs.getValue("Implementation-Version");

							if (StringUtils.isEmpty(name)) {
								name = mainAttribs.getValue("X-River-Artifact");
								// 平台的版本
								if ("platform".equalsIgnoreCase(name)) {
									version = resolvePlatformVersion(mainAttribs);
									logger.info("平台版本:" + version);
									this.version = version;
								}
								continue;
							}

							Component component = new Component();
							component.setName(name);
							component.setDescription(description);
							component.setVersion(version);
							component.setLevel(Integer.valueOf(buildlevel));
							logger.info("获取插件:[" + component.getName() + "],版本:[" + component.getVersion() + "]");
							components.add(component);
						}
					}
				} catch (Exception e) {
					// Silently ignore wrong manifests on classpath
				}
			}
			Collections.sort(components);
		} catch (IOException e1) {
			// Silently ignore wrong manifests on classpath
		}

	}

}
