package com.k12.common.dto;

import lombok.Data;

@Data
public class RelatedLinkVO {
    private String title;
    private String path;
    private String type;
    private String icon;

    public RelatedLinkVO() {
    }

    public RelatedLinkVO(String title, String path, String type, String icon) {
        this.title = title;
        this.path = path;
        this.type = type;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
