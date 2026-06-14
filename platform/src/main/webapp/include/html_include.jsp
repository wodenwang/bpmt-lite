
<%
	/**
	 * 用于在界面引入javascript和css等
	 *
	 */
%>
<%@ page language="java" pageEncoding="UTF-8"%>

<%-- css引入 --%>
<c:choose>
	<c:when test="${_ext_style==null||_ext_style==''}">
		<link href="${_cp}/css/themes/${_style!=null?_style:'smoothness'}/jquery-ui.min.css" rel="stylesheet">
	</c:when>
	<c:otherwise>
		<link href="${_ext_style}" rel="stylesheet">
	</c:otherwise>
</c:choose>
<link href="${_cp}/css/jquery-ui-ext.css?v=20260613-table-hover" rel="stylesheet">
<link href="${_cp}/css/styles.css" rel="stylesheet">
<%-- 深浅样式 --%>
<link href="${_cp}/css/styles_${_backgroud_style=='1'?'dark':'light'}.css" rel="stylesheet">
<%-- v1.8.0+ 桌面后台清晰度覆盖层，最后加载以便可单文件回滚 --%>
<link href="${_cp}/css/bpmt-modern.css?v=20260614-v190-shell" rel="stylesheet">

<%-- 公共js函数 --%>
<script>
	var _cp = '${_cp}';
	var _action = '${_action}';//当前action路径
	var _acp = '${_acp}';//当前action路径
</script>

<%-- js引入  --%>
<script src="${_cp}/js/jquery-1.11.3.min.js"></script>
<script src="${_cp}/js/jquery-migrate-1.2.1.min.js"></script>
<script src="${_cp}/js/jquery-ui-1.11.4.min.js"></script>

<script src="${_cp}/js/jquery-ui-timepicker-addon.js"></script>
<script src="${_cp}/js/jquery.form.min.js"></script>
<script src="${_cp}/js/jquery.validate.js"></script>
<script src="${_cp}/js/jquery.metadata.js"></script>
<script src="${_cp}/js/jquery.scrollTo.min.js"></script>
<script src="${_cp}/js/jquery.dialogextend.min.js"></script>

<%--ueditor引入 --%>
<script type="text/javascript" charset="utf-8" src="${_cp}/ueditor/ueditor.config.js"></script>
<script type="text/javascript" charset="utf-8" src="${_cp}/ueditor/ueditor.all.min.js"></script>
<script type="text/javascript" charset="utf-8" src="${_cp}/ueditor/lang/zh-cn/zh-cn.js"></script>

<%-- ztree引入 --%>
<link rel="stylesheet" href="${_cp}/js/ztree/zTreeStyle.css" type="text/css">
<script src="${_cp}/js/ztree/jquery.ztree.all-3.5.js"></script>

<%--chosen引入 --%>
<link rel="stylesheet" href="${_cp}/js/chosen/chosen.css" type="text/css">
<script src="${_cp}/js/chosen/jquery.chosen.js"></script>

<%--gridster引入 --%>
<link rel="stylesheet" href="${_cp}/js/gridster/jquery.gridster.min.css" type="text/css">
<script src="${_cp}/js/gridster/jquery.gridster.min.js"></script>

<%--codemirror引入 --%>
<link rel="stylesheet" href="${_cp}/js/codemirror/lib/codemirror.css" type="text/css">
<link rel="stylesheet" href="${_cp}/js/codemirror/theme/eclipse.css">
<script src="${_cp}/js/codemirror/lib/codemirror.js"></script>
<script src="${_cp}/js/codemirror/addon/selection/active-line.js"></script>
<script src="${_cp}/js/codemirror/addon/edit/matchbrackets.js"></script>
<script src="${_cp}/js/codemirror/addon/mode/loadmode.js"></script>
<script src="${_cp}/js/codemirror/mode/meta.js"></script>

<%-- icheck引入 --%>
<link href="${_cp}/js/icheck/skins/all.css?v=1.0.2" rel="stylesheet">
<script src="${_cp}/js/icheck/icheck.min.js?v=1.0.2"></script>

<%-- colorpicker引入 --%>
<link rel="stylesheet" href="${_cp}/js/colorpicker/colorPicker.css" type="text/css">
<script src="${_cp}/js/colorpicker/jquery.colorPicker.min.js"></script>

<%-- xheditor引入 --%>
<script type="text/javascript" src="${_cp}/js/xheditor/xheditor-1.2.2.min.js"></script>
<script type="text/javascript" src="${_cp}/js/xheditor/xheditor_lang/zh-cn.js"></script>

<%--magnific-popup引入 --%>
<link rel="stylesheet" href="${_cp}/js/magnific-popup/magnific-popup.css" type="text/css">
<script type="text/javascript" src="${_cp}/js/magnific-popup/jquery.magnific-popup.min.js"></script>

<%-- echarts引入 --%>
<script src="${_cp}/js/echarts-2.2.1/build/dist/echarts-all.js"></script>

<%-- pdf object引入 --%>
<script src="${_cp}/js/pdfobject/pdfobject.js"></script>

<%-- ui totop引入 --%>
<link rel="stylesheet" href="${_cp}/js/uitotop/ui.totop.css" type="text/css">
<script src="${_cp}/js/uitotop/easing.js"></script>
<script src="${_cp}/js/uitotop/jquery.ui.totop.min.js"></script>

<%-- plupload引入 --%>
<link rel="stylesheet" href="${_cp}/js/plupload/jquery.ui.plupload/css/jquery.ui.plupload.css" type="text/css" />
<script type="text/javascript" src="${_cp}/js/plupload/plupload.full.min.js"></script>
<script type="text/javascript" src="${_cp}/js/plupload/jquery.ui.plupload/jquery.ui.plupload.min.js"></script>
<script type="text/javascript" src="${_cp}/js/plupload/i18n/${wpf:lan('#:zh[zh_CN]:en[en]#')}.js"></script>

<script src="${_cp}/js/additional-methods.js"></script>

<c:if test="${wpf:lan('#:zh[1]:en[0]#')=='1'}">
	<script src="${_cp}/js/localization/jquery.validate.message.js"></script>
	<script src="${_cp}/js/localization/jquery.ui.datepicker-zh-CN.js"></script>
	<script src="${_cp}/js/localization/jquery-ui-timepicker-zh-CN.js"></script>
</c:if>

<script type="text/javascript">
	var _lan = "${wpf:lan('#:zh[zh]:en[en]#')}";//当前语言
</script>

<script src="${_cp}/js/jquery-ui-ext.js"></script>
<script src="${_cp}/js/ws-ajax.js"></script>
<script src="${_cp}/js/ws-ajax-ext.js"></script>
<script src="${_cp}/js/ws-core.js"></script>
<script src="${_cp}/js/ws-core-ext.js?v=20260614-v190-empty-state"></script>
<script src="${_cp}/js/ws-ui.js"></script>
<script src="${_cp}/js/ws-ui-ext.js"></script>
<script src="${_cp}/js/ws-widget.js"></script>
