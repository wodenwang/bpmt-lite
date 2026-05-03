/*
 * $HeadURL: $
 * $Id: $
 * Copyright (c) 2013 by Riversoft System, all rights reserved.
 */
package com.riversoft.module.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.riversoft.util.jackson.JsonMapper;
import org.apache.commons.lang3.StringUtils;

import com.riversoft.core.BeanFactory;
import com.riversoft.core.IDGenerator;
import com.riversoft.core.db.DataCondition;
import com.riversoft.core.db.DataPO;
import com.riversoft.core.db.ORMService;
import com.riversoft.core.exception.ExceptionType;
import com.riversoft.core.exception.SystemRuntimeException;
import com.riversoft.core.web.Actions;
import com.riversoft.core.web.Actions.Util;
import com.riversoft.core.web.RequestUtils;
import com.riversoft.core.web.annotation.ActionAccess;
import com.riversoft.core.web.annotation.ActionAccess.SafeLevel;
import com.riversoft.platform.po.CmPri;

/**
 * 菜单管理
 * 
 * @author Woden
 * 
 */
@ActionAccess(level = SafeLevel.DEV_R)
public class MenuAction {

	/**
	 * 首页
	 * 
	 * @param request
	 * @param response
	 */
	public void index(HttpServletRequest request, HttpServletResponse response) {
		Actions.includePage(request, response, Util.getPagePath(request, "main.jsp"));
	}

	/**
	 * 菜单树
	 * 
	 * @param request
	 * @param response
	 */
	public void tree(HttpServletRequest request, HttpServletResponse response) {
		final String cp = Actions.Util.getContextPath(request);
		List<Map<String, Object>> treeList = new ArrayList<>();
		Set<String> domainKeys = new HashSet<>();
		// 域
		List<Map<String, Object>> domains = ORMService.getInstance().query("CmDomain",
				new DataCondition().setOrderByAsc("sort").toEntity());
		for (Map<String, Object> domain : domains) {
			Map<String, Object> obj = new HashMap<>();
			obj.put("domainKey", domain.get("domainKey"));
			obj.put("id", "domain_" + domain.get("domainKey"));
			obj.put("busiName", domain.get("busiName") + "(域)");
			obj.put("sysFlag", 1 == (int) domain.get("sysFlag"));
			obj.put("parentId", null);
			obj.put("icon", cp + "/css/icon/house.png");
			obj.put("type", 1);// 域
			obj.put("font", "{'font-weight':'bold','color':'blue'}");
			domainKeys.add((String) domain.get("domainKey"));
			treeList.add(obj);
		}

		String random = String.valueOf(new Random().nextLong());
		// 设置无主域
		{
			Map<String, Object> obj = new HashMap<>();
			obj.put("id", "domain_" + random);
			obj.put("busiName", "无主记录(域)");
			obj.put("unManageFlag", true);
			obj.put("sysFlag", true);
			obj.put("parentId", null);
			obj.put("icon", cp + "/css/icon/house_connect.png");
			obj.put("type", 1);// 域
			obj.put("font", "{'font-weight':'bold','color':'red'}");
			treeList.add(obj);
		}

		List<Map<String, Object>> menus = ORMService.getInstance().query("CmMenu",
				new DataCondition().setOrderByAsc("sort").toEntity());
		for (Map<String, Object> menu : menus) {
			Map<String, Object> obj = new HashMap<>();
			obj.put("menuKey", menu.get("id"));
			obj.put("domainKey", menu.get("domainKey"));
			obj.put("id", "menu_" + menu.get("id"));
			obj.put("busiName", menu.get("name"));
			obj.put("sysFlag", 1 == (int) menu.get("sysFlag"));
			String domainKey = (String) menu.get("domainKey");
			String parentId = (String) menu.get("parentId");
			if (StringUtils.isNotEmpty(parentId) && !"null".equalsIgnoreCase(parentId)) {
				obj.put("parentId", "menu_" + parentId);
			} else {
				if (domainKeys.contains(domainKey)) {
					obj.put("parentId", "domain_" + domainKey);
				} else {
					obj.put("parentId", "domain_" + random);
				}
			}
			String icon = (String) menu.get("icon");
			if (StringUtils.isNotEmpty(icon)) {
				if (icon.toLowerCase().startsWith("http://") || icon.toLowerCase().startsWith("https://")) {
					obj.put("icon", icon);
				} else {
					obj.put("icon", cp + "/css/icon/" + icon);
				}
			}
			obj.put("type", 2);// 菜单
			treeList.add(obj);
		}
		request.setAttribute("menu", JsonMapper.defaultMapper().toJson(treeList));
		Actions.includePage(request, response, Util.getPagePath(request, "menu_tree.jsp"));
	}

	/**
	 * 保存位置
	 * 
	 * @param request
	 * @param response
	 */
	@ActionAccess(level = SafeLevel.DEV_W)
	public void saveSort(HttpServletRequest request, HttpServletResponse response) {
		HashMap<String, Object> tree = RequestUtils.getJsonValue(request, "tree");
		MenuService service = BeanFactory.getInstance().getBean(MenuService.class);
		service.executeSaveSort((List<HashMap<String, Object>>)tree.get("nodes"));
		Actions.redirectInfoPage(request, response, "保存位置成功.");
	}

	/**
	 * 删除节点
	 * 
	 * @param request
	 * @param response
	 */
	@ActionAccess(level = SafeLevel.DEV_W)
	@SuppressWarnings("unchecked")
	public void delMenuNode(HttpServletRequest request, HttpServletResponse response) {
		String id = RequestUtils.getStringValue(request, "id");
		Map<String, Object> menu = ((Map<String, Object>) ORMService.getInstance().findByPk("CmMenu", id));
		if (menu == null) {
			throw new SystemRuntimeException(ExceptionType.BUSINESS, "菜单不存在.");
		}
		String menuName = (String) menu.get("name");
		ORMService.getInstance().remove(menu);
		Actions.redirectInfoPage(request, response, "删除节点[" + menuName + "]成功.");
	}

	/**
	 * 编辑节点
	 * 
	 * @param request
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	public void editMenuZone(HttpServletRequest request, HttpServletResponse response) {
		String id = RequestUtils.getStringValue(request, "id");
		Map<String, Object> menu = ((Map<String, Object>) ORMService.getInstance().findByPk("CmMenu", id));
		if (menu == null) {
			throw new SystemRuntimeException(ExceptionType.BUSINESS, "菜单不存在.");
		}
		request.setAttribute("vo", menu);
		Actions.includePage(request, response, Util.getPagePath(request, "menu_form.jsp"));
	}

	/**
	 * 新增节点
	 * 
	 * @param request
	 * @param response
	 */
	public void createMenuZone(HttpServletRequest request, HttpServletResponse response) {
		Actions.includePage(request, response, Util.getPagePath(request, "menu_form.jsp"));
	}

	/**
	 * 表单提交
	 * 
	 * @param request
	 * @param response
	 */
	@ActionAccess(level = SafeLevel.DEV_W)
	public void submitMenuForm(HttpServletRequest request, HttpServletResponse response) {
		Integer isCreate = RequestUtils.getIntegerValue(request, "isCreate");
		String name = RequestUtils.getStringValue(request, "name");
		String icon = RequestUtils.getStringValue(request, "icon");
		String action = RequestUtils.getStringValue(request, "action");
		String thirdpartUrl = RequestUtils.getStringValue(request, "thirdpartUrl");
		String domainKey = RequestUtils.getStringValue(request, "domainKey");
		String parentId = RequestUtils.getStringValue(request, "parentId");
		Integer openType = RequestUtils.getIntegerValue(request, "openType");
		Integer sort = RequestUtils.getIntegerValue(request, "sort");
		Integer paramType = RequestUtils.getIntegerValue(request, "paramType");
		String paramScript = RequestUtils.getStringValue(request, "paramScript");
		CmPri pri = RequestUtils.getValue(request, "pri", CmPri.class);
		if (Integer.valueOf(2).equals(openType)) {
			action = thirdpartUrl == null ? null : thirdpartUrl.trim();
			if (!isValidThirdpartUrl(action)) {
				throw new SystemRuntimeException(ExceptionType.BUSINESS, "第三方网页地址格式不正确.");
			}
		}

		DataPO po;
		if (isCreate == 1) {// 新增
			po = newMenuPO();
			po.set("id", RequestUtils.getStringValue(request, "id"));
			po.set("sysFlag", 0);
		} else {// 编辑
			po = new DataPO("CmMenu", findMenu(RequestUtils.getStringValue(request, "id")));
		}

		po.set("icon", icon);
		po.set("name", name);
		po.set("action", action);
		po.set("domainKey", domainKey);
		po.set("parentId", parentId);
		po.set("openType", openType);
		po.set("sort", sort);
		po.set("paramType", paramType);
		po.set("paramScript", paramScript);
		po.set("pri", pri);

		MenuService service = menuService();
		if (isCreate == 1) {
			service.save(po.toEntity());
		} else {
			service.update(po.toEntity());
		}

		redirectInfoPage(request, response, "编辑菜单[" + name + "]成功.");
	}

	protected DataPO newMenuPO() {
		return new DataPO("CmMenu");
	}

	@SuppressWarnings("unchecked")
	protected Map<String, Object> findMenu(String id) {
		return (Map<String, Object>) ORMService.getInstance().findByPk("CmMenu", id);
	}

	protected MenuService menuService() {
		return BeanFactory.getInstance().getBean(MenuService.class);
	}

	protected void redirectInfoPage(HttpServletRequest request, HttpServletResponse response, String message) {
		Actions.redirectInfoPage(request, response, message);
	}

	private boolean isValidThirdpartUrl(String url) {
		return StringUtils.isNotBlank(url)
				&& (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("/"));
	}

	/**
	 * 删除节点
	 * 
	 * @param request
	 * @param response
	 */
	@ActionAccess(level = SafeLevel.DEV_W)
	public void delDomainNode(HttpServletRequest request, HttpServletResponse response) {
		String domainKey = RequestUtils.getStringValue(request, "domainKey");
		MenuService service = BeanFactory.getInstance().getBean(MenuService.class);
		service.executeRemoveDomain(domainKey);

		Actions.redirectInfoPage(request, response, "删除节点成功.");
	}

	/**
	 * 编辑节点
	 * 
	 * @param request
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	public void editDomainZone(HttpServletRequest request, HttpServletResponse response) {
		String domainKey = RequestUtils.getStringValue(request, "domainKey");
		Map<String, Object> domain = ((Map<String, Object>) ORMService.getInstance().findByPk("CmDomain", domainKey));
		if (domain == null) {
			throw new SystemRuntimeException(ExceptionType.BUSINESS, "域不存在.");
		}

		// 首页信息
		List<Map<String, Object>> homes = ORMService.getInstance().query("CmHome",
				new DataCondition().setStringEqual("domainKey", domainKey).setOrderByAsc("sort").toEntity());
		request.setAttribute("homes", homes);

		request.setAttribute("vo", domain);
		Actions.includePage(request, response, Util.getPagePath(request, "domain_form.jsp"));
	}

	/**
	 * 新增节点
	 * 
	 * @param request
	 * @param response
	 */
	public void createDomainZone(HttpServletRequest request, HttpServletResponse response) {
		Actions.includePage(request, response, Util.getPagePath(request, "domain_form.jsp"));
	}

	/**
	 * 表单提交
	 * 
	 * @param request
	 * @param response
	 */
	@ActionAccess(level = SafeLevel.DEV_W)
	@SuppressWarnings("unchecked")
	public void submitDomainForm(HttpServletRequest request, HttpServletResponse response) {
		Integer isCreate = RequestUtils.getIntegerValue(request, "isCreate");
		String busiName = RequestUtils.getStringValue(request, "busiName");
		String description = RequestUtils.getStringValue(request, "description");
		String domainKey = RequestUtils.getStringValue(request, "domainKey");
		String icon = RequestUtils.getStringValue(request, "icon");
		CmPri pri = RequestUtils.getValue(request, "pri", CmPri.class);

		DataPO po;
		if (isCreate == 1) {// 新增
			po = new DataPO("CmDomain");
			po.set("domainKey", domainKey);
			po.set("sysFlag", 0);
			po.set("columns", "50;50");
			po.set("sort", 999);
		} else {// 编辑
			po = new DataPO("CmDomain", (Map<String, Object>) ORMService.getInstance().findByPk("CmDomain", domainKey));
		}
		po.set("icon", icon);
		po.set("description", description);
		po.set("busiName", busiName);
		pri.setDevelopmentInfo(po);
		po.set("pri", pri);

		MenuService service = BeanFactory.getInstance().getBean(MenuService.class);
		if (isCreate == 1) {
			service.save(po.toEntity());
		} else {
			service.update(po.toEntity());
		}

		Actions.redirectInfoPage(request, response, "编辑域[" + busiName + "]成功.");
	}

	/**
	 * 首页配置提交
	 * 
	 * @param request
	 * @param response
	 */
	@ActionAccess(level = SafeLevel.DEV_W)
	public void submitDomainHomeForm(HttpServletRequest request, HttpServletResponse response) {
		String[] pixels = RequestUtils.getStringValues(request, "homes");
		String domainKey = RequestUtils.getStringValue(request, "domainKey");
		String columns = RequestUtils.getStringValue(request, "columns");

		List<Map<String, Object>> batchList = new ArrayList<>();
		if (pixels != null) {
			for (String pixel : pixels) {
				String id = RequestUtils.getStringValue(request, pixel + ".id");
				DataPO po;
				if (StringUtils.isEmpty(id)) {// 新增
					po = new DataPO("CmHome");
					po.set("id", IDGenerator.next());
					po.set("sysFlag", 0);
				} else {// 编辑
					po = new DataPO("CmHome", (Map<String, Object>) ORMService.getInstance().findByPk("CmHome", id));
				}

				String action = RequestUtils.getStringValue(request, pixel + ".action");
				String name = RequestUtils.getStringValue(request, pixel + ".name");
				Integer paramType = RequestUtils.getIntegerValue(request, pixel + ".paramType");
				String paramScript = RequestUtils.getStringValue(request, pixel + ".paramScript");
				Integer sort = RequestUtils.getIntegerValue(request, pixel + ".sort");
				Integer columnIndex = RequestUtils.getIntegerValue(request, pixel + ".columnIndex");
				Integer height = RequestUtils.getIntegerValue(request, pixel + ".height");
				po.set("domainKey", domainKey);
				po.set("action", action);
				po.set("name", name);
				po.set("paramType", paramType);
				po.set("paramScript", paramScript);
				po.set("sort", sort);
				po.set("columnIndex", columnIndex);
				po.set("height", height);
				CmPri pri = RequestUtils.getValue(request, pixel + ".pri", CmPri.class);
				pri.setDevelopmentInfo(po);
				po.set("pri", pri);
				batchList.add(po.toEntity());
			}
		}
		BeanFactory.getInstance().getBean(MenuService.class).executeSaveHome(domainKey, columns, batchList);
		Actions.redirectInfoPage(request, response, "保存成功.");
	}

	/**
	 * 增加首页标签
	 * 
	 * @param request
	 * @param response
	 */
	public void domainHomeTab(HttpServletRequest request, HttpServletResponse response) {
		Actions.includePage(request, response, Util.getPagePath(request, "home_form.jsp"));
	}

	/**
	 * 位置调整
	 * 
	 * @param request
	 * @param response
	 */
	public void changeHomePosition(HttpServletRequest request, HttpServletResponse response) {
		String strColumns = RequestUtils.getStringValue(request, "columns");
		String[] columns = strColumns.split(";");
		request.setAttribute("columns", strColumns);
		request.setAttribute("size", columns.length);
		request.setAttribute("json", RequestUtils.getStringValue(request, "json"));

		Actions.includePage(request, response, Util.getPagePath(request, "home_position.jsp"));
	}
}
