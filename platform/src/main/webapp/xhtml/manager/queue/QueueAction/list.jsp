<%@ page language="java" pageEncoding="UTF-8" %>

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/common.jsp" %>
<%@ include file="/include/html_head.jsp" %>

<script type="text/javascript">
    $(function () {
        var $zone = $('#${_zone}');

        //修改
        $('button[name=edit]').click(function () {
            var val = $(this).val();
            Core.fn($zone, 'edit')(val);
        });

        //删除
        $('button[name=del]').click(function () {
            var val = $(this).val();
            Core.fn($zone, 'del')(val);
        });

        //新建
        $('button[name=create]').click(function () {
            Core.fn($zone, 'create')();
        });

    });
</script>

<%--数据表格 --%>
<table class="ws-table" form="${_zone}_form">
    <thead>
    <tr>
        <th style="width: 120px;">操作</th>
        <th field="queueKey">队列KEY</th>
        <th field="description">描述</th>
        <th>队列表</th>
        <th>日志表</th>
        <th field="createUid">创建人</th>
        <th field="createDate">创建时间</th>
        <th field="updateDate">更新时间</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${dp.list}" var="vo">
        <tr>
            <td class="center ws-group">
                <button icon="trash" text="false" type="button" name="del"
                        value="${vo.queueKey}">删除
                </button>
                <button icon="wrench" text="false" type="button" name="edit"
                        value="${vo.queueKey}">修改
                </button>
            </td>
            <td class="center">${vo.queueKey}</td>
            <td class="left">${vo.description}</td>
            <td class="center">${vo.tableName}</td>
            <td class="center">${vo.logTableName}</td>
            <td class="center">${wcm:widget('select[$com.riversoft.platform.po.UsUser;uid;busiName]',vo.createUid)}</td>
            <td class="center">${wcm:widget('date',vo.createDate)}</td>
            <td class="center">${wcm:widget('date',vo.updateDate)}</td>
        </tr>
    </c:forEach>
    <c:if test="${fn:length(dp.list)<1}">
        <tr class="bpmt-empty-table-row">
            <td colspan="99">
                <div class="bpmt-state bpmt-state-empty">
                    <strong>没有匹配的队列配置</strong>
                    <span>当前查询条件下没有队列配置。请调整筛选条件，或点击“重置查询”后重新查询。</span>
                </div>
            </td>
        </tr>
    </c:if>
    </tbody>
    <tr>
        <th class="ws-bar">
            <div class="ws-group right">
                <button type="button" icon="plus" text="true" name="create">新建队列配置</button>
            </div>
        </th>
    </tr>
</table>

<%-- 分页  --%>
<wcm:page dp="${dp}" form="${_zone}_form"/>

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/html_bottom.jsp" %>
