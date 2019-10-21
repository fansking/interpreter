package com.fansking.demo.service;

/**
 * @author fansking
 * @date 2019/10/14 23:41
 */
public class ParamData {
    String text;

    public ParamData(String text) {
        this.text = text;
    }

    public ParamData() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
