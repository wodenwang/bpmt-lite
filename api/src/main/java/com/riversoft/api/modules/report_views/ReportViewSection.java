package com.riversoft.api.modules.report_views;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

public enum ReportViewSection {
    BASE,
    COLUMNS,
    QUERIES,
    LIMITS,
    VARIABLES,
    SUBVIEWS,
    BUTTONS,
    WEIXIN,
    SCRIPTS;

    public String value() {
        return name().toLowerCase(Locale.ENGLISH);
    }

    public static ReportViewSection parse(String value) {
        String trimmed = StringUtils.trimToEmpty(value);
        for (ReportViewSection section : values()) {
            if (section.value().equals(trimmed)) {
                return section;
            }
        }
        throw ReportViewErrors.unsupportedSection(trimmed);
    }
}
