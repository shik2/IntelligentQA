package com.appleyk.service.impl;

import com.appleyk.dao.QuestionMapper;
import com.appleyk.domain.Question;
import com.appleyk.service.InputThinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
public class InputThinkServiceImpl implements InputThinkService {

    @Autowired
    QuestionMapper questionMapper;

    public String getQuestions() {
        String data = "";
        List<Question> questions = questionMapper.getQuestions();
        HashSet<String> question_set = new HashSet();
        //去除重复问题
        for (Question question : questions) {
            question_set.add(question.getQuery());
        }
        for (String q : question_set) {
            String question_tmp= "\"" + q + "\"";
            data = data + question_tmp + ",";
        }
       /* for (Question question : questions) {
            String question_tmp;
            question_tmp = "\"" + question.getQuery() + "\"";
            data = data + question_tmp + ",";
        }*/
        data = data.substring(0, data.length() - 1);
        data = "[" + data + "]";
        return data;

//        String data = "";
//        Map<String, Integer> vocabulary = null;
//        String strPath = "E:\\question";
//        List<File> fileList;
//        fileList = getFileList(strPath);
//        for (File file : fileList) {
//            BufferedReader br = null;
//            try {
//                br = new BufferedReader(new FileReader(file));
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            String line;
//            try {
//                while ((line = br.readLine()) != null) {
//                    String question = "\"" + line + "\"";
//                    data = data+question+",";
//                }
//            } catch (NumberFormatException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//        }
//        data = data.substring(0,data.length()-1);
//
//        data = "[" + data + "]";
//        return data;
    }

    /**
     *  从数据库中获取问题集，如果不存在该问题则加入该新问题
     */
    public void saveQuestion(Question question) {
        List<Question> questions = questionMapper.getQuestions();
        HashSet<String> question_set = new HashSet();
        //        //去除重复问题
        for (Question all_question : questions) {
            question_set.add(all_question.getQuery());
        }
        int add_flag = 1;
        for (String s : question_set) {
            if(s.equals(question.getQuery())){
                add_flag = 0;
                System.out.println("-----已经存在该问题-----");
                break;
            }
        }
        if(add_flag == 1){
            questionMapper.saveQuestion(question);
            System.out.println("-----添加了新问题:"+question.getQuery());
        }
    }

//    public List<File> getFileList(String strPath) {
//        File dir = new File(strPath);
//        List<File> filelist = new ArrayList<>();
//        File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组
//        if (files != null) {
//            for (int i = 0; i < files.length; i++) {
//                String fileName = files[i].getName();
//                if (files[i].isDirectory()) { // 判断是文件还是文件夹
//                    getFileList(files[i].getAbsolutePath()); // 获取文件绝对路径
//                } else {
//                    String strFileName = files[i].getAbsolutePath();
//                    System.out.println("---" + strFileName);
//                    filelist.add(files[i]);
//                }
//            }
//        }
//        return filelist;
//    }
}
