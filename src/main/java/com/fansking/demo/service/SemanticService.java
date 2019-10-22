package com.fansking.demo.service;

import com.alibaba.fastjson.JSON;
import com.sun.media.jfxmedia.events.AudioSpectrumEvent;

import java.util.*;

import static com.fansking.demo.service.SyntaxService.syntacticAnalyse;



public class SemanticService {
//    private static Map<String, List<String>> ID_TABLE = new HashMap<>();
//    private static Stack<TreeNode> stack = new Stack<>();
    private static TreeNode currentParentNode;
    /**
     * 警告和错误信息
     */
    private static List<String> warningInfo = new ArrayList<>();
    /**
     * 中间代码
     */
    private static List<String> codes=new ArrayList<>();


    private static List<Map<String, List<String>>> varStack = new ArrayList<>();
    public static String semanticParse(String text){
        TreeNode t= syntacticAnalyse(LexicalService.lexicalAnalyse(text));
        String syntaxException = SyntaxException.printExceptionList();
        if (syntaxException.length()>0){
            return "有语法错误请修正后再提交\n"+syntaxException;
        }
        for (TreeNode node:t.getTreeNodes()) {
//            parseHeadNode(node);
        }

        return "";
    }
    /*private static void parseHeadNode(TreeNode headNode){
        switch (headNode.getType()){
            case 0:
                for (TreeNode node:headNode.getTreeNodes()) {
                    parseHeadNode(node);
                }
                break;
            //if
            case 35: parseIfStmt(headNode);
            break;
            //while
            case 3: return parseWhileStmt();
            //int and real
            case 4:
            case 5: return parseDeclareStmt();
            //{
            case 17: return parseStmtBlock();
            // id
            case 25: return parseAssignStmt();
            default:{
                //consumeNextToken(type);
                SyntaxException.addSyntaxException(getNextTokenLineNo(currentToken),"语句首token错误"+(type+getNextTokenValue()));
                return null;
            }
        }
    }*/
/**
     * 如果是if语句那么他的第三个节点一定是exp类型，第五个节点是一个（0，S）或某个语句，第六个是else或者没有
      * @param headNode	 头节点
     */

    private static void parseIfStmt(TreeNode headNode){
        int flag;
        if(checkNodeType(headNode.getTreeNodes().get(2),32)){
            //parseExp(headNode.getTreeNodes().get(2));
        }

        
    }
    /**
     * exp只可能有三个（布尔）四个（赋值）1个（变量或数值） 这里我们将所有运算都认为是浮点运算
      * @param node	 头节点
     * @return java.lang.String
     */
    //private static List<String,String> parseExp(TreeNode node){
//        if(node.getTreeNodes().size()==1&& (node.getTreeNodes().get(0).getType()==26|| node.getTreeNodes().get(0).getType()==27 )){
//            return Double.parseDouble( node.getTreeNodes().get(0).getValue());
//        }
//        if(node.getTreeNodes().size()>=3){
//            switch (node.getTreeNodes().get(0).getType()){
//                case 25:
//
//            }
//        }
//        return 0.1;
   // }
    private static Boolean checkNodeType(TreeNode node,int type){
        if(node.getType()==type){
            return true;
        }else{
            System.err.println(node.getType()+"应当是"+type);
            return false;
        }
    }
    /**
     * 没有数组定义是可以接受的，但是由于初步考虑时没有考虑到数组初始化问题，所以在这里认为使用未赋值数组内容时默认为0，但是要加入警告信息
     */
    private static List<String> findVar(String varName){
        for(int j = varStack.size()-1;j>=0;j--){
            if(varStack.get(j).containsKey(varName)){
                return varStack.get(j).get(varName);
            }
        }
    }

    public static void main(String []a){
        System.out.println(Double.parseDouble("1"));
    }
}
