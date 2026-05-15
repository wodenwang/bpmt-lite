package com.riversoft.api.modules.report_views;

import java.util.ArrayList;
import java.util.List;

public class ReportViewValidationResult {
    private boolean valid = true;
    private List<ReportViewResponse.Warning> warnings = new ArrayList<ReportViewResponse.Warning>();
    private List<ReportViewResponse.ValidationItem> errors = new ArrayList<ReportViewResponse.ValidationItem>();
    private ReportViewSnapshot normalizedSnapshot;

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<ReportViewResponse.Warning> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<ReportViewResponse.Warning> warnings) {
        this.warnings = warnings;
    }

    public List<ReportViewResponse.ValidationItem> getErrors() {
        return errors;
    }

    public void setErrors(List<ReportViewResponse.ValidationItem> errors) {
        this.errors = errors;
    }

    public ReportViewSnapshot getNormalizedSnapshot() {
        return normalizedSnapshot;
    }

    public void setNormalizedSnapshot(ReportViewSnapshot normalizedSnapshot) {
        this.normalizedSnapshot = normalizedSnapshot;
    }

    public void addError(String path, String code, String message) {
        valid = false;
        errors.add(new ReportViewResponse.ValidationItem(path, code, message));
    }
}
