package com.fansking.demo.service;



import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * @author fansking
 * @date 2019/10/15 14:46
 */
public class SyntaxService {
    private static TreeNode headNode;
    private static Token currentToken = null;
    private static ListIterator<Token> iterator = null;

    public static TreeNode syntacticAnalyse(LinkedList<Token> tokenList) {
        headNode = new TreeNode(0);
        iterator = tokenList.listIterator();
        while (iterator.hasNext()) {
            TreeNode t =parseStmt();
            if(null==t){
                headNode.getTreeNodes().add(new TreeNode(getNextTokenType(),getNextTokenValue()));
                consumeNextToken(getNextTokenType());
            }else{
                headNode.getTreeNodes().add(t);
            }

        }

        return headNode;
    }
    private static TreeNode parseStmt() {
        int type=getNextTokenType();
        switch (type) {
            case 0: return new TreeNode(0,TreeNode.TABLE.get(0));
            //if
            case 1: return parseIfStmt();
            //while
            case 3: return parseWhileStmt();
            //int and real
            //function
            case 4:
            case 5: return parseDeclareStmt();
            //{
            case 17: return parseStmtBlock();
            // id
            case 25: return parseAssignStmt();
            case 90:
                return parsePrintStmt();
            case 91:
                return parseScanStmt();
            default:{
                //consumeNextToken(type);
                SyntaxException.addSyntaxException(getNextTokenLineNo(currentToken),"语句首token错误"+(type+getNextTokenValue()));
                return null;
            }
        }
    }
private static TreeNode parsePrintStmt(){
    TreeNode node = new TreeNode(90);
    consumeNextToken(90);
    consumeNextToken(15,node);
    if(checkNextTokenType(25)){
        node.getTreeNodes().add(variableName());
    }
    consumeNextToken(16,node);
    consumeNextToken(21,node);
    return node;
}
    private static TreeNode parseScanStmt(){
        TreeNode node = new TreeNode(91);
        consumeNextToken(91);
        consumeNextToken(15,node);
        if(checkNextTokenType(25)){
            node.getTreeNodes().add(variableName());
        }
        consumeNextToken(16,node);
        consumeNextToken(21,node);
        return node;
    }
    /**
     * assign语句
     */
    private static TreeNode parseAssignStmt() {
        TreeNode node = new TreeNode(32);
        node.getTreeNodes().add(variableName());
        if(getNextTokenType()==10){
            consumeNextToken(10,node);
            node.getTreeNodes().add(parseExp());
        }
        consumeNextToken(21,node);
        return node;
    }

    /**
     * 解析if语句,需要默认读取下一行语句
     */
    private static TreeNode parseIfStmt() {
        TreeNode node = new TreeNode(35);

        //if
        consumeNextToken(1,node);
        //(
        consumeNextToken(15,node);
        node.getTreeNodes().add(parseExp());
        consumeNextToken(16,node);
        node.getTreeNodes().add(parseStmt());
        if (getNextTokenType() == 2) {
            consumeNextToken(2,node);
            node.getTreeNodes().add(parseStmt());
        }
        return node;
    }
    /**
     * while语句
     */
    private static TreeNode parseWhileStmt() {
        TreeNode node = new TreeNode(36);
        consumeNextToken(3,node);
        consumeNextToken(15,node);
        node.getTreeNodes().add(parseExp());
        consumeNextToken(16,node);
        node.getTreeNodes().add(parseStmt());
        return node;
    }
    /**
     * 表达式
     */
    private static TreeNode parseExp() {
        TreeNode node = new TreeNode(32);
        TreeNode leftNode = addtiveExp();
        if (checkNextTokenType(11,12,13,14,28,29)) {
            node.getTreeNodes().add(leftNode);
            node.getTreeNodes().add(logicalOp());
            node.getTreeNodes().add(addtiveExp());
        } else if(leftNode.getType()==33){
            node.getTreeNodes().add( leftNode);
            return node;
        }else{
            return leftNode;
        }
        return node;
    }
    /**
     * 多项式，左结合是先算左边的
     */
    private static TreeNode addtiveExp() {
        TreeNode node = new TreeNode(33);
        TreeNode leftNode = term();
//       TreeNode pNode = new TreeNode(33);
        if (checkNextTokenType(6,7)) {
            node.getTreeNodes().add(leftNode);
            node.getTreeNodes().add(addtiveOp());
            node.getTreeNodes().add(term());
        } else {
            return leftNode;
        }
        while (checkNextTokenType(6,7)){
            TreeNode pNode = new TreeNode(33);
            pNode.getTreeNodes().add(node);
            pNode.getTreeNodes().add(addtiveOp());
            pNode.getTreeNodes().add(term());
            node = pNode;
        }
        return node;
    }
    /**
     * 项，这里如果是乘除的话会多加一层exp的节点，使生成树时乘除运算比加减运算多一个层
     */
    private static TreeNode term() {
        TreeNode node = new TreeNode(32);
        //首先处理+-
        TreeNode leftNode = factor();
        //处理乘除
        if (checkNextTokenType(8, 9)) {
            node.getTreeNodes().add(leftNode);
            node.getTreeNodes().add(multiplyOp());
            node.getTreeNodes().add(term());
        } else {
            //下一个是加减返回数字factor
            return leftNode;
        }
        //如果是乘除直接返回node
        return node;
    }
    private static TreeNode factor(){
        if (iterator.hasNext()) {
            TreeNode expNode = new TreeNode(32);
            switch (getNextTokenType()) {
                case 26:
                case 27:
                    expNode.getTreeNodes().add(litreal());
                    break;
                //(
                case 15:
                    consumeNextToken(15,expNode);
                    expNode.getTreeNodes().add(parseExp());
                    consumeNextToken(16,expNode);
                    break;
                case 7:
                    expNode= new TreeNode(7);
                    currentToken = iterator.next();
                    expNode.getTreeNodes().add(term());
                    break;
                case 6:
                    expNode= new TreeNode(6);
                    currentToken = iterator.next();
                    expNode.getTreeNodes().add(term());
                    break;
                case -1:
                    expNode= new TreeNode(-1,getNextTokenValue());
                    SyntaxException.addSyntaxException(getNextTokenLineNo(currentToken),"含有词法错误"+expNode);
                    currentToken = iterator.next();
                    break;

                default:
                    //返回的不是expNode
                    return variableName();
            }
            return expNode;
        }
        SyntaxException.addSyntaxException(getNextTokenLineNo(currentToken),"应该有下一个token并且为数字或变量或加减运算符");
        return null;
    }
    /**
     * 语句block{}
     */
    private static TreeNode parseStmtBlock() {
        TreeNode node = new TreeNode(0);
        TreeNode temp = null;
        consumeNextToken(17,node);
        while (getNextTokenType() != 18) {
            temp = parseStmt();
            node.getTreeNodes().add(temp);
            if(temp==null){
                SyntaxException.addSyntaxException(getNextTokenLineNo(currentToken),"block区域中有错误");
                if(!iterator.hasNext()){
                    SyntaxException.addSyntaxException(getNextTokenLineNo(currentToken),"大括号没有结尾");
                    break;
                }
            }
        }
        consumeNextToken(18,node);
        return node;
    }


    /**
     * 判断是否是数字
     *
     */
    private static TreeNode litreal() {
        if (iterator.hasNext()) {
            currentToken = iterator.next();
            int type = currentToken.getType();
            TreeNode node = new TreeNode(type);
            node.setValue(currentToken.getValue());
            return node;

        }
        SyntaxException.addSyntaxException(getNextTokenLineNo(currentToken),"缺少数字");
        return null;
    }
    private static TreeNode parseDeclareStmt() {
        //可能是函数或定义变量
        TreeNode funNode = new TreeNode(38);



        TreeNode node = new TreeNode(37);
        node.getTreeNodes().add(new TreeNode(getNextTokenType()));
        funNode.getTreeNodes().add(new TreeNode(getNextTokenType()));
        TreeNode varNode = new TreeNode();
        if (checkNextTokenType(4, 5)) {
            currentToken = iterator.next();
        } else {
            SyntaxException.addSyntaxException(getNextTokenLineNo(currentToken),"缺少定义符");
        }
        if (checkNextTokenType(25)) {
            currentToken = iterator.next();
            varNode.setType(currentToken.getType());
            varNode.setValue(currentToken.getValue());
            node.getTreeNodes().add(varNode);
            funNode.getTreeNodes().add(varNode);
        } else {
            SyntaxException.addSyntaxException(getNextTokenLineNo(currentToken),"缺少标识符");
        }

        if(checkNextTokenType(17)){
            varNode.setType(39);
            funNode.getTreeNodes().add(parseStmtBlock());
            return funNode;
        }
        if (getNextTokenType() == 19) {
            //数组
            consumeNextToken(19,varNode);
            varNode.getTreeNodes().add(parseExp());
            consumeNextToken(20,varNode);
        }
        if (getNextTokenType() == 10) {
            consumeNextToken(10,node);

            node.getTreeNodes().add(parseExp());
        }

        if (getNextTokenType() == 21) {
            consumeNextToken(21,node);
        }else{
            SyntaxException.addSyntaxException(getNextTokenLineNo(currentToken),"缺少分号结尾");
        }
        return node;
    }
    /**
     * 变量名,可能是单个的变量,也可能是数组的一个元素
     */
    private static TreeNode variableName(){
        TreeNode node = new TreeNode(25);
        if (checkNextTokenType(25)) {
            currentToken = iterator.next();
            node.setValue(currentToken.getValue());
        } else {
            SyntaxException.addSyntaxException(getNextTokenLineNo(currentToken),"缺少变量");
        }
        //数组的情况下
        if (getNextTokenType() == 19) {
            consumeNextToken(19,node);
            //此处可能是定义数组，即[]之间无内容
            if(getNextTokenType()==20){
                consumeNextToken(20,node);
                return node;
            }else{
                node.getTreeNodes().add(parseExp());
            }

            consumeNextToken(20,node);
        }
        return node;
    }
    /**
     * 加减运算符
     */
    private static TreeNode addtiveOp() {//throws ParserException {
        if (iterator.hasNext()) {
            currentToken = iterator.next();
            int type = currentToken.getType();
            if (type == 6 || type == 7) {
                return  new TreeNode(type);

            }
        }
        SyntaxException.addSyntaxException(getNextTokenLineNo(currentToken),"应当有加减运算符");
        return null;
    }
    /**
     * 逻辑运算符
     */
    private static TreeNode logicalOp() {
        if (iterator.hasNext()) {
            currentToken = iterator.next();
            int type = currentToken.getType();
            if (type == 11||type ==12||type ==13||type ==14||type ==28||type ==29) {
                return new TreeNode(type);
            }
        }
        SyntaxException.addSyntaxException(getNextTokenLineNo(currentToken),"应当有逻辑运算符");
        return null;
    }

    /**
     * 乘除运算符
     */
    private static TreeNode multiplyOp() {
        if (iterator.hasNext()) {
            currentToken = iterator.next();
            int type = currentToken.getType();
            if (type == 8 || type == 9) {
                return  new TreeNode(type);
            }
        }
        SyntaxException.addSyntaxException(getNextTokenLineNo(currentToken), "缺少乘除运算");
        return null;
    }
    /**
     * 检查下一个token的类型是否和type中的某一个元素相同,调用此函数currentToken位置不会移动
     * @param type int[]
     * @return 有相同为true,全部不同为false
     */
    private static boolean checkNextTokenType(int ... type) {
        if (iterator.hasNext()) {
            int nextType = iterator.next().getType();
            iterator.previous();
            for (int each : type) {
                if (nextType == each) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * 获取下一个token的type,如果没有下一个token,则返回-2
     */
    private static int getNextTokenType() {
        if (iterator.hasNext()) {
            int type = iterator.next().getType();
            iterator.previous();
            return type;
        }
        return -2;
    }
    /**
     * 获取下一个token的value,如果没有下一个token,则返回空字符串
     */
    private static String getNextTokenValue() {
        if (iterator.hasNext()) {
            String  value = iterator.next().getValue();
            iterator.previous();
            return value;
        }
        else{
            return "";
        }
    }
    /**
     * 获取下一个token的lineNo,如果没有下一个token,则返回-1
     */
    private static int getNextTokenLineNo(Token currentToken) {
        if (iterator.hasNext()) {
            int lineNo = iterator.next().getLineNo();
            iterator.previous();
            return lineNo;
        }
        else{
            return currentToken.getLineNo();
        }
    }

    /**
     * 消耗掉下一个token,要求必须是type类型,消耗之后currentToken值将停在最后消耗的token上
     * @param type int
     */
    private static void consumeNextToken(int type){
        if (iterator.hasNext()) {
            currentToken = iterator.next();
            if (currentToken.getType() == type) {
                return;
            }
            else{
                SyntaxException.addSyntaxException(getNextTokenLineNo(currentToken),currentToken+" -> " + new Token(type, 0));
                currentToken = iterator.previous();
            }
        }
    }
    /**
     * 消耗掉下一个token,要求必须是type类型,消耗之后currentToken值将停在最后消耗的token上
     * @param type int
     */
    private static void consumeNextToken(int type,TreeNode node){
        if (iterator.hasNext()) {
            currentToken = iterator.next();
            if(null == currentToken){
                return;
            }
            if (currentToken.getType() == type) {
                node.getTreeNodes().add(new TreeNode(type));
                return;
            }
            else{
                SyntaxException.addSyntaxException(getNextTokenLineNo(currentToken),currentToken+" -> " + new Token(type, 0));
                currentToken = iterator.previous();
            }
        }
    }
    public static String output(TreeNode node,int depth){
        String s ="";
        int i = 0;
        for(int j = 0;j<depth-1;j++){
            s+="\t";
        }
        s+=node+"\n";
        if(null==node){
            return s;
        }
        while (i<node.getTreeNodes().size()){
            TreeNode t = node.getTreeNodes().get(i);
            i++;
            s=s+output(t,depth+1);
        }
        return s;
    }
    public static void main(String [] a){
        TreeNode t= syntacticAnalyse(LexicalService.lexicalAnalyse("int a[5];\n" +
                "a[1]=3;"));
        SyntaxException.printExceptionList();
        System.out.println(output(t,1));


    }
}
