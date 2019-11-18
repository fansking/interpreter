package com.fansking.demo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 记录每个记号的类，分别有标识符、关键字、运算符、常量、分界符。
 * 在cmm的基础上增添了一些token
 * @author fansking
 * @date 2019/10/14 18:53
 *
 */
public class Token {
    /**
     * 存储token的类型,例如if,else,标识符,><等等
     */
    private int type;
    /**
     * 如果token需要存储值,则使用本字段存储
     */
    private String value;
    /**
     * 存储行号
     */
    private int lineNo;

    public static final Map<Integer,String> TABLE = new HashMap<Integer,String>() {
        {
            //错误token
            put(-1, "error");
            //关键字token
            put(1, "if");
            put(2, "else");
            put(3, "while");
            put(4,"int");
            put(5, "real");
            put(6, "+");
            put(7, "-");
            put(8, "*");
            put(9, "/");
            put(10, "=");
            put(11, "<");
            put(12, ">");
            put(13, "==");
            put(14, "<>");
            put(15, "(");
            put(16, ")");
            put(17, "{");
            put(18, "}");
            put(19, "[");
            put(20, "]");
            put(21, ";");
            put(22, "/*");
            put(23, "*/");
            put(24, "//");
            //标识符token
            put(25,"id");
            //数字token
            put(26, "literal_int");
            put(27, "literal_real");
            /*****************************************
             *新增token****25***************************
             ******************************************/
            put(28, "<=");
            put(29, ">=");
            put(30, "+=");
            put(31, "-=");
            put(90,"print");
            put(91,"scan");
            put(92,"&");
            put(93,"|");
            put(94,"^");
            put(95,",");
            put(97,"for");
            put(98,"continue");
            put(99,"break");
        }
    };
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getLineNo() {
        return lineNo;
    }

    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }

    public Token(int type, String value, int lineNo) {
        this.type = type;
        this.value = value;
        this.lineNo = lineNo;
    }

    public Token(int type, int lineNo) {
        this.type = type;
        this.lineNo = lineNo;
        this.value = TABLE.get(type);
    }

    @Override
    public String toString() {

        return "(" + type +
                ", " + value +
                ')';
    }
}
