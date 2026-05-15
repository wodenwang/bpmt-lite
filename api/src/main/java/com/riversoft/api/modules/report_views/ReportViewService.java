package com.riversoft.api.modules.report_views;

import com.riversoft.api.http.ApiException;
import com.riversoft.api.http.ApiJson;
import com.riversoft.platform.po.VwUrl;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReportViewService {
    private static final String REPORT_VIEW_CLASS = "rep_list";
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final ReportViewRepository repository;
    private final ReportViewMapper mapper = new ReportViewMapper();
    private final ReportViewDefaults defaults = new ReportViewDefaults();
    private final ReportViewValidator validator = new ReportViewValidator();
    private final ReportViewScriptRiskScanner riskScanner = new ReportViewScriptRiskScanner();
    private final ReportViewPermissionService permissionService = new ReportViewPermissionService();

    public ReportViewService() {
        this.repository = null;
    }

    ReportViewService(ReportViewRepository repository) {
        this.repository = repository;
    }

    public Map<String, Object> list(String start, String limit) {
        ReportViewRepository repo = repository();
        int parsedStart = parseStart(start);
        int parsedLimit = parseLimit(limit);
        List<VwUrl> urls = repo.listReportUrls(parsedStart, parsedLimit);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("start", Integer.valueOf(parsedStart));
        result.put("limit", Integer.valueOf(parsedLimit));
        result.put("totalRecord", Integer.valueOf(repo.countReportUrls()));
        result.put("items", listItems(urls));
        return result;
    }

    public Map<String, Object> export(String viewKey) {
        ReportViewRepository repo = repository();
        VwUrl url = requireReportUrl(repo, viewKey);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("snapshot", mapper.toSnapshot(url, repo.findReport(viewKey)));
        return result;
    }

    public Map<String, Object> validate(ReportViewSnapshot snapshot) {
        ReportViewSnapshot normalized = defaults.normalize(snapshot);
        permissionService.apply(normalized.getViewKey(), null, normalized);
        ReportViewValidationResult validation = validateNormalized(normalized);
        return ReportViewResponse.validation(validation.isValid(), validation.getWarnings(), validation.getErrors(),
                validation.getNormalizedSnapshot());
    }

    public Map<String, Object> create(ReportViewSnapshot snapshot, boolean dryRun) {
        ReportViewRepository repo = repository();
        ReportViewSnapshot normalized = defaults.normalizeForCreate(snapshot);
        if (repo.findUrl(normalized.getViewKey()) != null) {
            throw ReportViewErrors.alreadyExists(normalized.getViewKey());
        }
        ReportViewResponse.WritePlan plan = permissionService.apply(normalized.getViewKey(), null, normalized);
        plan.setDryRun(dryRun);
        plan.getCreates().add("VW_URL");
        plan.getCreates().add("VW_REPORT");
        ReportViewValidationResult validation = validateNormalized(normalized);
        if (!validation.isValid()) {
            throw invalidSnapshot(validation);
        }
        if (!dryRun) {
            repo.createViewConfig(toUrl(normalized, null), mapper.toReportMap(normalized), plan);
            repo.flushAndClearViewCache(normalized.getViewKey());
        }
        return ReportViewResponse.write(normalized, validation.getWarnings(), plan);
    }

    public Map<String, Object> replace(String viewKey, ReportViewSnapshot snapshot, boolean dryRun) {
        ReportViewRepository repo = repository();
        VwUrl existingUrl = requireReportUrl(repo, viewKey);
        ReportViewSnapshot oldSnapshot = mapper.toSnapshot(existingUrl, repo.findReport(viewKey));
        ReportViewSnapshot normalized = defaults.normalize(snapshot);
        normalized.setViewKey(viewKey);
        ReportViewResponse.WritePlan plan = permissionService.apply(viewKey, oldSnapshot, normalized);
        plan.setDryRun(dryRun);
        plan.getUpdates().add("VW_URL");
        plan.getUpdates().add("VW_REPORT");
        plan.getDeletes().add("VW_REPORT_CHILDREN");
        ReportViewValidationResult validation = validateNormalized(normalized);
        if (!validation.isValid()) {
            throw invalidSnapshot(validation);
        }
        if (!dryRun) {
            repo.replaceViewConfig(toUrl(normalized, existingUrl), mapper.toReportMap(normalized), plan);
            repo.flushAndClearViewCache(viewKey);
        }
        return ReportViewResponse.write(normalized, validation.getWarnings(), plan);
    }

    public Map<String, Object> patch(String viewKey, ReportViewSection section, Object body, boolean dryRun) {
        ReportViewRepository repo = repository();
        VwUrl existingUrl = requireReportUrl(repo, viewKey);
        ReportViewSnapshot oldSnapshot = mapper.toSnapshot(existingUrl, repo.findReport(viewKey));
        ReportViewSnapshot current = mapper.toSnapshot(existingUrl, repo.findReport(viewKey));
        applySection(current, section, body);
        ReportViewSnapshot normalized = defaults.normalize(current);
        normalized.setViewKey(viewKey);
        ReportViewResponse.WritePlan plan = permissionService.apply(viewKey, oldSnapshot, normalized);
        plan.setDryRun(dryRun);
        plan.getUpdates().add("VW_REPORT_" + section.name());
        plan.getUpdatedSections().add(section.value());
        ReportViewValidationResult validation = validateNormalized(normalized);
        if (!validation.isValid()) {
            throw invalidSnapshot(validation);
        }
        if (!dryRun) {
            repo.patchViewConfig(toUrl(normalized, existingUrl), section, mapper.toReportMap(normalized), plan);
            repo.flushAndClearViewCache(viewKey);
        }
        return ReportViewResponse.write(normalized, validation.getWarnings(), plan);
    }

    public Map<String, Object> delete(String viewKey, String confirmViewKey) {
        ReportViewRepository repo = repository();
        if (!StringUtils.equals(viewKey, confirmViewKey)) {
            throw ReportViewErrors.confirmRequired();
        }
        VwUrl existingUrl = requireReportUrl(repo, viewKey);
        ReportViewSnapshot oldSnapshot = mapper.toSnapshot(existingUrl, repo.findReport(viewKey));
        ReportViewResponse.WritePlan plan = permissionService.apply(viewKey, oldSnapshot, null);
        plan.getDeletes().add("VW_URL");
        plan.getDeletes().add("VW_REPORT");
        plan.getDeletes().add("VW_REPORT_CHILDREN");
        repo.removeViewConfig(viewKey, plan);
        repo.flushAndClearViewCache(viewKey);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("viewKey", viewKey);
        result.put("deleted", Boolean.TRUE);
        result.put("businessDataDeleted", Boolean.FALSE);
        result.put("menuDeleted", Boolean.FALSE);
        result.put("externalEntriesDeleted", Boolean.FALSE);
        return result;
    }

    private ReportViewRepository repository() {
        if (repository == null) {
            throw new ApiException(501, "REPORT_VIEW_NOT_IMPLEMENTED", "报表视图 API 尚未实现。");
        }
        return repository;
    }

    private VwUrl requireReportUrl(ReportViewRepository repo, String viewKey) {
        VwUrl url = repo.findUrl(viewKey);
        if (url == null) {
            throw ReportViewErrors.notFound(viewKey);
        }
        if (!REPORT_VIEW_CLASS.equals(url.getViewClass())) {
            throw ReportViewErrors.notRepList(viewKey);
        }
        return url;
    }

    private ReportViewValidationResult validateNormalized(ReportViewSnapshot normalized) {
        ReportViewValidationResult validation = validator.validate(normalized);
        validation.setWarnings(mergeWarnings(validation.getWarnings(), riskScanner.scan(normalized)));
        return validation;
    }

    private List<ReportViewResponse.Warning> mergeWarnings(List<ReportViewResponse.Warning> first,
                                                           List<ReportViewResponse.Warning> second) {
        List<ReportViewResponse.Warning> result = new ArrayList<ReportViewResponse.Warning>();
        Set<String> keys = new LinkedHashSet<String>();
        addWarnings(result, keys, first);
        addWarnings(result, keys, second);
        return result;
    }

    private void addWarnings(List<ReportViewResponse.Warning> result,
                             Set<String> keys,
                             List<ReportViewResponse.Warning> warnings) {
        if (warnings == null) {
            return;
        }
        for (ReportViewResponse.Warning warning : warnings) {
            if (warning == null) {
                continue;
            }
            String key = warning.getPath() + "|" + warning.getCode();
            if (keys.add(key)) {
                result.add(warning);
            }
        }
    }

    private ApiException invalidSnapshot(ReportViewValidationResult validation) {
        String message = "报表视图快照校验失败。";
        if (validation != null && validation.getErrors() != null && !validation.getErrors().isEmpty()) {
            message = validation.getErrors().get(0).getMessage();
        }
        return ReportViewErrors.invalidSnapshot(message);
    }

    private List<Map<String, Object>> listItems(List<VwUrl> urls) {
        if (urls == null || urls.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        for (VwUrl url : urls) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("viewKey", url.getViewKey());
            item.put("description", url.getDescription());
            item.put("url", url.getUrl());
            item.put("loginRequired", Boolean.valueOf(url.getLoginType() == null || url.getLoginType().intValue() == 1));
            items.add(item);
        }
        return items;
    }

    private VwUrl toUrl(ReportViewSnapshot snapshot, VwUrl existing) {
        Date now = new Date();
        VwUrl url = existing == null ? new VwUrl() : existing;
        if (url.getCreateDate() == null) {
            url.setCreateDate(now);
        }
        url.setUpdateDate(now);
        url.setViewKey(snapshot.getViewKey());
        url.setViewClass(REPORT_VIEW_CLASS);
        url.setDescription(snapshot.getDescription());
        url.setLoginType(snapshot.isLoginRequired() ? Integer.valueOf(1) : Integer.valueOf(0));
        url.setLockFlag(Integer.valueOf(0));
        if (StringUtils.isBlank(url.getCreateUid())) {
            url.setCreateUid("admin");
        }
        return url;
    }

    private void applySection(ReportViewSnapshot snapshot, ReportViewSection section, Object body) {
        switch (section) {
            case BASE:
                snapshot.setBase(convert(body, ReportViewSnapshot.Base.class));
                return;
            case COLUMNS:
                snapshot.setColumns(convert(body, ReportViewSnapshot.Columns.class));
                return;
            case QUERIES:
                snapshot.setQueries(Arrays.asList(convert(body, ReportViewSnapshot.Query[].class)));
                return;
            case LIMITS:
                snapshot.setLimits(Arrays.asList(convert(body, ReportViewSnapshot.Limit[].class)));
                return;
            case VARIABLES:
                snapshot.setVariables(convert(body, ReportViewSnapshot.Variables.class));
                return;
            case SUBVIEWS:
                snapshot.setSubviews(convert(body, ReportViewSnapshot.Subviews.class));
                return;
            case BUTTONS:
                snapshot.setButtons(convert(body, ReportViewSnapshot.Buttons.class));
                return;
            case WEIXIN:
                snapshot.setWeixin(convert(body, ReportViewSnapshot.Weixin.class));
                return;
            case SCRIPTS:
                snapshot.setScripts(convert(body, ReportViewSnapshot.Scripts.class));
                return;
            default:
                throw ReportViewErrors.unsupportedSection(section == null ? null : section.value());
        }
    }

    private <T> T convert(Object value, Class<T> type) {
        try {
            return ApiJson.fromJson(new ByteArrayInputStream(ApiJson.toJson(value).getBytes("UTF-8")), type);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    private int parseStart(String value) {
        if (StringUtils.isBlank(value)) {
            return 0;
        }
        int start = parseInt(value);
        if (start < 0) {
            throw new ApiException(400, "API_INVALID_PARAMETER", "分页参数 start 不能小于 0。");
        }
        return start;
    }

    private int parseLimit(String value) {
        if (StringUtils.isBlank(value)) {
            return DEFAULT_LIMIT;
        }
        int limit = parseInt(value);
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new ApiException(400, "API_INVALID_PARAMETER", "分页参数 limit 范围是 1 到 100。");
        }
        return limit;
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(StringUtils.trim(value));
        } catch (NumberFormatException e) {
            throw new ApiException(400, "API_INVALID_PARAMETER", "分页参数无效。");
        }
    }
}

class ReportViewPermissionService {
    ReportViewResponse.WritePlan apply(String viewKey,
                                       ReportViewSnapshot oldSnapshot,
                                       ReportViewSnapshot target) {
        ReportViewResponse.WritePlan plan = new ReportViewResponse.WritePlan();
        if (target == null) {
            collectDeletes(oldSnapshot, plan);
            return plan;
        }
        applyPermissions(viewKey, target);
        collectRemovedPermissions(oldSnapshot, target, plan);
        return plan;
    }

    private void applyPermissions(String viewKey, ReportViewSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        ReportViewSnapshot normalized = new ReportViewDefaults().normalize(snapshot);
        for (ReportViewSnapshot.ShowColumn column : normalized.getColumns().getShow()) {
            if (column.getPermissions() == null) {
                column.setPermissions(new ReportViewSnapshot.PermissionSet());
            }
            ensurePermission(column.getPermissions(), "report." + viewKey + ".column." + column.getStableKey() + ".view");
        }
        for (ReportViewSnapshot.LineColumn line : normalized.getColumns().getLines()) {
            if (line.getPermissions() == null) {
                line.setPermissions(new ReportViewSnapshot.PermissionSet());
            }
            ensurePermission(line.getPermissions(), "report." + viewKey + ".line." + line.getStableKey() + ".view");
        }
        for (ReportViewSnapshot.Limit limit : normalized.getLimits()) {
            if (limit.getPermissions() == null) {
                limit.setPermissions(new ReportViewSnapshot.PermissionSet());
            }
            ensurePermission(limit.getPermissions(), "report." + viewKey + ".limit." + limit.getStableKey() + ".view");
        }
        for (ReportViewSnapshot.ViewTab tab : normalized.getSubviews().getViewTabs()) {
            if (tab.getPermissions() == null) {
                tab.setPermissions(new ReportViewSnapshot.PermissionSet());
            }
            ensurePermission(tab.getPermissions(), "report." + viewKey + ".subview." + tab.getStableKey() + ".view");
        }
        for (ReportViewSnapshot.SystemButton button : normalized.getButtons().getSystem()) {
            if (button.getPermissions() == null) {
                button.setPermissions(new ReportViewSnapshot.PermissionSet());
            }
            ensurePermission(button.getPermissions(), "report." + viewKey + ".button.system." + button.getName() + ".view");
        }
        for (ReportViewSnapshot.CustomButton button : normalized.getButtons().getItem()) {
            if (button.getPermissions() == null) {
                button.setPermissions(new ReportViewSnapshot.PermissionSet());
            }
            ensurePermission(button.getPermissions(), "report." + viewKey + ".button.item." + button.getStableKey() + ".view");
        }
        for (ReportViewSnapshot.CustomButton button : normalized.getButtons().getSummary()) {
            if (button.getPermissions() == null) {
                button.setPermissions(new ReportViewSnapshot.PermissionSet());
            }
            ensurePermission(button.getPermissions(), "report." + viewKey + ".button.summary." + button.getStableKey() + ".view");
        }
        if (normalized.getWeixin() != null) {
            if (normalized.getWeixin().getPermissions() == null) {
                normalized.getWeixin().setPermissions(new ReportViewSnapshot.PermissionSet());
            }
            ensurePermission(normalized.getWeixin().getPermissions(), "report." + viewKey + ".weixin.view");
        }
    }

    private void collectRemovedPermissions(ReportViewSnapshot oldSnapshot,
                                           ReportViewSnapshot target,
                                           ReportViewResponse.WritePlan plan) {
        Set<String> oldKeys = collectKeys(oldSnapshot);
        Set<String> targetKeys = collectKeys(target);
        for (String key : oldKeys) {
            if (!targetKeys.contains(key)) {
                plan.getPermissionDeletes().add(key);
            }
        }
    }

    private void collectDeletes(ReportViewSnapshot oldSnapshot, ReportViewResponse.WritePlan plan) {
        plan.getPermissionDeletes().addAll(collectKeys(oldSnapshot));
    }

    private Set<String> collectKeys(ReportViewSnapshot snapshot) {
        Set<String> keys = new LinkedHashSet<String>();
        if (snapshot == null) {
            return keys;
        }
        ReportViewSnapshot normalized = new ReportViewDefaults().normalize(snapshot);
        for (ReportViewSnapshot.ShowColumn column : normalized.getColumns().getShow()) {
            collectPermission(keys, column.getPermissions());
        }
        for (ReportViewSnapshot.LineColumn line : normalized.getColumns().getLines()) {
            collectPermission(keys, line.getPermissions());
        }
        for (ReportViewSnapshot.Limit limit : normalized.getLimits()) {
            collectPermission(keys, limit.getPermissions());
        }
        for (ReportViewSnapshot.ViewTab tab : normalized.getSubviews().getViewTabs()) {
            collectPermission(keys, tab.getPermissions());
        }
        for (ReportViewSnapshot.SystemButton button : normalized.getButtons().getSystem()) {
            collectPermission(keys, button.getPermissions());
        }
        for (ReportViewSnapshot.CustomButton button : normalized.getButtons().getItem()) {
            collectPermission(keys, button.getPermissions());
        }
        for (ReportViewSnapshot.CustomButton button : normalized.getButtons().getSummary()) {
            collectPermission(keys, button.getPermissions());
        }
        if (normalized.getWeixin() != null) {
            collectPermission(keys, normalized.getWeixin().getPermissions());
        }
        return keys;
    }

    private void ensurePermission(ReportViewSnapshot.PermissionSet permissions, String generatedKey) {
        if (permissions == null) {
            return;
        }
        if (permissions.getView() == null) {
            permissions.setView(new ArrayList<String>());
        }
        for (String value : permissions.getView()) {
            if (StringUtils.isNotBlank(value)) {
                return;
            }
        }
        permissions.getView().clear();
        permissions.getView().add(generatedKey);
    }

    private void collectPermission(Set<String> keys, ReportViewSnapshot.PermissionSet permissions) {
        if (permissions == null || permissions.getView() == null) {
            return;
        }
        for (String key : permissions.getView()) {
            if (StringUtils.isNotBlank(key)) {
                keys.add(key);
            }
        }
    }
}
