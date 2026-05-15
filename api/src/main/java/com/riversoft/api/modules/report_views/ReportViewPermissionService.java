package com.riversoft.api.modules.report_views;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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
        applyPermissions(viewKey, oldSnapshot, target);
        collectRemovedPermissions(oldSnapshot, target, plan);
        return plan;
    }

    private void applyPermissions(String viewKey, ReportViewSnapshot oldSnapshot, ReportViewSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        List<Boolean> showColumnKeyBlanks = showColumnKeyBlanks(snapshot);
        List<Boolean> lineColumnKeyBlanks = lineColumnKeyBlanks(snapshot);
        List<Boolean> limitKeyBlanks = limitKeyBlanks(snapshot);
        List<Boolean> viewTabKeyBlanks = viewTabKeyBlanks(snapshot);
        List<Boolean> systemButtonNameBlanks = systemButtonNameBlanks(snapshot);
        List<Boolean> itemButtonKeyBlanks = itemButtonKeyBlanks(snapshot, false);
        List<Boolean> summaryButtonKeyBlanks = itemButtonKeyBlanks(snapshot, true);
        ReportViewSnapshot normalized = new ReportViewDefaults().normalize(snapshot);
        List<ReportViewSnapshot.ShowColumn> oldShowColumns = oldShowColumns(oldSnapshot);
        for (int i = 0; i < normalized.getColumns().getShow().size(); i++) {
            ReportViewSnapshot.ShowColumn column = normalized.getColumns().getShow().get(i);
            ensurePermissionSet(column);
            ensurePermission(column.getPermissions(),
                    oldPermission(matchShowColumn(oldShowColumns, column, i, booleanAt(showColumnKeyBlanks, i))),
                    "report." + viewKey + ".column." + column.getStableKey() + ".view");
        }
        List<ReportViewSnapshot.LineColumn> oldLineColumns = oldLineColumns(oldSnapshot);
        for (int i = 0; i < normalized.getColumns().getLines().size(); i++) {
            ReportViewSnapshot.LineColumn line = normalized.getColumns().getLines().get(i);
            ensurePermissionSet(line);
            ensurePermission(line.getPermissions(),
                    oldPermission(matchLineColumn(oldLineColumns, line, i, booleanAt(lineColumnKeyBlanks, i))),
                    "report." + viewKey + ".line." + line.getStableKey() + ".view");
        }
        List<ReportViewSnapshot.Limit> oldLimits = oldLimits(oldSnapshot);
        for (int i = 0; i < normalized.getLimits().size(); i++) {
            ReportViewSnapshot.Limit limit = normalized.getLimits().get(i);
            ensurePermissionSet(limit);
            ensurePermission(limit.getPermissions(),
                    oldPermission(matchLimit(oldLimits, limit, i, booleanAt(limitKeyBlanks, i))),
                    "report." + viewKey + ".limit." + limit.getStableKey() + ".view");
        }
        List<ReportViewSnapshot.ViewTab> oldTabs = oldViewTabs(oldSnapshot);
        for (int i = 0; i < normalized.getSubviews().getViewTabs().size(); i++) {
            ReportViewSnapshot.ViewTab tab = normalized.getSubviews().getViewTabs().get(i);
            ensurePermissionSet(tab);
            ensurePermission(tab.getPermissions(),
                    oldPermission(matchViewTab(oldTabs, tab, i, booleanAt(viewTabKeyBlanks, i))),
                    "report." + viewKey + ".subview." + tab.getStableKey() + ".view");
        }
        List<ReportViewSnapshot.SystemButton> oldSystemButtons = oldSystemButtons(oldSnapshot);
        for (int i = 0; i < normalized.getButtons().getSystem().size(); i++) {
            ReportViewSnapshot.SystemButton button = normalized.getButtons().getSystem().get(i);
            ensurePermissionSet(button);
            ensurePermission(button.getPermissions(),
                    oldPermission(matchSystemButton(oldSystemButtons, button, i, booleanAt(systemButtonNameBlanks, i))),
                    "report." + viewKey + ".button.system." + button.getName() + ".view");
        }
        List<ReportViewSnapshot.CustomButton> oldItemButtons = oldItemButtons(oldSnapshot);
        for (int i = 0; i < normalized.getButtons().getItem().size(); i++) {
            ReportViewSnapshot.CustomButton button = normalized.getButtons().getItem().get(i);
            ensurePermissionSet(button);
            ensurePermission(button.getPermissions(),
                    oldPermission(matchCustomButton(oldItemButtons, button, i, booleanAt(itemButtonKeyBlanks, i))),
                    "report." + viewKey + ".button.item." + button.getStableKey() + ".view");
        }
        List<ReportViewSnapshot.CustomButton> oldSummaryButtons = oldSummaryButtons(oldSnapshot);
        for (int i = 0; i < normalized.getButtons().getSummary().size(); i++) {
            ReportViewSnapshot.CustomButton button = normalized.getButtons().getSummary().get(i);
            ensurePermissionSet(button);
            ensurePermission(button.getPermissions(),
                    oldPermission(matchCustomButton(oldSummaryButtons, button, i, booleanAt(summaryButtonKeyBlanks, i))),
                    "report." + viewKey + ".button.summary." + button.getStableKey() + ".view");
        }
        if (normalized.getWeixin() != null) {
            ensurePermissionSet(normalized.getWeixin());
            ensurePermission(normalized.getWeixin().getPermissions(), oldWeixinPermission(oldSnapshot),
                    "report." + viewKey + ".weixin.view");
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

    private String oldPermission(ReportViewSnapshot.ShowColumn column) {
        return column == null ? null : firstPermission(column.getPermissions());
    }

    private String oldPermission(ReportViewSnapshot.LineColumn line) {
        return line == null ? null : firstPermission(line.getPermissions());
    }

    private String oldPermission(ReportViewSnapshot.Limit limit) {
        return limit == null ? null : firstPermission(limit.getPermissions());
    }

    private String oldPermission(ReportViewSnapshot.ViewTab tab) {
        return tab == null ? null : firstPermission(tab.getPermissions());
    }

    private String oldPermission(ReportViewSnapshot.SystemButton button) {
        return button == null ? null : firstPermission(button.getPermissions());
    }

    private String oldPermission(ReportViewSnapshot.CustomButton button) {
        return button == null ? null : firstPermission(button.getPermissions());
    }

    private ReportViewSnapshot.ShowColumn matchShowColumn(List<ReportViewSnapshot.ShowColumn> oldColumns,
                                                          ReportViewSnapshot.ShowColumn target,
                                                          int index,
                                                          boolean targetKeyBlank) {
        if (oldColumns == null || target == null) {
            return null;
        }
        if (targetKeyBlank) {
            return itemAt(oldColumns, index);
        }
        for (ReportViewSnapshot.ShowColumn old : oldColumns) {
            if (old != null && StringUtils.isNotBlank(target.getStableKey())
                    && StringUtils.equals(target.getStableKey(), old.getStableKey())) {
                return old;
            }
        }
        return itemAt(oldColumns, index);
    }

    private ReportViewSnapshot.LineColumn matchLineColumn(List<ReportViewSnapshot.LineColumn> oldLines,
                                                          ReportViewSnapshot.LineColumn target,
                                                          int index,
                                                          boolean targetKeyBlank) {
        if (oldLines == null || target == null) {
            return null;
        }
        if (targetKeyBlank) {
            return itemAt(oldLines, index);
        }
        for (ReportViewSnapshot.LineColumn old : oldLines) {
            if (old != null && StringUtils.isNotBlank(target.getStableKey())
                    && StringUtils.equals(target.getStableKey(), old.getStableKey())) {
                return old;
            }
        }
        return itemAt(oldLines, index);
    }

    private ReportViewSnapshot.Limit matchLimit(List<ReportViewSnapshot.Limit> oldLimits,
                                                ReportViewSnapshot.Limit target,
                                                int index,
                                                boolean targetKeyBlank) {
        if (oldLimits == null || target == null) {
            return null;
        }
        if (targetKeyBlank) {
            return itemAt(oldLimits, index);
        }
        for (ReportViewSnapshot.Limit old : oldLimits) {
            if (old != null && StringUtils.isNotBlank(target.getStableKey())
                    && StringUtils.equals(target.getStableKey(), old.getStableKey())) {
                return old;
            }
        }
        return itemAt(oldLimits, index);
    }

    private ReportViewSnapshot.ViewTab matchViewTab(List<ReportViewSnapshot.ViewTab> oldTabs,
                                                    ReportViewSnapshot.ViewTab target,
                                                    int index,
                                                    boolean targetKeyBlank) {
        if (oldTabs == null || target == null) {
            return null;
        }
        if (targetKeyBlank) {
            return itemAt(oldTabs, index);
        }
        for (ReportViewSnapshot.ViewTab old : oldTabs) {
            if (old != null && StringUtils.isNotBlank(target.getStableKey())
                    && StringUtils.equals(target.getStableKey(), old.getStableKey())) {
                return old;
            }
        }
        return itemAt(oldTabs, index);
    }

    private ReportViewSnapshot.SystemButton matchSystemButton(List<ReportViewSnapshot.SystemButton> oldButtons,
                                                              ReportViewSnapshot.SystemButton target,
                                                              int index,
                                                              boolean targetNameBlank) {
        if (oldButtons == null || target == null) {
            return null;
        }
        if (targetNameBlank) {
            return itemAt(oldButtons, index);
        }
        for (ReportViewSnapshot.SystemButton old : oldButtons) {
            if (old != null && StringUtils.isNotBlank(target.getName())
                    && StringUtils.equals(target.getName(), old.getName())) {
                return old;
            }
        }
        return itemAt(oldButtons, index);
    }

    private ReportViewSnapshot.CustomButton matchCustomButton(List<ReportViewSnapshot.CustomButton> oldButtons,
                                                              ReportViewSnapshot.CustomButton target,
                                                              int index,
                                                              boolean targetKeyBlank) {
        if (oldButtons == null || target == null) {
            return null;
        }
        if (targetKeyBlank) {
            return itemAt(oldButtons, index);
        }
        for (ReportViewSnapshot.CustomButton old : oldButtons) {
            if (old != null && StringUtils.isNotBlank(target.getStableKey())
                    && StringUtils.equals(target.getStableKey(), old.getStableKey())) {
                return old;
            }
        }
        return itemAt(oldButtons, index);
    }

    private <T> T itemAt(List<T> items, int index) {
        if (items == null || index < 0 || index >= items.size()) {
            return null;
        }
        return items.get(index);
    }

    private boolean booleanAt(List<Boolean> values, int index) {
        Boolean value = itemAt(values, index);
        return Boolean.TRUE.equals(value);
    }

    private List<Boolean> showColumnKeyBlanks(ReportViewSnapshot snapshot) {
        List<Boolean> values = new ArrayList<Boolean>();
        List<ReportViewSnapshot.ShowColumn> columns = snapshot == null || snapshot.getColumns() == null
                ? null : snapshot.getColumns().getShow();
        if (columns != null) {
            for (ReportViewSnapshot.ShowColumn column : columns) {
                values.add(Boolean.valueOf(column == null || StringUtils.isBlank(column.getStableKey())));
            }
        }
        return values;
    }

    private List<Boolean> lineColumnKeyBlanks(ReportViewSnapshot snapshot) {
        List<Boolean> values = new ArrayList<Boolean>();
        List<ReportViewSnapshot.LineColumn> lines = snapshot == null || snapshot.getColumns() == null
                ? null : snapshot.getColumns().getLines();
        if (lines != null) {
            for (ReportViewSnapshot.LineColumn line : lines) {
                values.add(Boolean.valueOf(line == null || StringUtils.isBlank(line.getStableKey())));
            }
        }
        return values;
    }

    private List<Boolean> limitKeyBlanks(ReportViewSnapshot snapshot) {
        List<Boolean> values = new ArrayList<Boolean>();
        if (snapshot != null && snapshot.getLimits() != null) {
            for (ReportViewSnapshot.Limit limit : snapshot.getLimits()) {
                values.add(Boolean.valueOf(limit == null || StringUtils.isBlank(limit.getStableKey())));
            }
        }
        return values;
    }

    private List<Boolean> viewTabKeyBlanks(ReportViewSnapshot snapshot) {
        List<Boolean> values = new ArrayList<Boolean>();
        List<ReportViewSnapshot.ViewTab> tabs = snapshot == null || snapshot.getSubviews() == null
                ? null : snapshot.getSubviews().getViewTabs();
        if (tabs != null) {
            for (ReportViewSnapshot.ViewTab tab : tabs) {
                values.add(Boolean.valueOf(tab == null || StringUtils.isBlank(tab.getStableKey())));
            }
        }
        return values;
    }

    private List<Boolean> systemButtonNameBlanks(ReportViewSnapshot snapshot) {
        List<Boolean> values = new ArrayList<Boolean>();
        List<ReportViewSnapshot.SystemButton> buttons = snapshot == null || snapshot.getButtons() == null
                ? null : snapshot.getButtons().getSystem();
        if (buttons != null) {
            for (ReportViewSnapshot.SystemButton button : buttons) {
                values.add(Boolean.valueOf(button == null || StringUtils.isBlank(button.getName())));
            }
        }
        return values;
    }

    private List<Boolean> itemButtonKeyBlanks(ReportViewSnapshot snapshot, boolean summary) {
        List<Boolean> values = new ArrayList<Boolean>();
        List<ReportViewSnapshot.CustomButton> buttons = null;
        if (snapshot != null && snapshot.getButtons() != null) {
            buttons = summary ? snapshot.getButtons().getSummary() : snapshot.getButtons().getItem();
        }
        if (buttons != null) {
            for (ReportViewSnapshot.CustomButton button : buttons) {
                values.add(Boolean.valueOf(button == null || StringUtils.isBlank(button.getStableKey())));
            }
        }
        return values;
    }

    private List<ReportViewSnapshot.ShowColumn> oldShowColumns(ReportViewSnapshot snapshot) {
        return snapshot == null || snapshot.getColumns() == null ? null : snapshot.getColumns().getShow();
    }

    private List<ReportViewSnapshot.LineColumn> oldLineColumns(ReportViewSnapshot snapshot) {
        return snapshot == null || snapshot.getColumns() == null ? null : snapshot.getColumns().getLines();
    }

    private List<ReportViewSnapshot.Limit> oldLimits(ReportViewSnapshot snapshot) {
        return snapshot == null ? null : snapshot.getLimits();
    }

    private List<ReportViewSnapshot.ViewTab> oldViewTabs(ReportViewSnapshot snapshot) {
        return snapshot == null || snapshot.getSubviews() == null ? null : snapshot.getSubviews().getViewTabs();
    }

    private List<ReportViewSnapshot.SystemButton> oldSystemButtons(ReportViewSnapshot snapshot) {
        return snapshot == null || snapshot.getButtons() == null ? null : snapshot.getButtons().getSystem();
    }

    private List<ReportViewSnapshot.CustomButton> oldItemButtons(ReportViewSnapshot snapshot) {
        return snapshot == null || snapshot.getButtons() == null ? null : snapshot.getButtons().getItem();
    }

    private List<ReportViewSnapshot.CustomButton> oldSummaryButtons(ReportViewSnapshot snapshot) {
        return snapshot == null || snapshot.getButtons() == null ? null : snapshot.getButtons().getSummary();
    }

    private String oldWeixinPermission(ReportViewSnapshot snapshot) {
        return snapshot == null || snapshot.getWeixin() == null
                ? null : firstPermission(snapshot.getWeixin().getPermissions());
    }

    private void ensurePermission(ReportViewSnapshot.PermissionSet permissions, String oldPermission, String generatedKey) {
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
        permissions.getView().add(StringUtils.isNotBlank(oldPermission) ? oldPermission : generatedKey);
    }

    private String firstPermission(ReportViewSnapshot.PermissionSet permissions) {
        if (permissions == null || permissions.getView() == null) {
            return null;
        }
        for (String key : permissions.getView()) {
            if (StringUtils.isNotBlank(key)) {
                return key;
            }
        }
        return null;
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
