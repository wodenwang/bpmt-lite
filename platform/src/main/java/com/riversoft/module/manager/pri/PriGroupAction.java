/*
 * $HeadURL: $
 * $Id: $
 * Copyright (c) 2013 by Riversoft System, all rights reserved.
 */
package com.riversoft.module.manager.pri;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.riversoft.core.BeanFactory;
import com.riversoft.core.IDGenerator;
import com.riversoft.core.db.DataCondition;
import com.riversoft.core.db.DataPO;
import com.riversoft.core.db.ORMService;
import com.riversoft.core.db.ORMService.QueryVO;
import com.riversoft.core.exception.ExceptionType;
import com.riversoft.core.exception.SystemRuntimeException;
import com.riversoft.core.web.Actions;
import com.riversoft.core.web.Actions.Util;
import com.riversoft.core.web.RequestUtils;
import com.riversoft.core.web.annotation.ActionAccess;
import com.riversoft.core.web.annotation.ActionAccess.SafeLevel;
import com.riversoft.platform.po.CmPri;
import com.riversoft.platform.po.CmPriGroupRelate;
import com.riversoft.platform.po.UsGroup;
import com.riversoft.platform.po.UsRole;
import com.riversoft.platform.po.VwUrl;
import com.riversoft.platform.service.PriService;
import com.riversoft.platform.web.view.ViewActionBuilder;
import com.riversoft.platform.web.view.ViewActionBuilder.ViewVO;
import com.riversoft.platform.web.view.annotation.PriConfigMethod;
import com.riversoft.util.jackson.JsonMapper;

/**
 * 权限组管理
 * 
 * @author woden
 * 
 */
@ActionAccess(level = SafeLevel.DEV_R)
public class PriGroupAction {

	private PriGroupService service = BeanFactory.getInstance().getBean(PriGroupService.class);

	/**
	 * 框架页
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
	@SuppressWarnings("unchecked")
	public void tree(HttpServletRequest request, HttpServletResponse response) {
		List<Map<String, Object>> list = ORMService.getInstance().query("CmPriGroup",
				new DataCondition().setOrderByAsc("sort").toEntity());
		String cp = Actions.Util.getContextPath(request);
		for (Map<String, Object> o : list) {
			if (((Integer) o.get("leafFlag")).intValue() == 1) {// 叶子
				o.put("icon", cp + "/css/icon/user_key.png");
			} else {
				o.put("icon", cp + "/css/icon/folder_key.png");
			}
		}

		request.setAttribute("tree", JsonMapper.defaultMapper().toJson(list));
		Actions.includePage(request, response, Util.getPagePath(request, "tree.jsp"));
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
		service.executeSaveSort((List<HashMap<String, Object>>) tree.get("nodes"));
		Actions.redirectInfoPage(request, response, "保存位置成功.");
	}

	/**
	 * 删除节点
	 *
	 * @param request
	 * @param response
	 */
	@ActionAccess(level = SafeLevel.DEV_W)
	public void delNode(HttpServletRequest request, HttpServletResponse response) {
		String groupId = RequestUtils.getStringValue(request, "groupId");
		service.removeByPk("CmPriGroup", groupId);
		Actions.redirectInfoPage(request, response, "删除节点成功.");
	}

	/**
	 * 编辑节点
	 *
	 * @param request
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	public void editZone(HttpServletRequest request, HttpServletResponse response) {
		String groupId = RequestUtils.getStringValue(request, "groupId");
		Map<String, Object> po = ((Map<String, Object>) service.findByPk("CmPriGroup", groupId));
		if (po == null) {
			throw new SystemRuntimeException(ExceptionType.BUSINESS, "对象不存在.");
		}

		request.setAttribute("vo", po);
		Actions.includePage(request, response, Util.getPagePath(request, "form.jsp"));
	}

	/**
	 * 新增节点
	 *
	 * @param request
	 * @param response
	 */
	public void createZone(HttpServletRequest request, HttpServletResponse response) {
		Actions.includePage(request, response, Util.getPagePath(request, "form.jsp"));
	}

	/**
	 * 表单提交
	 *
	 * @param request
	 * @param response
	 */
	@ActionAccess(level = SafeLevel.DEV_W)
	public void submitForm(HttpServletRequest request, HttpServletResponse response) {

		Integer isCreate = RequestUtils.getIntegerValue(request, "isCreate");
		String name = RequestUtils.getStringValue(request, "name");
		String description = RequestUtils.getStringValue(request, "description");
		String groupId = RequestUtils.getStringValue(request, "groupId");
		String parentId = RequestUtils.getStringValue(request, "parentId");
		Integer sort = RequestUtils.getIntegerValue(request, "sort");
		Integer leafFlag = RequestUtils.getIntegerValue(request, "leafFlag");

		DataPO po;
		if (isCreate == 1) {// 新增
			po = new DataPO("CmPriGroup");
			po.set("groupId", IDGenerator.next());
		} else {// 编辑
			po = new DataPO("CmPriGroup", (Map<String, Object>) ORMService.getInstance()
					.findByPk("CmPriGroup", groupId));
		}

		po.set("description", description);
		po.set("name", name);
		po.set("parentId", parentId);
		po.set("parentId", parentId);
		po.set("leafFlag", leafFlag);
		po.set("sort", sort);

		if (isCreate == 1) {
			service.save(po.toEntity());
		} else {
			service.update(po.toEntity());
		}

		Actions.redirectInfoPage(request, response, "保存[" + name + "]成功.");
	}

	/**
	 * 保存权限设置
	 *
	 * @param request
	 * @param response
	 */
	@ActionAccess(level = SafeLevel.DEV_W)
	public void submitPris(HttpServletRequest request, HttpServletResponse response) {
		String groupId = RequestUtils.getStringValue(request, "groupId");
		List<CmPriGroupRelate> relates = RequestUtils.getValues(request, "relate", CmPriGroupRelate.class);
		String[] checkedPriKeys = RequestUtils.getStringValues(request, "priKey");

		PriService service = BeanFactory.getInstance().getSingleBean(PriService.class);
		service.executePriGroupRelate(groupId, checkedPriKeys, relates);

		Actions.redirectInfoPage(request, response, "保存成功.");
	}

	/**
	 * 菜单,域权限设置
	 *
	 * @param request
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	public void menuPri(HttpServletRequest request, HttpServletResponse response) {
		final String cp = Actions.Util.getContextPath(request);
		String groupId = RequestUtils.getStringValue(request, "groupId");

		// priKeys
		List<String> singlePriKeys = service.queryHQL("select distinct priKey from CmPriGroupRelate where groupId = ?",
				groupId);
		List<String> singleCatelogKeys = singlePriKeys.size() > 0 ? service.queryHQL("select distinct catelogKey from "
				+ CmPri.class.getName() + " where catelogType = :catelogType and priKey in (:list)", new QueryVO(
				"catelogType", CmPri.Catelog.MENU.getCode()), new QueryVO("list", singlePriKeys))
				: Collections.EMPTY_LIST;

		// 组合客户端树
		List<Map<String, Object>> menuList = new ArrayList<>();
		List<CmPri> pris = new ArrayList<>();

		// 找出已被选的记录
		List<String> checkPriKeys = ORMService.getInstance().queryHQL(
				"select priKey from CmPriGroupRelate where groupId = ?", groupId);

		// 查找子系统
		List<Map<String, Object>> domains = ORMService.getInstance().query("CmDomain",
				new DataCondition().setOrderByAsc("sort").toEntity());
		for (Map<String, Object> domain : domains) {
			Map<String, Object> obj = new HashMap<>();
			CmPri pri = (CmPri) domain.get("pri");
			if (pri.getType() == 1) {
				if (checkPriKeys.contains(pri.getPriKey())) {// 默认选中
					obj.put("checked", true);
				}
			} else {
				obj.put("chkDisabled", true);// 不允许选择
			}
			obj.put("pri", pri);
			obj.put("id", "domain_" + domain.get("domainKey"));// domain的ID增加前缀,避免与menu重复
			obj.put("name", domain.get("busiName"));
			obj.put("parentId", null);// 域做为根
			obj.put("icon", cp + "/css/icon/house.png");
			obj.put("title", "");

			if (singleCatelogKeys.contains(domain.get("domainKey"))) {
				obj.put("font", "{'font-weight':'bold','color':'red'}");
				obj.put("title", "直接关联");
			}

			pris.add(pri);
			menuList.add(obj);

			// 首页
			{
				Map<String, Object> o = new HashMap<>();
				o.put("id", "index_" + domain.get("domainKey"));// domain的ID增加前缀,避免与menu重复
				o.put("name", "首页");
				o.put("parentId", "domain_" + domain.get("domainKey"));// 域做为根
				o.put("icon", cp + "/css/icon/house.png");
				o.put("font", "{'font-weight':'bold','color':'gray'}");
				o.put("chkDisabled", true);// 不允许选中
				o.put("title", "");
				menuList.add(o);
			}
		}

		// 查找菜单
		List<Map<String, Object>> menus = ORMService.getInstance().query("CmMenu",
				new DataCondition().setOrderByAsc("sort").toEntity());
		for (Map<String, Object> menu : menus) {
			Map<String, Object> obj = new HashMap<>();
			CmPri pri = (CmPri) menu.get("pri");
			if (pri.getType() == 1) {
				if (checkPriKeys.contains(pri.getPriKey())) {// 默认选中
					obj.put("checked", true);
				}
			} else {
				obj.put("chkDisabled", true);// 不允许选择
			}
			obj.put("pri", pri);
			obj.put("id", menu.get("id"));// domain的ID增加前缀,避免与menu重复
			obj.put("name", menu.get("name"));
			String icon = (String) menu.get("icon");
			obj.put("title", "");
			if (StringUtils.isNotEmpty(icon)) {
				obj.put("icon", cp + "/css/icon/" + icon);
			}
			if (StringUtils.isEmpty((String) menu.get("parentId")) || "null".equals(menu.get("parentId"))) {
				obj.put("parentId", "domain_" + menu.get("domainKey"));// 域做为上一级
			} else {
				obj.put("parentId", menu.get("parentId"));
			}

			if (singleCatelogKeys.contains(menu.get("id"))) {
				obj.put("font", "{'font-weight':'bold','color':'red'}");
				obj.put("title", "直接关联");
			}

			pris.add(pri);
			menuList.add(obj);
		}

		// 查找首页配置
		List<Map<String, Object>> homes = ORMService.getInstance().query("CmHome",
				new DataCondition().setOrderByAsc("sort").toEntity());
		for (Map<String, Object> home : homes) {
			Map<String, Object> obj = new HashMap<>();
			CmPri pri = (CmPri) home.get("pri");
			if (pri.getType() == 1) {
				if (checkPriKeys.contains(pri.getPriKey())) {// 默认选中
					obj.put("checked", true);
				}
			} else {
				obj.put("chkDisabled", true);// 不允许选择
			}
			obj.put("pri", pri);
			obj.put("id", home.get("id"));// domain的ID增加前缀,避免与menu重复
			obj.put("name", home.get("name"));
			obj.put("icon", cp + "/css/icon/tab.png");
			obj.put("parentId", "index_" + home.get("domainKey"));
			obj.put("title", "");

			if (singleCatelogKeys.contains(home.get("id"))) {
				obj.put("font", "{'font-weight':'bold','color':'red'}");
				obj.put("title", "直接关联");
			}

			pris.add(pri);
			menuList.add(obj);
		}

		// 处理icon属性,为每个icon增加${_cp}值
		request.setAttribute("groupId", groupId);
		request.setAttribute("pris", pris);
		request.setAttribute("menu", JsonMapper.defaultMapper().toJson(menuList));
		Actions.includePage(request, response, Util.getPagePath(request, "menu_pri.jsp"));
	}

	/**
	 * 视图权限
	 *
	 * @param request
	 * @param response
	 */
	public void viewPri(HttpServletRequest request, HttpServletResponse response) {
		final String cp = Actions.Util.getContextPath(request);
		String groupId = RequestUtils.getStringValue(request, "groupId");

		// priKeys
		List<String> singlePriKeys = service.queryHQL("select distinct priKey from CmPriGroupRelate where groupId = ?",
				groupId);
		List<String> singleCatelogKeys = singlePriKeys.size() > 0 ? service.queryHQL("select distinct catelogKey from "
				+ CmPri.class.getName() + " where catelogType = :catelogType and priKey in (:list)", new QueryVO(
				"catelogType", CmPri.Catelog.VIEW.getCode()), new QueryVO("list", singlePriKeys))
				: Collections.EMPTY_LIST;

		// 组合客户端树
		List<Map<String, Object>> treeList = new ArrayList<>();
		// 视图分类
		for (ViewVO module : ViewActionBuilder.getInstance().getViewModuleList()) {
			// 增加视图根节点
			Map<String, Object> obj = new HashMap<>();
			obj.put("name", module.getDescription());
			obj.put("id", "_view_" + module.getName());
			obj.put("icon", cp + "/css/icon/bookmark.png");
			treeList.add(obj);
		}

		// 查找视图
		List<VwUrl> views = ORMService.getInstance().query(VwUrl.class.getName(),
				new DataCondition().setOrderByAsc("description").toEntity());
		for (VwUrl view : views) {
			Map<String, Object> obj = new HashMap<>();
			obj.put("id", "view_" + view.getViewKey());
			obj.put("viewKey", view.getViewKey());
			obj.put("name", view.getDescription());
			obj.put("parentId", "_view_" + view.getViewClass());

			String action;
			PriConfigMethod annotation = ViewActionBuilder.getInstance().getViewClass(view.getViewClass())
					.getAnnotation(PriConfigMethod.class);
			if (annotation == null) {// 默认设置方法
				action = "/development/view/ViewConfigAction/priList.shtml";
			} else {
				action = Actions.Util.getActionUrl(ViewActionBuilder.getInstance().getViewClass(view.getViewClass()))
						+ "/" + annotation.value() + ".shtml";
			}
			obj.put("action", action);
			obj.put("icon", cp + "/css/icon/image.png");
			obj.put("title", "");

			if (singleCatelogKeys.contains(view.getViewKey())) {
				obj.put("font", "{'font-weight':'bold','color':'red'}");
				obj.put("title", "直接关联");
			}

			treeList.add(obj);
		}

		request.setAttribute("groupId", groupId);
		request.setAttribute("tree", JsonMapper.defaultMapper().toJson(treeList));
		Actions.includePage(request, response, Util.getPagePath(request, "view_pri.jsp"));
	}

	/**
	 * 控件权限
	 *
	 * @param request
	 * @param response
	 */
	public void widgetPri(HttpServletRequest request, HttpServletResponse response) {
		final String cp = Actions.Util.getContextPath(request);
		String groupId = RequestUtils.getStringValue(request, "groupId");

		List<String> singlePriKeys = service.queryHQL("select distinct priKey from CmPriGroupRelate where groupId = ?",
				groupId);
		List<String> singleCatelogKeys = singlePriKeys.size() > 0 ? service.queryHQL("select distinct catelogKey from "
				+ CmPri.class.getName() + " where catelogType = :catelogType and priKey in (:list)", new QueryVO(
				"catelogType", CmPri.Catelog.WIDGET.getCode()), new QueryVO("list", singlePriKeys))
				: Collections.EMPTY_LIST;

		// 组合客户端树
		List<Map<String, Object>> treeList = new ArrayList<>();
		{
			// 增加控件根节点
			Map<String, Object> obj = new HashMap<>();
			obj.put("name", "自定义控件");
			obj.put("id", "_widget");
			obj.put("parentId", null);
			obj.put("icon", cp + "/css/icon/folder_explore.png");
			treeList.add(obj);
		}

		// 查找控件
		List<Map<String, Object>> widgets = ORMService.getInstance().query("WdgBase",
				new DataCondition().setOrderByAsc("busiName").toEntity());
		for (Map<String, Object> widget : widgets) {
			Map<String, Object> obj = new HashMap<>();
			obj.put("id", "widget_" + widget.get("widgetKey"));
			obj.put("widgetKey", widget.get("widgetKey"));
			obj.put("name", widget.get("busiName"));
			obj.put("parentId", "_widget");
			obj.put("action", "/development/widget/WidgetConfigAction/priList.shtml");
			obj.put("icon", cp + "/css/icon/zoom.png");
			obj.put("title", "");

			if (singleCatelogKeys.contains(widget.get("widgetKey"))) {
				obj.put("font", "{'font-weight':'bold','color':'red'}");
				obj.put("title", "直接关联");
			}
			treeList.add(obj);
		}

		request.setAttribute("groupId", groupId);
		request.setAttribute("tree", JsonMapper.defaultMapper().toJson(treeList));
		Actions.includePage(request, response, Util.getPagePath(request, "view_pri.jsp"));
	}

	/**
	 * 第三方系统权限
	 *
	 * @param request
	 * @param response
	 */
	public void thirdpartPri(HttpServletRequest request, HttpServletResponse response) {
		final String cp = Actions.Util.getContextPath(request);
		String groupId = RequestUtils.getStringValue(request, "groupId");

		List<String> singlePriKeys = service.queryHQL("select distinct priKey from CmPriGroupRelate where groupId = ?",
				groupId);
		List<String> singleCatelogKeys = singlePriKeys.size() > 0 ? service.queryHQL("select distinct catelogKey from "
				+ CmPri.class.getName() + " where catelogType = :catelogType and priKey in (:list)", new QueryVO(
				"catelogType", CmPri.Catelog.THIRDPART.getCode()), new QueryVO("list", singlePriKeys))
				: Collections.EMPTY_LIST;

		List<String> checkPriKeys = ORMService.getInstance().queryHQL(
				"select priKey from CmPriGroupRelate where groupId = ?", groupId);
		List<Map<String, Object>> treeList = new ArrayList<>();
		List<CmPri> pris = new ArrayList<>();

		{
			Map<String, Object> root = new HashMap<>();
			root.put("name", "第三方系统");
			root.put("id", "_thirdpart");
			root.put("parentId", null);
			root.put("icon", cp + "/css/icon/folder_explore.png");
			root.put("chkDisabled", true);
			root.put("title", "");
			treeList.add(root);
		}

		List<Map<String, Object>> thirdparts = ORMService.getInstance().query("CmThirdpart",
				new DataCondition().setOrderByAsc("thirdpartName").toEntity());
		for (Map<String, Object> thirdpart : thirdparts) {
			CmPri pri = (CmPri) thirdpart.get("pri");
			if (pri == null) {
				continue;
			}

			Map<String, Object> obj = new HashMap<>();
			if (pri.getType() == 1) {
				if (checkPriKeys.contains(pri.getPriKey())) {
					obj.put("checked", true);
				}
			} else {
				obj.put("chkDisabled", true);
			}
			obj.put("pri", pri);
			obj.put("id", "thirdpart_" + thirdpart.get("thirdpartKey"));
			obj.put("name", thirdpart.get("thirdpartName"));
			obj.put("parentId", "_thirdpart");
			obj.put("icon", cp + "/css/icon/link_edit.png");
			obj.put("title", "");

			if (singleCatelogKeys.contains(thirdpart.get("thirdpartKey"))) {
				obj.put("font", "{'font-weight':'bold','color':'red'}");
				obj.put("title", "直接关联");
			}

			pris.add(pri);
			treeList.add(obj);
		}

		request.setAttribute("groupId", groupId);
		request.setAttribute("pris", pris);
		request.setAttribute("menu", JsonMapper.defaultMapper().toJson(treeList));
		Actions.includePage(request, response, Util.getPagePath(request, "menu_pri.jsp"));
	}

	/**
	 * 权限归属
	 *
	 * @param request
	 * @param response
	 */
	public void roleList(HttpServletRequest request, HttpServletResponse response) {
		String groupId = RequestUtils.getStringValue(request, "groupId");

		Set<String> sigleList = new HashSet<>();
		sigleList.addAll(service.queryHQL("select distinct roleKey from UsRolePriGroupRelate where groupId = ?",
				groupId));
		sigleList.addAll(service.queryHQL(
				"select CONCAT(groupKey,';',roleKey) as key from UsRoleGroupPriRelate where groupId = ?", groupId));

		// 组织架构树
		List<UsGroup> groups = ORMService.getInstance().query(UsGroup.class.getName(),
				new DataCondition().setOrderByAsc("sort").toEntity());
		final String cp = Actions.Util.getContextPath(request);
		List<Map<String, Object>> treeList = new ArrayList<>();
		for (UsGroup group : groups) {
			Map<String, Object> obj = new HashMap<>();
			obj.put("icon", cp + "/css/icon/house.png");
			obj.put("busiName", group.getBusiName());
			obj.put("id", group.getGroupKey());
			obj.put("parentId", group.getParentKey());
			obj.put("groupKey", group.getGroupKey());
			obj.put("sysFlag", group.getSysFlag());
			obj.put("sort", group.getSort());
			obj.put("title", "");
			treeList.add(obj);

			List<Map<String, Object>> relates = ORMService.getInstance().query("UsGroupRole",
					new DataCondition().setStringEqual("groupKey", group.getGroupKey()).toEntity());

			for (Map<String, Object> relate : relates) {
				Map<String, Object> o = new HashMap<>();
				UsRole role = (UsRole) ORMService.getInstance().findByPk(UsRole.class.getName(),
						(Serializable) relate.get("roleKey"));
				if (role == null) {
					continue;
				}
				o.put("icon", cp + "/css/icon/vcard.png");
				o.put("busiName", role.getBusiName());
				o.put("id", "role_" + role.getRoleKey());
				o.put("parentId", group.getGroupKey());
				o.put("roleKey", role.getRoleKey());
				o.put("sysFlag", relate.get("sysFlag"));
				o.put("sort", role.getSort() - 999);// 部门里面角色要排在组织上面
				o.put("title", "");
				if (sigleList.contains(role.getRoleKey())) {
					o.put("font", "{'font-weight':'bold','color':'red'}");
					o.put("title", "角色直接关联");
				} else if (sigleList.contains(group.getGroupKey() + ";" + role.getRoleKey())) {
					o.put("font", "{'font-weight':'bold','color':'red'}");
					o.put("title", "组织+角色直接关联");
				}
				treeList.add(o);
			}
		}

		// 排序
		Collections.sort(treeList, new Comparator<Map<String, Object>>() {
			@Override
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				int sort1 = o1.containsKey("sort") ? (int) o1.get("sort") : -999;
				int sort2 = o2.containsKey("sort") ? (int) o2.get("sort") : -999;
				if (sort1 < sort2) {
					return -1;
				} else if (sort1 == sort2) {
					return 0;
				} else {
					return 1;
				}
			}
		});

		request.setAttribute("tree", JsonMapper.defaultMapper().toJson(treeList));

		Actions.includePage(request, response, Util.getPagePath(request, "role_list.jsp"));
	}
}
