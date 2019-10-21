package com.fansking.demo.service;




import java.util.ArrayList;
import java.util.List;

/**
 * @author fansking
 * @date 2019/10/15 15:48
 */
public class SyntaxException {
    private static List<SyntaxException> exceptionList = new ArrayList<>();
    private String detail;
    private int lineNo;

    public SyntaxException(int lineNo,String detail) {
        this.detail = detail;
        this.lineNo = lineNo;
    }

    public SyntaxException() {
    }
    public static void addSyntaxException(int lineNo,String detail){
        exceptionList.add(new SyntaxException(lineNo,detail));
    }

    public static String printExceptionList(){
        String s ="";
        for(SyntaxException exception : exceptionList) {
            s+=("[SyntaxError]lineNo:" + exception.lineNo + "    " + exception.detail+"\n");
        }
        exceptionList.clear();
        return s;
    }
}
