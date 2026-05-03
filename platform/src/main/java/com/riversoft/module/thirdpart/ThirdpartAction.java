package com.riversoft.module.thirdpart;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.riversoft.core.db.DataCondition;
import com.riversoft.core.db.DataPackage;
import com.riversoft.core.db.ORMService;
import com.riversoft.core.exception.ExceptionType;
import com.riversoft.core.exception.SystemRuntimeException;
import com.riversoft.core.web.Actions;
import com.riversoft.core.web.RequestUtils;
import com.riversoft.core.web.annotation.ActionAccess;
import com.riversoft.core.web.annotation.ActionAccess.SafeLevel;
import com.riversoft.platform.po.CmPri;

@ActionAccess(level = SafeLevel.DEV_R)
public class ThirdpartAction {
    private static final String ENTITY_NAME = "CmThirdpart";

    public void index(HttpServletRequest request, HttpServletResponse response) {
        includePage(request, response, "main.jsp");
    }

    public void list(HttpServletRequest request, HttpServletResponse response) {
        DataPackage dp = queryAll(request);
        request.setAttribute("dp", dp);
        includePage(request, response, "list.jsp");
    }

    public void createZone(HttpServletRequest request, HttpServletResponse response) {
        includePage(request, response, "form.jsp");
    }

    public void editZone(HttpServletRequest request, HttpServletResponse response) {
        String thirdpartKey = RequestUtils.getStringValue(request, "thirdpartKey");
        Map<String, Object> vo = findByPk(thirdpartKey);
        if (vo == null) {
            throw new SystemRuntimeException(ExceptionType.BUSINESS, "外部系统[" + thirdpartKey + "]不存在.");
        }
        request.setAttribute("vo", vo);
        includePage(request, response, "form.jsp");
    }

    @ActionAccess(level = SafeLevel.DEV_W)
    public void submitForm(HttpServletRequest request, HttpServletResponse response) {
        String thirdpartKey = RequestUtils.getStringValue(request, "thirdpartKey");
        Map<String, Object> input = readInput(request);
        CmPri pri = readPri(request);

        boolean isCreate = "1".equals(RequestUtils.getStringValue(request, "isCreate"));
        if (isCreate) {
            String clientSecret = service().createThirdpart(input, pri);
            redirectInfoPage(request, response, "新增成功. clientSecret只展示一次: " + clientSecret);
        } else {
            service().updateThirdpart(thirdpartKey, input, pri);
            redirectInfoPage(request, response, "保存成功.");
        }
    }

    @ActionAccess(level = SafeLevel.DEV_W)
    public void toggleActive(HttpServletRequest request, HttpServletResponse response) {
        String thirdpartKey = RequestUtils.getStringValue(request, "thirdpartKey");
        Map<String, Object> entity = findByPk(thirdpartKey);
        if (entity == null) {
            throw new SystemRuntimeException(ExceptionType.BUSINESS, "外部系统[" + thirdpartKey + "]不存在.");
        }

        String requestedFlag = RequestUtils.getStringValue(request, "activeFlag");
        Object activeFlag = StringUtils.isBlank(requestedFlag) ? Integer.valueOf(currentActiveFlag(entity) == 1 ? 0 : 1)
                : requestedFlag;
        Map<String, Object> input = new HashMap<String, Object>();
        input.put("activeFlag", activeFlag);
        service().updateThirdpart(thirdpartKey, input, null);
        redirectInfoPage(request, response,
                Integer.valueOf(1).equals(activeFlag) || "1".equals(activeFlag) ? "启用成功." : "停用成功.");
    }

    protected ThirdpartService service() {
        return new ThirdpartService();
    }

    protected DataPackage queryAll(HttpServletRequest request) {
        int start = Actions.Util.getStart(request);
        int limit = Actions.Util.getLimit(request);
        String field = Actions.Util.getSortField(request);
        String dir = Actions.Util.getSortDir(request);
        if (StringUtils.isBlank(field)) {
            field = "thirdpartKey";
        }
        if (StringUtils.isBlank(dir)) {
            dir = "asc";
        }
        DataCondition condition = new DataCondition(Actions.Util.buildQueryMap(new HashMap<String, Object>(), request));
        condition.setOrderBy(field, dir);
        return ORMService.getInstance().queryPackage(ENTITY_NAME, start, limit, condition.toEntity());
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> findByPk(String thirdpartKey) {
        if (StringUtils.isBlank(thirdpartKey)) {
            return null;
        }
        return (Map<String, Object>) ORMService.getInstance().findByPk(ENTITY_NAME, thirdpartKey);
    }

    protected CmPri readPri(HttpServletRequest request) {
        return RequestUtils.getValue(request, "pri", CmPri.class);
    }

    protected void includePage(HttpServletRequest request, HttpServletResponse response, String page) {
        Actions.includePage(request, response, Actions.Util.getPagePath(request, page));
    }

    protected void redirectInfoPage(HttpServletRequest request, HttpServletResponse response, String message) {
        Actions.redirectInfoPage(request, response, message);
    }

    private Map<String, Object> readInput(HttpServletRequest request) {
        Map<String, Object> input = new HashMap<String, Object>();
        copy(request, input, "thirdpartKey");
        copy(request, input, "thirdpartName");
        copy(request, input, "clientId");
        copy(request, input, "redirectUris");
        copy(request, input, "homeUrl");
        copy(request, input, "activeFlag");
        copy(request, input, "description");
        String clientSecret = RequestUtils.getStringValue(request, "clientSecret");
        if (StringUtils.isNotBlank(clientSecret)) {
            input.put("clientSecret", clientSecret);
        }
        return input;
    }

    private void copy(HttpServletRequest request, Map<String, Object> input, String name) {
        input.put(name, RequestUtils.getStringValue(request, name));
    }

    private int currentActiveFlag(Map<String, Object> entity) {
        Object activeFlag = entity.get("activeFlag");
        if (activeFlag instanceof Number) {
            return ((Number) activeFlag).intValue();
        }
        return "1".equals(String.valueOf(activeFlag)) ? 1 : 0;
    }
}
