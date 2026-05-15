package com.riversoft.api.modules.report_views;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

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
            ensurePermissionSet(column);
            ensurePermission(column.getPermissions(), "report." + viewKey + ".column." + column.getStableKey() + ".view");
        }
        for (ReportViewSnapshot.LineColumn line : normalized.getColumns().getLines()) {
            ensurePermissionSet(line);
            ensurePermission(line.getPermissions(), "report." + viewKey + ".line." + line.getStableKey() + ".view");
        }
        for (ReportViewSnapshot.Limit limit : normalized.getLimits()) {
            ensurePermissionSet(limit);
            ensurePermission(limit.getPermissions(), "report." + viewKey + ".limit." + limit.getStableKey() + ".view");
        }
        for (ReportViewSnapshot.ViewTab tab : normalized.getSubviews().getViewTabs()) {
            ensurePermissionSet(tab);
            ensurePermission(tab.getPermissions(), "report." + viewKey + ".subview." + tab.getStableKey() + ".view");
        }
        for (ReportViewSnapshot.SystemButton button : normalized.getButtons().getSystem()) {
            ensurePermissionSet(button);
            ensurePermission(button.getPermissions(), "report." + viewKey + ".button.system." + button.getName() + ".view");
        }
        for (ReportViewSnapshot.CustomButton button : normalized.getButtons().getItem()) {
            ensurePermissionSet(button);
            ensurePermission(button.getPermissions(), "report." + viewKey + ".button.item." + button.getStableKey() + ".view");
        }
        for (ReportViewSnapshot.CustomButton button : normalized.getButtons().getSummary()) {
            ensurePermissionSet(button);
            ensurePermission(button.getPermissions(), "report." + viewKey + ".button.summary." + button.getStableKey() + ".view");
        }
        if (normalized.getWeixin() != null) {
            ensurePermissionSet(normalized.getWeixin());
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

    private void ensurePermissionSet(ReportViewSnapshot.ShowColumn column) {
        if (column.getPermissions() == null) {
            column.setPermissions(new ReportViewSnapshot.PermissionSet());
        }
    }

    private void ensurePermissionSet(ReportViewSnapshot.LineColumn line) {
        if (line.getPermissions() == null) {
            line.setPermissions(new ReportViewSnapshot.PermissionSet());
        }
    }

    private void ensurePermissionSet(ReportViewSnapshot.Limit limit) {
        if (limit.getPermissions() == null) {
            limit.setPermissions(new ReportViewSnapshot.PermissionSet());
        }
    }

    private void ensurePermissionSet(ReportViewSnapshot.ViewTab tab) {
        if (tab.getPermissions() == null) {
            tab.setPermissions(new ReportViewSnapshot.PermissionSet());
        }
    }

    private void ensurePermissionSet(ReportViewSnapshot.SystemButton button) {
        if (button.getPermissions() == null) {
            button.setPermissions(new ReportViewSnapshot.PermissionSet());
        }
    }

    private void ensurePermissionSet(ReportViewSnapshot.CustomButton button) {
        if (button.getPermissions() == null) {
            button.setPermissions(new ReportViewSnapshot.PermissionSet());
        }
    }

    private void ensurePermissionSet(ReportViewSnapshot.Weixin weixin) {
        if (weixin.getPermissions() == null) {
            weixin.setPermissions(new ReportViewSnapshot.PermissionSet());
        }
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
