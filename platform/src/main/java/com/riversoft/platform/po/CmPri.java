/*
 * $HeadURL: $
 * $Id: $
 * Copyright (c) 2013 by Riversoft System, all rights reserved.
 */
package com.riversoft.platform.po;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.riversoft.core.db.DataPO;
import com.riversoft.core.db.po.Code2NameVO;

/**
 * @author Woden
 * 
 */
public class CmPri implements Serializable {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(CmPri.class);

	/** */
	private static final long serialVersionUID = 1L;
	private String priKey;
	private Integer type = (Integer) Types.BASIC.getCode();
	private Integer checkType = 2;// EL
	private String checkScript = "${true}";
	private String description;

	private Integer catelogType;
	private String catelogKey;
	private String busiName;

	/**
	 * @return the catelogType
	 */
	public Integer getCatelogType() {
		return catelogType;
	}

	/**
	 * @param catelogType
	 *            the catelogType to set
	 */
	public void setCatelogType(Integer catelogType) {
		this.catelogType = catelogType;
	}

	/**
	 * @return the catelogKey
	 */
	public String getCatelogKey() {
		return catelogKey;
	}

	/**
	 * @param catelogKey
	 *            the catelogKey to set
	 */
	public void setCatelogKey(String catelogKey) {
		this.catelogKey = catelogKey;
	}

	/**
	 * @param busiName
	 *            the busiName to set
	 */
	public void setBusiName(String busiName) {
		this.busiName = busiName;
	}

	/**
	 * @return the busiName
	 */
	public String getBusiName() {
		return busiName;
	}

	/**
	 * @return the priKey
	 */
	public String getPriKey() {
		return priKey;
	}

	/**
	 * @param priKey
	 *            the priKey to set
	 */
	public void setPriKey(String priKey) {
		this.priKey = priKey;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the type
	 */
	public Integer getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(Integer type) {
		this.type = type;
	}

	/**
	 * @return the checkType
	 */
	public Integer getCheckType() {
		return checkType;
	}

	/**
	 * @param checkType
	 *            the checkType to set
	 */
	public void setCheckType(Integer checkType) {
		this.checkType = checkType;
	}

	/**
	 * @return the checkScript
	 */
	public String getCheckScript() {
		return checkScript;
	}

	/**
	 * @param checkScript
	 *            the checkScript to set
	 */
	public void setCheckScript(String checkScript) {
		this.checkScript = checkScript;
	}

	/**
	 * 是否只使用脚本
	 * 
	 * @return
	 */
	public boolean isScriptOnly() {
		return Types.ONLY_SCRIPT.code == this.type;
	}

	/**
	 * 设置开发信息<br>
	 * FIXME 目前用try/catch不优雅,未来再考虑优化
	 * 
	 * @param po
	 * @param msgs
	 */
	public void setDevelopmentInfo(final Object po, String... msgs) {
		Object obj = po;
		if (po instanceof DataPO) {
			obj = ((DataPO) po).toEntity();
		}

		try {
			try {
				String description = (String) PropertyUtils.getProperty(obj, "description");
				if (StringUtils.isNotEmpty(description)) {
					this.busiName = description + (msgs != null && msgs.length > 0 ? "-" : "")
							+ StringUtils.join(msgs, "-");
				}
			} catch (NoSuchMethodException e1) {
				// do nothing
			}

			try {
				String name = (String) PropertyUtils.getProperty(obj, "name");
				if (StringUtils.isNotEmpty(name)) {
					this.busiName = name + (msgs != null && msgs.length > 0 ? "-" : "") + StringUtils.join(msgs, "-");
				}
			} catch (NoSuchMethodException e1) {
				// do nothing
			}

			try {
				String busiName = (String) PropertyUtils.getProperty(obj, "busiName");
				if (StringUtils.isNotEmpty(busiName)) {
					this.busiName = busiName + (msgs != null && msgs.length > 0 ? "-" : "")
							+ StringUtils.join(msgs, "-");
				}
			} catch (NoSuchMethodException e1) {
				// do nothing
			}

			try {
				String viewKey = (String) PropertyUtils.getProperty(obj, "viewKey");
				if (StringUtils.isNotEmpty(viewKey)) {
					this.catelogType = Catelog.VIEW.code;
					this.catelogKey = viewKey;
					return;
				}
			} catch (NoSuchMethodException e1) {
				// do nothing
			}
			try {
				String widgetKey = (String) PropertyUtils.getProperty(obj, "widgetKey");
				if (StringUtils.isNotEmpty(widgetKey)) {
					this.catelogType = Catelog.WIDGET.code;
					this.catelogKey = widgetKey;
					return;
				}
			} catch (NoSuchMethodException e1) {
				// do nothing
			}

			try {
				String id = (String) PropertyUtils.getProperty(obj, "id");
				if (StringUtils.isNotEmpty(id)) {
					this.catelogType = Catelog.MENU.code;
					this.catelogKey = id;
					return;
				}
			} catch (NoSuchMethodException e1) {
				// do nothing
			}

		} catch (IllegalAccessException | InvocationTargetException e) {
			// do nothing
			logger.warn("设置CmPri出错.", e);
		}

		this.catelogType = Catelog.NONE.code;
		this.catelogKey = "NULL";
	}

	public static enum Types implements Code2NameVO {

		BASIC(1, "仅权限点"), ONLY_SCRIPT(2, "仅功能点"), AND_SCRIPT(3, "且模式"), OR_SCRIPT(4, "或模式");
		private int code;
		private String showName;

		private Types(int code, String showName) {
			this.code = code;
			this.showName = showName;
		}

		/**
		 * 数字转换成枚举
		 * 
		 * @param code
		 * @return
		 */
		public static Types fromCode(int code) {
			for (Types types : values()) {
				if (types.code == code) {
					return types;
				}
			}

			return BASIC;
		}

		@Override
		public Object getCode() {
			return code;
		}

		@Override
		public String getShowName() {
			return showName;
		}

	}

	public static enum Catelog implements Code2NameVO {
		MENU(1, "菜单/域权限"), VIEW(2, "视图权限"), WIDGET(3, "控件权限"), THIRDPART(4, "第三方系统权限"), NONE(-1, "检索错误");
		private int code;
		private String showName;

		private Catelog(int code, String showName) {
			this.code = code;
			this.showName = showName;
		}

		/**
		 * 数字转换成枚举
		 * 
		 * @param code
		 * @return
		 */
		public static Catelog fromCode(int code) {
			for (Catelog catelog : values()) {
				if (catelog.code == code) {
					return catelog;
				}
			}
			return NONE;
		}

		@Override
		public Object getCode() {
			return code;
		}

		@Override
		public String getShowName() {
			return showName;
		}
	}
}
