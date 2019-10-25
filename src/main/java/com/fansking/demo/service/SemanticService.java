package com.fansking.demo.service;


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

    /**
     * key:变量名   value:第一位是类型，第二位是值
     */
    private static List<Map<String, List<String>>> varStack = new ArrayList<>();
    public static String semanticParse(String text){
        TreeNode t= syntacticAnalyse(LexicalService.lexicalAnalyse(text));
        String syntaxException = SyntaxException.printExceptionList();
        if (syntaxException.length()>0){
            return "有语法错误请修正后再提交\n"+syntaxException;
        }
        varStack.add(new HashMap<>());
        for (TreeNode node:t.getTreeNodes()) {
            parseHeadNode(node);
        }
        varStack.remove(varStack.size()-1);
        StringBuilder sb = new StringBuilder();
        sb.append("错误警告信息:\n");
        for (String s:warningInfo
             ) {
            sb.append(s+"\n");
        }
        sb.append("中间代码过程为:\n");
        for (String s:codes
        ) {
            sb.append(s+"\n");
        }
        return sb.toString();
    }
    private static void parseHeadNode(TreeNode headNode){
        switch (headNode.getType()){
            case 0:
                varStack.add(new HashMap<>());
                for (TreeNode node:headNode.getTreeNodes()) {
                    parseHeadNode(node);
                }
                varStack.remove(varStack.size()-1);
                break;
            //if
            case 35: parseIfStmt(headNode);
            break;
            //while
            case 36:  parseWhileStmt(headNode);
            //int and real
            case 37:  parseDeclareStmt(headNode);break;
            //{
            case 17:
            case 18:
                return ;
            case 32:
                parseExp(headNode);
            default:{

                break;
            }
        }
    }


    private static void parseWhileStmt(TreeNode headNode){
        List<String> res;
        if(checkNodeType(headNode.getTreeNodes().get(2),32)){
            res= parseExp(headNode.getTreeNodes().get(2));
        }else{
            warningInfo.add("while语句出错");
            res= new ArrayList<>();
            res.add("literal_int");
            res.add("0");
        }

//        if(Double.parseDouble(res.get(1))>0){
//            codes.add("while语句判断条件成立"+res.get(1)+">0");
//            parseHeadNode(headNode.getTreeNodes().get(4));
//        }else if(Double.parseDouble(res.get(1))<=0){
//            codes.add("while语句判断条件失败"+res.get(1)+"<0,跳出循环");
//
//        }
        while (Double.parseDouble(res.get(1))>0){
            codes.add("while语句判断条件成立"+res.get(1)+">0");
            parseHeadNode(headNode.getTreeNodes().get(4));
            if(checkNodeType(headNode.getTreeNodes().get(2),32)){
                res= parseExp(headNode.getTreeNodes().get(2));
            }else{
                warningInfo.add("while语句出错");
                res= new ArrayList<>();
                res.add("literal_int");
                res.add("0");
            }
        }
        codes.add("while语句判断条件失败"+res.get(1)+"<0,跳出循环");

    }

    private static void parseAssignExp(String varName,
                                       List<String> r2){
        for(int j = varStack.size()-1;j>=0;j--){
            if(varStack.get(j).containsKey(varName)){
                varStack.get(j).replace(varName,r2);
            }
        }

    }
    /**
     * 声明语句 一定是5个或三个节点
     * @param node 头节点
     */
    private static void parseDeclareStmt(TreeNode node){
        String type = "literal_int";
        if(node.getTreeNodes().get(0).getType()==5){
            type = "literal_real";
        }
        List<String> r2;
        if(node.getTreeNodes().size()==5){

            if(node.getTreeNodes().get(3).getType()==32){
                r2 = parseExp(node.getTreeNodes().get(3));
            }else{
                r2 = findVar(node.getTreeNodes().get(3).getValue());
            }
            if(!r2.get(0).equals(type)){
                warningInfo.add("对"+node.getTreeNodes().get(1).getValue()+"赋值出现类型不匹配");
                r2.set(0,type);
            }
            codes.add("声明且初始化"+node.getTreeNodes().get(1).getValue()+"为"+r2.get(1));
            varStack.get(varStack.size()-1).put(node.getTreeNodes().get(1).getValue(),r2);
        }else{
            r2=new ArrayList<>();
            r2.add(type);
            r2.add("");
            codes.add("声明"+node.getTreeNodes().get(1).getValue());
            varStack.get(varStack.size()-1).put(node.getTreeNodes().get(1).getValue(),r2);
        }
    }
/**
     * 如果是if语句那么他的第三个节点一定是exp类型，第五个节点是一个（0，S）或某个语句，第六个是else或者没有
      * @param headNode	 头节点
     */

    private static void parseIfStmt(TreeNode headNode){
        List<String> res;
        if(checkNodeType(headNode.getTreeNodes().get(2),32)){
            res= parseExp(headNode.getTreeNodes().get(2));
        }else{
            warningInfo.add("if语句出错");
            res= new ArrayList<>();
            res.add("literal_int");
            res.add("1");
        }
        if(Double.parseDouble(res.get(1))>0){
            codes.add("if语句判断条件成立"+res.get(1)+">0");
            parseHeadNode(headNode.getTreeNodes().get(4));
        }else if(Double.parseDouble(res.get(1))<=0&&headNode.getTreeNodes().size()>5){
            codes.add("if语句判断条件失败"+res.get(1)+"<=0,执行else语句");
            parseHeadNode(headNode.getTreeNodes().get(6));
        }
        
    }
    /**
     * exp只可能有三个（布尔）四个（赋值）1个（变量或数值）
      * @param node	 头节点
     */
    private static List<String> parseExp(TreeNode node){
        if(node.getTreeNodes().size()==1&& (node.getTreeNodes().get(0).getType()==26|| node.getTreeNodes().get(0).getType()==27 )){
            List<String> res = new ArrayList<>();
            res.add(TreeNode.TABLE.get(node.getTreeNodes().get(0).getType()));
            res.add(node.getTreeNodes().get(0).getValue());
            return res;
        }
        if(node.getTreeNodes().size()>=3){
            List<String> r1;
            List<String> r2;
            switch (node.getTreeNodes().get(0).getType()){
                case 25:
                    r1= findVar(node.getTreeNodes().get(0).getValue());
                    break;
                case 32:
                    r1= parseExp(node.getTreeNodes().get(0));
                    break;
                case 33:
                    r1= parseAdditiveExp(node.getTreeNodes().get(0));
                    break;
                    //如果是左括号，那么不需要对其分析他一定是（AdditiveExp）直接返回AdditiveExp的值就好
                case 15:
                    return parseAdditiveExp(node.getTreeNodes().get(1));
                    default:
                        warningInfo.add("exp子节点出现错误类型");
                        r1= new ArrayList<>();
                        r1.add("literal_int");
                        r1.add("0");
                        break;
            }
            switch (node.getTreeNodes().get(2).getType()){
                case 25:
                    r2= findVar(node.getTreeNodes().get(2).getValue());
                    break;
                case 32:
                    r2= parseExp(node.getTreeNodes().get(2));
                    break;
                case 33:
                    r2= parseAdditiveExp(node.getTreeNodes().get(2));
                    break;
                default:
                    warningInfo.add("exp子节点出现错误类型");
                    r2= new ArrayList<>();
                    r2.add("literal_int");
                    r2.add("0");
                    break;
            }
            if(node.getTreeNodes().get(1).getType()==10){
                parseAssignExp(node.getTreeNodes().get(0).getValue(),r2);
                return null;
            }else{
                return parseNum(r1,r2,node.getTreeNodes().get(1).getValue());
            }


        }
        List<String> r1= new ArrayList<>();
        r1.add("literal_int");
        r1.add("0");
        warningInfo.add("exp子节点个数不正确，请查看");
        return r1;
    }

    /**
     * additiveExp一定有三个节点，第一个节点为exp/var 第二个节点是+-/* 第三个节点是exp/var
     * @param hNode
     * @return
     */
    private static List<String> parseAdditiveExp(TreeNode hNode){
        List<String> r1;
        List<String> r2;
        if(hNode.getTreeNodes().get(0).getType()==32){
            r1= parseExp(hNode.getTreeNodes().get(0));
        }else if(hNode.getTreeNodes().get(0).getType()==25){
            r1= findVar(hNode.getTreeNodes().get(0).getValue());
        }else{
            warningInfo.add("节点异常，多项式节点的子节点类型错误");
            r1= new ArrayList<>();
            r1.add("literal_int");
            r1.add("0");
        }
        if(hNode.getTreeNodes().get(2).getType()==32){
            r2= parseExp(hNode.getTreeNodes().get(2));
        }else if(hNode.getTreeNodes().get(2).getType()==25){
            r2= findVar(hNode.getTreeNodes().get(2).getValue());
        }else{
            warningInfo.add("节点异常，多项式节点的子节点类型错误");
            r2= new ArrayList<>();
            r2.add("literal_int");
            r2.add("0");
        }
        return parseNum(r1,r2,hNode.getTreeNodes().get(1).getValue());



    }

    /**
     *解析两数运算
     * @param r1
     * @param r2
     * @param op 符号
     * @return
     */
    private static List<String> parseNum(List<String> r1,
            List<String> r2,String op){
        List<String> resList = new ArrayList<>();
        String res;
        if(r1.get(0).equals("literal_int")&&r2.get(0).equals("literal_int")){
            res = parseIntOp(Integer.parseInt(r1.get(1)),Integer.parseInt(r2.get(1)),op).toString();
            resList.add("literal_int");
            resList.add(res);
        }else {
            res = parseDoubleOp(Double.parseDouble(r1.get(1)),Double.parseDouble(r2.get(1)),op).toString();
            resList.add("literal_real");
            resList.add(res);
        }
        return resList;
    }
    private static Integer parseIntOp(int a1,int a2,String op){
        int res;
        switch (op){
            case "+":
                res= a1+a2;
                break;
            case "-":
                res=  a1-a2;
                break;
            case "*":

                res=  a1*a2;
                break;
            case "/":
                if(a2 ==0){
                    warningInfo.add("出现除零错误！！");
                    res =0;
                }else{
                    res= a1/a2;
                }
                break;
            case ">":
                res = a1>a2?1:0;
                break;
            case "<":
                res = a1<a2?1:0;
                break;
            case "<=":
                res = a1<=a2?1:0;
                break;
            case ">=":
                res = a1>=a2?1:0;
                break;
            case "<>":
                res = a1!=a2?1:0;
                break;
            default:
                    warningInfo.add("出现运算符错误");
                    res = 0;
                    break;
        }
        codes.add(a1+op+a2+"="+res);
        return res;

    }
    private static Double parseDoubleOp(double a1,double a2,String op){
        double res;
        switch (op){
            case "+":
                res= a1+a2;
                break;
            case "-":
                res=  a1-a2;
                break;
            case "*":

                res=  a1*a2;
                break;
            case "/":
                res= a1/a2;
                break;
            case ">":
                res = a1>a2?1:0;
                break;
            case "<":
                res = a1<a2?1:0;
                break;
            case "<=":
                res = a1<=a2?1:0;
                break;
            case ">=":
                res = a1>=a2?1:0;
                break;
            case "<>":
                res = a1!=a2?1:0;
                break;
            default:
                warningInfo.add("出现运算符错误");
                res = 0;
                break;
        }
        codes.add(a1+op+a2+"="+res);
        return res;
    }
    private static Boolean checkNodeType(TreeNode node,int type){
        if(node.getType()==type){
            return true;
        }else{
            warningInfo.add(node.getType()+"应当是"+type);
//            System.err.println(node.getType()+"应当是"+type);
            return false;
        }
    }
    /**
     * 没有数组定义是可以接受的，但是由于初步考虑时没有考虑到数组初始化问题，所以在这里认为使用未赋值数组内容时默认为0，但是要加入警告信息
     * 如果是未定义变量也返回0
     */
    private static List<String> findVar(String varName){
        for(int j = varStack.size()-1;j>=0;j--){
            if(varStack.get(j).containsKey(varName)){
                if(varStack.get(j).get(varName).get(1).equals("")){
                    warningInfo.add("使用了未初始化的变量:"+varName);
                    List<String> res = new ArrayList<>();
                    res.add(varStack.get(j).get(varName).get(0));
                    res.add("0");
                    return res;
                }
                return varStack.get(j).get(varName);
            }
        }
        warningInfo.add("使用了未定义的变量:"+varName);
        List<String> res = new ArrayList<>();
        res.add("literal_int");
        res.add("0");
        return res;
    }

    public static void main(String []a){
        System.out.println(semanticParse("int a =5;\n" +
                "real b =3.3;\n" +
                "if((a+b)>9){//测试else是否正常工作\n" +
                "int b=6;\n" +
                "c=5+b;\n" +
                "\n" +
                "}else//测试不加大括号\n" +
                "a=6;\n" +
                "\n" +
                "\n" +
                "//测试未初始化变量是否报错\n" +
                "if((a+b)>0){\n" +
                "int b=6;\n" +
                "c=5+b;\n" +
                "//测试除0报错\n" +
                "b = b/0;\n" +
                "}else\n" +
                "a=6;\n" +
                "//测试while循环是否正常工作\n" +
                "while(a)\n" +
                "a=a-1;\n" +
                "\n" +
                "b=3.6+2.4*(5+2.1)"));
    }
}
