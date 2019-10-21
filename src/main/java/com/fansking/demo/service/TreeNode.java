package com.fansking.demo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author fansking
 * @date 2019/10/15 14:20
 */
public class TreeNode {
    /**
     * 存放后续节点
     */
    private List<TreeNode> treeNodes = new ArrayList<>();
    /**
     * 存放当前节点类型
     */
    private int type;
    /**
     * 存放当前节点值,可能是数字或者字符串
     */
    private String value;

    public static final Map<Integer,String> TABLE = new HashMap<Integer,String>() {
        {
            //错误node
            put(-1, "error");
            //初始节点
            put(0,"S");
            //关键字node
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
             *新增token*******************************
             ******************************************/
            put(28, "<=");
            put(29, ">=");
            put(30, "+=");
            put(31, "-=");
            //表达式
            put(32,"exp");
            //多项式
            put(33,"additiveExp");
            //if语句
            put(35,"if_stmt");
            put(36,"while_stmt");
            put(37,"assign_stmt");
        }
    };

    public List<TreeNode> getTreeNodes() {
        return treeNodes;
    }

    public void setTreeNodes(List<TreeNode> treeNodes) {
        this.treeNodes = treeNodes;
    }

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

    public TreeNode(int type, String value) {
        this.type = type;
        this.value = value;
    }

    public TreeNode(int type) {
        this.type = type;
        this.value =TABLE.get(type);
    }

    public TreeNode() {

    }

    @Override
    public String toString() {
        return "(" + type +
                ", " + value +
                ')';
    }
}
