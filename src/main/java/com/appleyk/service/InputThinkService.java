package com.appleyk.service;

import com.appleyk.domain.Question;

public interface InputThinkService {
    String getQuestions();
    void saveQuestion(Question question);
}
