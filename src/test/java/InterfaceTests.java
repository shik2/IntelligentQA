import com.appleyk.Application;
import com.appleyk.dao.QuestionMapper;
import com.appleyk.domain.Question;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class InterfaceTests {

    @Autowired
    QuestionMapper questionMapper;

    @Test
    public void contextLoads() {
        List<Question> qsList = questionMapper.getQuestions();
        for (Question question : qsList) {
            System.out.println(question);
        }
    }

}
