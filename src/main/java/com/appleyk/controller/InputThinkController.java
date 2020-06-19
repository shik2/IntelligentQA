package com.appleyk.controller;

import com.appleyk.domain.Question;
import com.appleyk.service.InputThinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InputThinkController {
    @Autowired
    InputThinkService inputThinkService;

    @RequestMapping(value = "/getQuestions")
    public String query() {
        return inputThinkService.getQuestions();
    }

    @RequestMapping(value = "/saveQuestion")
    public void save(@RequestParam("query") String query) {
        Question question = new Question();
        question.setQuery(query);
        inputThinkService.saveQuestion(question);
    }
}
