package com.appleyk.dao;


import com.appleyk.domain.Question;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface QuestionMapper {
    List<Question> getQuestions();

    void saveQuestion(Question question);
}
