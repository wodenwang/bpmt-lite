/*
 * $HeadURL: $
 * $Id: $
 * Copyright (c) 2013 by Riversoft System, all rights reserved.
 */
package com.riversoft.module.frame;

import com.riversoft.core.db.po.Code2NameVO;

/**
 * 菜单打开类型枚举
 * 
 * @author Woden
 * 
 */
public enum MenuOpenType implements Code2NameVO {

    NONE(0, "无操作"), AJAX_ZONE(1, "打开网址"), EXTERNAL_IFRAME(2, "第三方网页")

    ;
    private Object code;
    private String showName;

    private MenuOpenType(Integer code, String showName) {
        this.code = code;
        this.showName = showName;
    }

    /**
     * @return the code
     */
    public Object getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(Object code) {
        this.code = code;
    }

    /**
     * @return the showName
     */
    public String getShowName() {
        return showName;
    }

    /**
     * @param showName the showName to set
     */
    public void setShowName(String showName) {
        this.showName = showName;
    }
}
