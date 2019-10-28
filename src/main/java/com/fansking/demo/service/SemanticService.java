package com.fansking.demo.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.*;

import static com.fansking.demo.service.SyntaxService.syntacticAnalyse;


public class SemanticService {

    private static TreeNode currentParentNode;
    /**
     * 警告和错误信息
     */
    /**
     * 中间代码
     */
    private static List<String> codes = new ArrayList<>();
    /**
     * 控制台输出
     */
    private static List<String> out = new ArrayList<>();
    /**
     * key:变量名   value:第一位是类型，第二位是值
     */
    private static List<Map<String, List<String>>> varStack = new ArrayList<>();

    public static String semanticParse(String text) {
        codes.clear();
        varStack.clear();
        out.clear();
        TreeNode t = syntacticAnalyse(LexicalService.lexicalAnalyse(text));
        String syntaxException = SyntaxException.printExceptionList();
        if (syntaxException.length() > 0) {
            return "有语法错误请修正后再提交\n" + syntaxException;
        }
        varStack.add(new HashMap<>());
        for (TreeNode node : t.getTreeNodes()) {
            parseHeadNode(node);
        }
        varStack.remove(varStack.size() - 1);
        StringBuilder sb = new StringBuilder();
        sb.append("中间过程为:\n");
        for (String s : codes
        ) {
            sb.append(s + "\n");
        }
        sb.append("控制台输出为:\n");
        for (String s : out
        ) {
            sb.append(s + "\n");
        }
        return sb.toString();
    }

    private static void parseHeadNode(TreeNode headNode) {
        switch (headNode.getType()) {
            case 0:
                varStack.add(new HashMap<>());
                for (TreeNode node : headNode.getTreeNodes()) {
                    parseHeadNode(node);
                }
                varStack.remove(varStack.size() - 1);
                break;
            //if
            case 35:
                parseIfStmt(headNode);
                break;
            //while
            case 36:
                parseWhileStmt(headNode);
                break;
            //int and real
            case 37:
                parseDeclareStmt(headNode);
                break;
            //{
            case 17:
            case 18:
                return;
            case 32:
                parseExp(headNode);
                break;
            case 90:
                parsePrintStmt(headNode);
                break;
            case 91:
                parseScanStmt(headNode);
                break;
            default: {

                break;
            }
        }
    }

    private static void parsePrintStmt(TreeNode headNode) {
        out.add(findVar(headNode.getTreeNodes().get(1).getValue(), true).get(1));
    }

    private static void parseScanStmt(TreeNode node) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        List<String> r1;
        List<String> r2 = new ArrayList<>();
        TreeNode varNode = node.getTreeNodes().get(1);
        if (varNode.getType() == 25) {
            r1 = findVar(getVarName(varNode), false);
            r2.add(r1.get(0));
            String num = "0";
            try {
                num = br.readLine();

            } catch (Exception e) {
                codes.add("输入的数字有误,初始化为0");
                num = "0";
            }

            r2.add(num.toString());
            parseAssignExp(getVarName(varNode), r2);

        }
        return;
    }

    private static void parseWhileStmt(TreeNode headNode) {
        List<String> res;
        if (checkNodeType(headNode.getTreeNodes().get(2), 32)) {
            res = parseExp(headNode.getTreeNodes().get(2));
        } else {
            codes.add("while语句出错");
            res = new ArrayList<>();
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
        while (Double.parseDouble(res.get(1)) > 0) {
            codes.add("while语句判断条件成立" + res.get(1) + ">0");
            parseHeadNode(headNode.getTreeNodes().get(4));
            if (checkNodeType(headNode.getTreeNodes().get(2), 32)) {
                res = parseExp(headNode.getTreeNodes().get(2));
            } else {
                codes.add("while语句出错");
                res = new ArrayList<>();
                res.add("literal_int");
                res.add("0");
            }
        }
        codes.add("while语句判断条件失败" + res.get(1) + "<0,跳出循环");

    }

    private static void parseAssignExp(String varName,
                                       List<String> r2) {
        for (int j = varStack.size() - 1; j >= 0; j--) {
            if (varStack.get(j).containsKey(varName)) {
                if (varStack.get(j).get(varName).get(0).equals(r2.get(0)) || varStack.get(j).get(varName).get(0).equals("literal_real")) {
                    varStack.get(j).replace(varName, r2);
                    codes.add("变量" + varName + "被赋值为" + r2.get(1));
                    return;
                } else {
                    codes.add("变量赋值类型不匹配，自动转换类型");
                    r2.set(1, r2.get(1).split("\\.")[0]);
                    r2.set(0, "literal_int");
                    varStack.get(j).replace(varName, r2);
                    codes.add("变量" + varName + "被赋值为" + r2.get(1));
                    return;
                }

            }
        }

    }

    private static String getVarName(TreeNode node) {
        if (node.getTreeNodes().size() == 3) {
            List<String> num = parseExp(node.getTreeNodes().get(1));
            if (num.get(0).equals("literal_real")) {
                codes.add("数组偏移量不能是小数" + num.get(1));
            }
            int length = (int) Double.parseDouble(num.get(1));
            return node.getValue() + "[" + length + "]";
        } else {
            return node.getValue();
        }
    }

    /**
     * 声明语句 一定是5个或三个节点
     *
     * @param node 头节点
     */
    private static void parseDeclareStmt(TreeNode node) {
        String type = "literal_int";
        if (node.getTreeNodes().get(0).getType() == 5) {
            type = "literal_real";
        }
        if (findVarInSingleLayer(node.getTreeNodes().get(1).getValue())) {
            codes.add("变量" + node.getTreeNodes().get(1).getValue() + "已经声明过");
        }
        List<String> r2;
        if (node.getTreeNodes().size() == 5) {

            if (node.getTreeNodes().get(3).getType() == 32) {
                r2 = parseExp(node.getTreeNodes().get(3));
            } else if (node.getTreeNodes().get(3).getType() == 25) {

                r2 = findVar(getVarName(node.getTreeNodes().get(3)), true);
            } else if (node.getTreeNodes().get(3).getType() == 33) {
                r2 = parseAdditiveExp(node.getTreeNodes().get(3));

            } else {
                codes.add("声明赋值变量时出现意外节点类型" + node.getTreeNodes().get(3).getValue());
                r2 = new ArrayList<>();
                r2.add(type);
                r2.add("0");
            }
            if (!r2.get(0).equals(type)) {
                codes.add("对" + node.getTreeNodes().get(1).getValue() + "赋值出现类型不匹配");
                r2.set(0, type);
            }
            codes.add("声明且初始化" + node.getTreeNodes().get(1).getValue() + "为" + r2.get(1));
            varStack.get(varStack.size() - 1).put(node.getTreeNodes().get(1).getValue(), r2);
        } else {
            if (node.getTreeNodes().get(1).getTreeNodes().size() == 3) {
                TreeNode idNode = node.getTreeNodes().get(1);
                List<String> num = parseExp(idNode.getTreeNodes().get(1));
                if (num.get(0).equals("literal_real")) {
                    codes.add("数组定义长度不能是小数" + num.get(1));

                }
                int length = (int) Double.parseDouble(num.get(1));
                r2 = new ArrayList<>();
                r2.add(type);
                r2.add("");
                for (int i = 0; i < length; i++) {
                    varStack.get(varStack.size() - 1).put(node.getTreeNodes().get(1).getValue() + "[" + i + "]", r2);
                }
                codes.add("声明长度为" + length + "数组" + node.getTreeNodes().get(1).getValue());
                return;
            }
            r2 = new ArrayList<>();
            r2.add(type);
            r2.add("");
            codes.add("声明" + node.getTreeNodes().get(1).getValue());
            varStack.get(varStack.size() - 1).put(node.getTreeNodes().get(1).getValue(), r2);

        }
    }

    /**
     * 如果是if语句那么他的第三个节点一定是exp类型，第五个节点是一个（0，S）或某个语句，第六个是else或者没有
     *
     * @param headNode 头节点
     */

    private static void parseIfStmt(TreeNode headNode) {
        List<String> res;
        if (checkNodeType(headNode.getTreeNodes().get(2), 32)) {
            res = parseExp(headNode.getTreeNodes().get(2));
        } else {
            codes.add("if语句出错");
            res = new ArrayList<>();
            res.add("literal_int");
            res.add("1");
        }
        if (Double.parseDouble(res.get(1)) > 0) {
            codes.add("if语句判断条件成立" + res.get(1) + ">0");
            parseHeadNode(headNode.getTreeNodes().get(4));
        } else if (Double.parseDouble(res.get(1)) <= 0 && headNode.getTreeNodes().size() > 5) {
            codes.add("if语句判断条件失败" + res.get(1) + "<=0,执行else语句");
            parseHeadNode(headNode.getTreeNodes().get(6));
        }

    }

    /**
     * exp只可能有三个（布尔）四个（赋值）1个（变量或数值）
     *
     * @param node 头节点
     */
    private static List<String> parseExp(TreeNode node) {
        if (node.getTreeNodes().size() == 1 && (node.getTreeNodes().get(0).getType() == 26 || node.getTreeNodes().get(0).getType() == 27)) {
            List<String> res = new ArrayList<>();
            res.add(TreeNode.TABLE.get(node.getTreeNodes().get(0).getType()));
            res.add(node.getTreeNodes().get(0).getValue());
            return res;
        }
        if (node.getTreeNodes().size() == 1 && (node.getTreeNodes().get(0).getType() == 33)) {
            return parseAdditiveExp(node.getTreeNodes().get(0));
        }
        if (node.getTreeNodes().size() >= 3) {
            boolean flag = node.getTreeNodes().get(1).getType() == 10;
            List<String> r1;
            List<String> r2;
            switch (node.getTreeNodes().get(0).getType()) {
                case 25:
                    r1 = findVar(getVarName(node.getTreeNodes().get(0)), !flag);
                    break;
                case 32:
                    r1 = parseExp(node.getTreeNodes().get(0));
                    break;
                case 33:
                    r1 = parseAdditiveExp(node.getTreeNodes().get(0));
                    break;
                //如果是左括号，那么不需要对其分析他一定是（AdditiveExp）直接返回AdditiveExp的值就好
                case 15:
                    return parseExp(node.getTreeNodes().get(1));
                default:
                    codes.add("exp子节点出现错误类型");
                    r1 = new ArrayList<>();
                    r1.add("literal_int");
                    r1.add("0");
                    break;
            }
            switch (node.getTreeNodes().get(2).getType()) {
                case 25:
                    r2 = findVar(getVarName(node.getTreeNodes().get(2)), true);
                    break;
                case 32:
                    r2 = parseExp(node.getTreeNodes().get(2));
                    break;
                case 33:
                    r2 = parseAdditiveExp(node.getTreeNodes().get(2));
                    break;
                default:
                    codes.add("exp子节点出现错误类型");
                    r2 = new ArrayList<>();
                    r2.add("literal_int");
                    r2.add("0");
                    break;
            }
            if (flag) {
                parseAssignExp(getVarName(node.getTreeNodes().get(0)), r2);
                return null;
            } else {
                return parseNum(r1, r2, node.getTreeNodes().get(1).getValue());
            }


        }
        List<String> r1 = new ArrayList<>();
        r1.add("literal_int");
        r1.add("0");
        codes.add("exp子节点个数不正确，请查看");
        return r1;
    }

    /**
     * additiveExp一定有三个节点，第一个节点为exp/var 第二个节点是+-/* 第三个节点是exp/var
     *
     * @param hNode
     * @return
     */
    private static List<String> parseAdditiveExp(TreeNode hNode) {
        List<String> r1;
        List<String> r2;
        if (hNode.getTreeNodes().get(0).getType() == 32) {
            r1 = parseExp(hNode.getTreeNodes().get(0));
        } else if (hNode.getTreeNodes().get(0).getType() == 25) {
            r1 = findVar(getVarName(hNode.getTreeNodes().get(0)), true);
        } else if (hNode.getTreeNodes().get(0).getType() == 33) {
            r1 = parseAdditiveExp(hNode.getTreeNodes().get(0));
        } else {
            codes.add("节点异常，多项式节点的子节点类型错误");
            r1 = new ArrayList<>();
            r1.add("literal_int");
            r1.add("0");
        }
        if (hNode.getTreeNodes().get(2).getType() == 32) {
            r2 = parseExp(hNode.getTreeNodes().get(2));
        } else if (hNode.getTreeNodes().get(2).getType() == 25) {
            r2 = findVar(getVarName(hNode.getTreeNodes().get(2)), true);
        } else if (hNode.getTreeNodes().get(2).getType() == 33) {
            r2 = parseAdditiveExp(hNode.getTreeNodes().get(2));
        } else {
            codes.add("节点异常，多项式节点的子节点类型错误");
            r2 = new ArrayList<>();
            r2.add("literal_int");
            r2.add("0");
        }
        return parseNum(r1, r2, hNode.getTreeNodes().get(1).getValue());


    }

    /**
     * 解析两数运算
     *
     * @param r1
     * @param r2
     * @param op 符号
     * @return
     */
    private static List<String> parseNum(List<String> r1,
                                         List<String> r2, String op) {
        List<String> resList = new ArrayList<>();
        String res;
        if (r1.get(0).equals("literal_int") && r2.get(0).equals("literal_int")) {
            res = parseIntOp(Integer.parseInt(r1.get(1)), Integer.parseInt(r2.get(1)), op).toString();
            resList.add("literal_int");
            resList.add(res);
        } else {
            res = parseDoubleOp(Double.parseDouble(r1.get(1)), Double.parseDouble(r2.get(1)), op).toString();
            resList.add("literal_real");
            resList.add(res);
        }
        return resList;
    }

    private static Integer parseIntOp(int a1, int a2, String op) {
        int res;
        switch (op) {
            case "+":
                res = a1 + a2;
                break;
            case "-":
                res = a1 - a2;
                break;
            case "*":

                res = a1 * a2;
                break;
            case "/":
                if (a2 == 0) {
                    codes.add("出现除零错误！！");
                    res = 0;
                } else {
                    res = a1 / a2;
                }
                break;
            case ">":
                res = a1 > a2 ? 1 : 0;
                break;
            case "<":
                res = a1 < a2 ? 1 : 0;
                break;
            case "<=":
                res = a1 <= a2 ? 1 : 0;
                break;
            case ">=":
                res = a1 >= a2 ? 1 : 0;
                break;
            case "<>":
                res = a1 != a2 ? 1 : 0;
                break;
            default:
                codes.add("出现运算符错误");
                res = 0;
                break;
        }
        codes.add(a1 + op + a2 + "=" + res);
        return res;

    }

    private static Double parseDoubleOp(double a1, double a2, String op) {
        double res;
        switch (op) {
            case "+":
                res = a1 + a2;
                break;
            case "-":
                res = a1 - a2;
                break;
            case "*":

                res = a1 * a2;
                break;
            case "/":
                res = a1 / a2;
                break;
            case ">":
                res = a1 > a2 ? 1 : 0;
                break;
            case "<":
                res = a1 < a2 ? 1 : 0;
                break;
            case "<=":
                res = a1 <= a2 ? 1 : 0;
                break;
            case ">=":
                res = a1 >= a2 ? 1 : 0;
                break;
            case "<>":
                res = a1 != a2 ? 1 : 0;
                break;
            default:
                codes.add("出现运算符错误");
                res = 0;
                break;
        }
        codes.add(a1 + op + a2 + "=" + res);
        return res;
    }

    private static Boolean checkNodeType(TreeNode node, int type) {
        if (node.getType() == type) {
            return true;
        } else {
            codes.add(node.getType() + "应当是" + type);
//            System.err.println(node.getType()+"应当是"+type);
            return false;
        }
    }

    private static Boolean findVarInSingleLayer(String varName) {
        int index = varStack.size() - 1;
        return varStack.get(index).containsKey(varName);
    }

    /**
     * 没有数组定义是可以接受的，但是由于初步考虑时没有考虑到数组初始化问题，所以在这里认为使用未赋值数组内容时默认为0，但是要加入警告信息
     * 如果是未定义变量也返回0
     */
    private static List<String> findVar(String varName, boolean flag) {
        for (int j = varStack.size() - 1; j >= 0; j--) {
            if (varStack.get(j).containsKey(varName)) {
                if (varStack.get(j).get(varName).get(1).equals("") & flag) {
                    codes.add("使用了未初始化的变量:" + varName);
                    List<String> res = new ArrayList<>();
                    res.add(varStack.get(j).get(varName).get(0));
                    res.add("0");
                    return res;
                }
                return varStack.get(j).get(varName);
            }
        }
        codes.add("使用了未声明的变量:" + varName);
        List<String> res = new ArrayList<>();
        res.add("literal_int");
        res.add("0");
        return res;
    }

    public static void main(String[] a) {
        System.out.println(semanticParse("int a[5];\n" +
                "a[1]=3;\n" +
                "a[2]=a[1];\n" +
                "if(a[3]>5)\n" +
                "a[1]=a[1]+1;\n" +
                "a[5]=4.4;\n"));

    }
}
