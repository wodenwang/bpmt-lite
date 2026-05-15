package com.riversoft.api.modules.report_views;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class ReportViewDefaults {
    public ReportViewSnapshot normalize(ReportViewSnapshot snapshot) {
        ReportViewSnapshot normalized = snapshot == null ? new ReportViewSnapshot() : snapshot;
        if (normalized.getBase() == null) {
            normalized.setBase(new ReportViewSnapshot.Base());
        }
        if (normalized.getColumns() == null) {
            normalized.setColumns(new ReportViewSnapshot.Columns());
        }
        if (normalized.getQueries() == null) {
            normalized.setQueries(new ArrayList<ReportViewSnapshot.Query>());
        }
        if (normalized.getLimits() == null) {
            normalized.setLimits(new ArrayList<ReportViewSnapshot.Limit>());
        }
        if (normalized.getVariables() == null) {
            normalized.setVariables(new ReportViewSnapshot.Variables());
        }
        if (normalized.getSubviews() == null) {
            normalized.setSubviews(new ReportViewSnapshot.Subviews());
        }
        if (normalized.getButtons() == null) {
            normalized.setButtons(new ReportViewSnapshot.Buttons());
        }
        if (normalized.getScripts() == null) {
            normalized.setScripts(new ReportViewSnapshot.Scripts());
        }
        normalizeBase(normalized.getBase());
        normalizeColumns(normalized.getColumns());
        normalizeQueries(normalized);
        normalizeLimits(normalized);
        normalizeVariables(normalized.getVariables());
        normalizeSubviews(normalized.getSubviews());
        normalizeButtons(normalized.getButtons());
        return normalized;
    }

    public ReportViewSnapshot normalizeForCreate(ReportViewSnapshot snapshot) {
        ReportViewSnapshot normalized = normalize(snapshot);
        if (StringUtils.isBlank(normalized.getViewKey())) {
            normalized.setViewKey("REPORT_" + UUID.randomUUID().toString().replace("-", "")
                    .substring(0, 12).toUpperCase(Locale.ENGLISH));
        }
        return normalized;
    }

    private void normalizeBase(ReportViewSnapshot.Base base) {
        if (base.getPagination() == null) {
            base.setPagination(new ReportViewSnapshot.Pagination());
        }
        if (base.getSummaryEnabled() == null) {
            base.setSummaryEnabled(Boolean.FALSE);
        }
    }

    private void normalizeColumns(ReportViewSnapshot.Columns columns) {
        if (columns.getShow() == null) {
            columns.setShow(new ArrayList<ReportViewSnapshot.ShowColumn>());
        }
        if (columns.getLines() == null) {
            columns.setLines(new ArrayList<ReportViewSnapshot.LineColumn>());
        }
        if (columns.getListOrder() == null) {
            columns.setListOrder(new ArrayList<String>());
        }
        for (int i = 0; i < columns.getShow().size(); i++) {
            ReportViewSnapshot.ShowColumn column = columns.getShow().get(i);
            if (column != null && StringUtils.isBlank(column.getStableKey())) {
                column.setStableKey("showColumn-" + (i + 1));
            }
            if (column != null && column.getWholeLine() == null) {
                column.setWholeLine(Boolean.FALSE);
            }
        }
        for (int i = 0; i < columns.getLines().size(); i++) {
            ReportViewSnapshot.LineColumn line = columns.getLines().get(i);
            if (line != null && StringUtils.isBlank(line.getStableKey())) {
                line.setStableKey("lineColumn-" + (i + 1));
            }
        }
        if (columns.getListOrder().isEmpty()) {
            for (ReportViewSnapshot.ShowColumn column : columns.getShow()) {
                if (column != null && StringUtils.isNotBlank(column.getStableKey())) {
                    columns.getListOrder().add(column.getStableKey());
                }
            }
        }
    }

    private void normalizeQueries(ReportViewSnapshot snapshot) {
        for (int i = 0; i < snapshot.getQueries().size(); i++) {
            ReportViewSnapshot.Query query = snapshot.getQueries().get(i);
            if (query != null && StringUtils.isBlank(query.getName())) {
                query.setName("query-" + (i + 1));
            }
        }
    }

    private void normalizeLimits(ReportViewSnapshot snapshot) {
        for (int i = 0; i < snapshot.getLimits().size(); i++) {
            ReportViewSnapshot.Limit limit = snapshot.getLimits().get(i);
            if (limit != null && StringUtils.isBlank(limit.getStableKey())) {
                limit.setStableKey("limit-" + (i + 1));
            }
        }
    }

    private void normalizeVariables(ReportViewSnapshot.Variables variables) {
        if (variables.getPrepared() == null) {
            variables.setPrepared(new ArrayList<ReportViewSnapshot.PreparedVariable>());
        }
        for (int i = 0; i < variables.getPrepared().size(); i++) {
            ReportViewSnapshot.PreparedVariable variable = variables.getPrepared().get(i);
            if (variable != null && StringUtils.isBlank(variable.getVar())) {
                variable.setVar("preparedVariable" + (i + 1));
            }
        }
    }

    private void normalizeSubviews(ReportViewSnapshot.Subviews subviews) {
        if (subviews.getViewTabs() == null) {
            subviews.setViewTabs(new ArrayList<ReportViewSnapshot.ViewTab>());
        }
        for (int i = 0; i < subviews.getViewTabs().size(); i++) {
            ReportViewSnapshot.ViewTab tab = subviews.getViewTabs().get(i);
            if (tab != null && StringUtils.isBlank(tab.getStableKey())) {
                tab.setStableKey("viewTab-" + (i + 1));
            }
        }
    }

    private void normalizeButtons(ReportViewSnapshot.Buttons buttons) {
        if (buttons.getSystem() == null) {
            buttons.setSystem(new ArrayList<ReportViewSnapshot.SystemButton>());
        }
        if (buttons.getItem() == null) {
            buttons.setItem(new ArrayList<ReportViewSnapshot.CustomButton>());
        }
        if (buttons.getSummary() == null) {
            buttons.setSummary(new ArrayList<ReportViewSnapshot.CustomButton>());
        }
        for (int i = 0; i < buttons.getItem().size(); i++) {
            ReportViewSnapshot.CustomButton button = buttons.getItem().get(i);
            if (button != null && StringUtils.isBlank(button.getStableKey())) {
                button.setStableKey("itemButton-" + (i + 1));
            }
        }
        for (int i = 0; i < buttons.getSummary().size(); i++) {
            ReportViewSnapshot.CustomButton button = buttons.getSummary().get(i);
            if (button != null && StringUtils.isBlank(button.getStableKey())) {
                button.setStableKey("summaryButton-" + (i + 1));
            }
        }
    }
}
