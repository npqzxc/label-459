package com.toolplatform.dto;

public class TextConvertRequest {
    private String text;
    private String type; // uppercase, lowercase, titlecase

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
