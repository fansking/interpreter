package com.fansking.demo.service;

import com.alibaba.fastjson.JSON;

import java.util.*;

import static com.fansking.demo.service.SyntaxService.syntacticAnalyse;

/**
 * @author fansking
 * @date 2019/10/22 10:21
 */
public class SemanticService {
//    private static Map<String, List<String>> ID_TABLE = new HashMap<>();
//    private static Stack<TreeNode> stack = new Stack<>();
    /**
    变量栈，不使用Stack是因为要取上一级的变量
     */
    private static List<Map<String, List<String>>> varStack = new ArrayList<>();
    public static String semanticParse(String text){
        TreeNode t= syntacticAnalyse(LexicalService.lexicalAnalyse(text));
        String syntaxException = SyntaxException.printExceptionList();
        if (syntaxException.length()>0){
            return "有语法错误请修正后再提交\n"+syntaxException;
        }

        return "";
    }
    public static Integer parseExp(){
        return 0;
    }
}
