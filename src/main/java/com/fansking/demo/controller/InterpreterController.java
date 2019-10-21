package com.fansking.demo.controller;

import com.fansking.demo.service.*;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.fansking.demo.service.SyntaxService.syntacticAnalyse;

/**
 * @author fansking
 * @date 2019/10/14 16:10
 */
@RestController
@RequestMapping(value = "/service")
@CrossOrigin
public class InterpreterController {
    @PostMapping("/lexer")
    public ParamData getLexerRes(@RequestBody ParamData data){
        return new ParamData(LexicalService.output(data.getText()));
    }

    @PostMapping("/syntax")
    public ParamData getSyntax(@RequestBody ParamData data){

       String s= SyntaxService.output(syntacticAnalyse(LexicalService.lexicalAnalyse(data.getText())),1);
        return (new ParamData(SyntaxException.printExceptionList()+"\n"+ s));

    }


}


