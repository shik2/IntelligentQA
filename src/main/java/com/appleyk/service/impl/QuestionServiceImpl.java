package com.appleyk.service.impl;

import com.appleyk.configuration.PropertiesConfig;
import com.appleyk.process.ModelProcess;
import com.appleyk.repository.QuestionRepository;
import com.appleyk.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Primary
public class QuestionServiceImpl implements QuestionService {

    //注入变量不能使用static
    @Value("${aaa}")
    private String aaa;

    @Value("${rootDirPath}")
    private String rootDictPath;

    @Value("${HanLP.CustomDictionary.path.communityDict}")
    private String communityDictPath;


    @Autowired
    private PropertiesConfig pcg;

    @Autowired
    private QuestionRepository questionRepository;

    public QuestionServiceImpl() throws Exception {
    }

    @Override
    public void showDictPath() {
        System.out.println("HanLP分词字典及自定义问题模板根目录：" + rootDictPath);
    }

    /**
     * 初始化模型输入：问题类别、特征向量、nb模型
     * 原先是写在answer里面
     *
     * @param rootDirPath
     * @throws Exception
     */
    private ModelProcess queryProcess = new ModelProcess("D:/HanLP/data");

    /**
     * 多轮对话的卡槽部分
     */
    private String memory_community = "";
    private String last_question = "";

    @Override
    public String answer(String question) throws Exception {

        System.out.println(pcg.aaa);


        ArrayList<String> reStrings = queryProcess.analyQuery(question);
        int modelIndex = Integer.valueOf(reStrings.get(0));
        String answer = null;
        String community = "";
        String title = "";
        String name = "";
        String type = "";
        Double score = 0.0;

        /**
         * 匹配问题模板
         */
        switch (modelIndex) {
            /**
             * community 地址
             */
            case 0:
                community = reStrings.get(1);
                String address;
                if (!"community".equals(community)) {
                    memory_community = community;
                    address = questionRepository.getAddress(community);
                } else {
                    System.out.println(community+"ssss");
                    address = questionRepository.getAddress(memory_community);
                }
                if (address == null) {
                    answer = null;
                } else {
                    answer = address.replace("[", "").replace("]", "");
                }

                break;
            case 1:
                /**
                 * community 医院
                 */
                community = reStrings.get(1);
                List<String> hospitals;
                if (!"community".equals(community)) {
                    memory_community = community;
                    hospitals = questionRepository.getHospital(community);
                } else {
                    hospitals = questionRepository.getHospital(memory_community);
                }
                if (hospitals.size() == 0) {
                    answer = null;
                } else {
                    answer = hospitals.toString().replace("[", "").replace("]", "");
                }
                break;
            case 2:
                /**
                 * community 学校
                 */
                community = reStrings.get(1);
                List<String> uschools;
                List<String> pschools;
                List<String> nschools;
                if (!"community".equals(community)) {
                    memory_community = community;
                    uschools = questionRepository.getUschool(community);
                    pschools = questionRepository.getPschool(community);
                    nschools = questionRepository.getNschool(community);
                } else {
                    uschools = questionRepository.getUschool(memory_community);
                    pschools = questionRepository.getPschool(memory_community);
                    nschools = questionRepository.getNschool(memory_community);
                }
                if (uschools.size() == 0 && pschools.size() == 0 && nschools.size() == 0) {
                    answer = null;
                } else {
                    answer = "";
                    if (uschools.size() != 0)
                        answer += "大学：" + uschools.toString().replace("[", "").replace("]", "") + "<br />";
                    if (pschools.size() != 0)
                        answer += "中小学：" + pschools.toString().replace("[", "").replace("]", "") + "<br />";
                    if (nschools.size() != 0)
                        answer += "幼儿园：" + nschools.toString().replace("[", "").replace("]", "");
                }
                break;
            case 3:
                /**
                 * community 交通
                 */
                community = reStrings.get(1);
                List<String> transports;
                if (!"community".equals(community)) {
                    memory_community = community;
                    transports = questionRepository.getTransport(community);
                } else {
                    transports = questionRepository.getTransport(memory_community);
                }
                if (transports.size() == 0) {
                    answer = null;
                } else {
                    answer = transports.toString().replace("[", "").replace("]", "");
                }
                break;
            case 4:
                /**
                 * again
                 */
                System.out.println("上一个问题" + last_question);
                answer = answer(last_question);
                break;
            case 5:
                /**
                 * community 商场
                 */
                community = reStrings.get(1);
                List<String> markets;
                if (!"community".equals(community)) {
                    memory_community = community;
                    markets = questionRepository.getMarkets(community);
                } else {
                    markets = questionRepository.getMarkets(memory_community);
                }
                if (markets.size() == 0) {
                    answer = null;
                } else {
                    answer = markets.toString().replace("[", "").replace("]", "");
                }
                break;

            case 6:
                /**
                 * community 商场
                 */
                community = reStrings.get(1);
                List<String> banks;
                if (!"community".equals(community)) {
                    memory_community = community;
                    banks = questionRepository.getBanks(community);
                } else {
                    banks = questionRepository.getBanks(memory_community);
                }
                if (banks.size() == 0) {
                    answer = null;
                } else {
                    answer = banks.toString().replace("[", "").replace("]", "");
                }
                break;

            case 7:
                /**
                 * community 建造年份
                 */
                community = reStrings.get(1);
                String bulit_day = "";
                if (!"community".equals(community)) {
                    memory_community = community;
                    bulit_day = questionRepository.getBuiltDay(community);
                } else {
                    bulit_day = questionRepository.getBuiltDay(memory_community);
                }
                if (bulit_day == "") {
                    answer = null;
                } else {
                    answer = bulit_day.replace("[", "").replace("]", "");
                }
                break;
            case 8:
                /**
                 * 1 2 3 4 nnt 参演评分 大于 x == 演员参演的电影评分大于x的有哪些
                 */
                name = reStrings.get(1);
                score = Double.parseDouble(reStrings.get(4));
                List<String> actorMoviesByScore = questionRepository.getActorMoviesByHScore(name, score);
                if (actorMoviesByScore.size() == 0) {
                    answer = null;
                } else {
                    answer = actorMoviesByScore.toString().replace("[", "").replace("]", "");
                }
                break;
            case 9:
                /**
                 * 1 2 3 4 nnt 参演评分 小于 x == 演员参演的电影评分小于x的有哪些
                 */
                name = reStrings.get(1);
                score = Double.parseDouble(reStrings.get(4));
                List<String> actorMoviesByLScore = questionRepository.getActorMoviesByLScore(name, score);
                if (actorMoviesByLScore.size() == 0) {
                    answer = null;
                } else {
                    answer = actorMoviesByLScore.toString().replace("[", "").replace("]", "");
                }

                break;
            case 10:
                /**
                 * nnt 电影类型 == 演员参演的电影类型有哪些
                 */
                name = reStrings.get(1);
                List<String> movieTypes = questionRepository.getActorMoviesType(name);
                if (movieTypes.size() == 0) {
                    answer = null;
                } else {
                    answer = movieTypes.toString().replace("[", "").replace("]", "");
                }
                break;
            case 11:
                /**
                 * 1 2 3 4 nnt nnr 合作 电影列表 == 演员A和演员B合作的电影有哪些
                 */
                name = reStrings.get(1);
                List<String> actorMoviesA = questionRepository.getActorMovies(name);
                /**
                 * 如果演员A的电影作品无，那么A和演员B无合作之谈
                 */
                if (actorMoviesA.size() == 0) {
                    answer = null;
                    break;
                }

                name = reStrings.get(2);
                List<String> actorMoviesB = questionRepository.getActorMovies(name);
                /**
                 * 如果演员B的电影作品无，那么B和演员A无合作之谈
                 */
                if (actorMoviesB.size() == 0) {
                    answer = null;
                    break;
                }

                /**
                 * A的作品与B的作品求交集
                 */
                actorMoviesA.retainAll(actorMoviesB);

                if (actorMoviesA.size() == 0) {
                    answer = null;
                } else {
                    answer = actorMoviesA.toString().replace("[", "").replace("]", "");
                }
                break;
            case 12:
                name = reStrings.get(1);
                Integer count = questionRepository.getMoviesCount(name);
                if (count == null) {
                    answer = null;
                } else {
                    answer = String.valueOf(count) + "部电影";
                }
                break;
            case 13:
                /**
                 * nnt 出生日期 == 演员出生日期
                 */
                name = reStrings.get(1);
                answer = questionRepository.getActorBirth(name);
                break;
            case 999:
                answer = "暂时无法回答该问题";
                break;
            default:
                break;
        }


        System.out.println(answer);
        List<String> sim_questions = queryProcess.getSimQuestion();
        if (sim_questions.size() != 0 && modelIndex != 4) {
            answer += "<br /><br />你可能感兴趣的问题：<br />";
            for (String sim_question : sim_questions) {
                if ("community".equals(community)) {
                    answer += sim_question.replace("community", memory_community) + "<br />";
                } else {
                    answer += sim_question.replace("community", community) + "<br />";
                }
            }
        }
        System.out.println("相似问题:" + queryProcess.getSimQuestion());
        if (answer != null && !answer.equals("") && !answer.equals("\\N")) {
            //将当前问题放入卡槽
            if (modelIndex != 4) {
                last_question = question;
            }
            return answer;
        } else {
            return "sorry,没有找到你要的答案";
        }

    }


}
