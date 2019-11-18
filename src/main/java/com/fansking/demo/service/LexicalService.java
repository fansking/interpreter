package com.fansking.demo.service;


import java.util.LinkedList;

/**
 * @author fansking
 * @date 2019/10/14 18:51
 */
public class LexicalService {

    /**
     * 在扫描中实时改变的临时字符
     */
    private static char currentChar;
    /**
     * 在扫描中实时改变的行数
     */
    private static int lineNo;
    /**
     * 在生成输出文本时的实时行数
     */
    private static int currentLineNo;
    /**
     * 在扫描中实时改变的临时字符位置
     */
    private static int currentPosition;
    /**
     * 源程序字符串
     */
    public static String text;

    /**
     * 读取下一个字符，并且当前字符是换行符时改变lineNo
     */
    private static void readChar() {
        if(currentPosition<text.length()){
            currentChar = text.charAt(currentPosition);
        }else{
            currentPosition += 1;
        }
       currentPosition += 1;
        if (currentChar == '\n') {
            lineNo++;
        }
    }

    public static LinkedList<Token> lexicalAnalyse(String fileContent) {
        currentPosition = 0;
        lineNo = 1;
        text = fileContent;
        LinkedList<Token> tokenList = new LinkedList<Token>();
        StringBuilder sb = new StringBuilder();
        readChar();

        while (currentPosition<=text.length()) {
            //\t 水平制表符
            //\r 回车
            //\n 回车换行
            //\f换页
            if (currentChar == '\n'
                    || currentChar == '\r'
                    || currentChar == '\t'
                    || currentChar == '\f'
                    || currentChar == ' ') {
                readChar();
                continue;
            }
            //简单特殊符号
            switch (currentChar) {
                case ',':
                    tokenList.add(new Token(95,lineNo));
                    readChar();
                    continue;
                case '|':
                    tokenList.add(new Token(93,lineNo));
                    readChar();
                    continue;
                case '&':
                    tokenList.add(new Token(92,lineNo));
                    readChar();
                    continue;
                case '^':tokenList.add(new Token(94,lineNo));
                    readChar();
                    continue;
                case ';':
                    tokenList.add(new Token(21, lineNo));
                    readChar();
                    continue;
                case '*':
                    tokenList.add(new Token(8, lineNo));
                    readChar();
                    continue;
                case '(':
                    tokenList.add(new Token(15, lineNo));
                    readChar();
                    continue;
                case ')':
                    tokenList.add(new Token(16, lineNo));
                    readChar();
                    continue;
                case '[':
                    tokenList.add(new Token(19, lineNo));
                    readChar();
                    continue;
                case ']':
                    tokenList.add(new Token(20, lineNo));
                    readChar();
                    continue;
                case '{':
                    tokenList.add(new Token(17, lineNo));
                    readChar();
                    continue;
                case '}':
                    tokenList.add(new Token(18, lineNo));
                    readChar();
                    continue;
                default:
                    break;

            }

            /**
             * 复合特殊符号,且应当注意如果readChar之后的char并没有起作用，则应当不再读取，直接进行下一次循环
             *
             * */
            if (currentChar == '/') {
                readChar();
                if (currentChar == '*') {
                    readChar();
                    //使用死循环消耗多行注释内字符
                    while (true) {
                        if (currentChar == '*') {
                            readChar();
                            if (currentChar == '/') {
                                readChar();
                                break;
                            }
                        } else {
                            readChar();
                            if(currentPosition>text.length()){
                                tokenList.add(new Token(-1,"多行注释未闭合",currentLineNo));
                                break;
                            }
                        }
                    }
                    continue;
                } else if (currentChar == '/') {
                    while (currentChar != '\n') {
                        readChar();
                        if(currentPosition>text.length()){
                            break;
                        }
                    }
                    continue;
                } else {
                    tokenList.add(new Token(9, lineNo));
                    continue;
                }
            } else if (currentChar == '=') {
                readChar();
                if (currentChar == '=') {
                    tokenList.add(new Token(13, lineNo));
                    readChar();
                } else {
                    tokenList.add(new Token(10, lineNo));
                }
                continue;
            } else if (currentChar == '>') {
                readChar();
                if (currentChar == '=') {
                    tokenList.add(new Token(29, lineNo));
                    readChar();
                } else {
                    tokenList.add(new Token(12, lineNo));
                }
                continue;
            } else if (currentChar == '<') {
                readChar();
                if (currentChar == '=') {
                    tokenList.add(new Token(28, lineNo));
                    readChar();
                } else if (currentChar == '>') {
                    tokenList.add(new Token(14, lineNo));
                    readChar();
                } else {
                    tokenList.add(new Token(11, lineNo));
                }
                continue;
            }else if (currentChar == '+'){
                readChar();
                if (currentChar == '=') {
                    tokenList.add(new Token(30, lineNo));
                    readChar();
                } else {
                    tokenList.add(new Token(6, lineNo));
                }
                continue;
            }else if (currentChar == '-'){
                readChar();
                if (currentChar == '=') {
                    tokenList.add(new Token(31, lineNo));
                    readChar();
                } else {
                    tokenList.add(new Token(7, lineNo));
                }
                continue;
            }
            /**
             * 检查是否是数字，这里对不合规范的数字，例如2.4.5或09这样的数字抛出错误
             */
            if (currentChar >= '0' && currentChar <= '9') {
                boolean isZero = false;
                boolean isReal = false;
                boolean isError = false;
                boolean isF=false;
                if (currentChar=='0'){
                    isZero=true;
                }
                while ((currentChar >= '0' && currentChar <= '9') || currentChar == '.') {
                    if (currentChar == '.') {
                        if (isReal) {
                            isError=true;
                        } else {
                            isReal = true;
                        }
                    }

                    sb.append(currentChar);
                    readChar();
                }
                if (tokenList.size()==1){
                    if(tokenList.get(tokenList.size()-1).getType()==7){
                        isF =true;
                        tokenList.remove(tokenList.size()-1);
                    }
                }else if(tokenList.size()>1){
                    if(tokenList.get(tokenList.size()-1).getType()==7&&(tokenList.get(tokenList.size()-2).getType()==10||
                            tokenList.get(tokenList.size()-2).getType()==15||
                            tokenList.get(tokenList.size()-2).getType()==95||
                            tokenList.get(tokenList.size()-2).getType()==17)){
                        isF=true;
                        tokenList.remove(tokenList.size()-1);
                    }

                }
                if(isF){
                    sb.insert(0,"-");
                }
                if (isReal && !isError) {
                    tokenList.add(new Token(27, sb.toString(), lineNo));
                } else if(!isReal && !isZero){
                    tokenList.add(new Token(26, sb.toString(), lineNo));
                } else if(isZero && "0".equals(sb.toString())){
                    tokenList.add(new Token(26, "0", lineNo));
                } else{
                    tokenList.add(new Token(-1, sb.toString()+"不是正确的数字", lineNo));
                }
                sb.delete(0, sb.length());
                continue;
            }
            //标识符,包括保留字和ID
            if ((currentChar >= 'a' && currentChar <= 'z') || currentChar == '_'
                    || (currentChar >= 'A' && currentChar <= 'Z')) {
                //继续读取
                while ((currentChar >= 'a' && currentChar <= 'z')
                        || (currentChar >= 'A' && currentChar <= 'Z')
                        || currentChar == '_'
                        || (currentChar >= '0' && currentChar <= '9')) {
                    sb.append(currentChar);
                    readChar();
                }
                //识别保留字
                String sbString = sb.toString();
                if (sbString.equals("if")) {
                    tokenList.add(new Token(1, lineNo));
                } else if (sbString.equals("else")) {
                    tokenList.add(new Token(2, lineNo));
                } else if (sbString.equals("while")) {
                    tokenList.add(new Token(3, lineNo));
                } else if (sbString.equals("int")) {
                    tokenList.add(new Token(4, lineNo));
                } else if (sbString.equals("real")) {
                    tokenList.add(new Token(5, lineNo));
                }else if(sbString.equals("print")){
                    tokenList.add(new Token(90, lineNo));
                }else if(sbString.equals("scan")){
                    tokenList.add(new Token(91, lineNo));
                }else if(sbString.equals("for")){
                    tokenList.add(new Token(97, lineNo));
                }else if(sbString.equals("continue")){
                    tokenList.add(new Token(98, lineNo));
                }else if(sbString.equals("break")){
                    tokenList.add(new Token(99, lineNo));
                }else if(sbString.charAt(sbString.length()-1)=='_'){
                    tokenList.add(new Token(-1, sbString+"命名错误,变量名不能以_结尾",lineNo));
                }
                else {
                    tokenList.add(new Token(25, sbString,lineNo));
                }
                sb.delete(0, sb.length());
                continue;
            }
            tokenList.add(new Token(-1, "未识别标识符"+currentChar,lineNo));
            readChar();
        }
        return tokenList;
    }
    public static String output(String fileContent){
        currentLineNo=-1;
        LinkedList<Token> tokenList =lexicalAnalyse(fileContent);
        StringBuilder sbOutput=new StringBuilder();
        for (Token token:tokenList) {
            if(token.getLineNo()!=currentLineNo){
                currentLineNo = token.getLineNo();
                sbOutput.append("\n第"+currentLineNo+"行:");
            }
            sbOutput.append(token);
        }
        return sbOutput.toString();
    }
}
