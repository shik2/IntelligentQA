package com.appleyk.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import com.hankcs.hanlp.dictionary.CustomDictionary;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.NaiveBayes;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import org.springframework.beans.factory.annotation.Value;

/**
 * Spark贝叶斯分类器 + HanLP分词器 + 实现问题语句的抽象+模板匹配+关键性语句还原
 *
 * @blob http://blog.csdn.net/appleyk
 * @date 2018年5月9日-上午10:07:52
 */
public class ModelProcess {
    private String communityDictPath = "D:/HanLP/data/dictionary/custom/community.txt";

    /**
     * 可能问题列表
     */
    List<Integer> similar;

    /**
     * 分类标签号和问句模板对应表
     */
    Map<Double, String> questionsPattern;

    /**
     * Spark贝叶斯分类器
     */
    NaiveBayesModel nbModel;

    /**
     * 词语和下标的对应表   == 词汇表
     */
    Map<String, Integer> vocabulary;

    /**
     * 关键字与其词性的map键值对集合 == 句子抽象
     */
    Map<String, String> abstractMap;

    /**
     * 指定问题question及字典的txt模板所在的根目录
     */
    String rootDirPath = "D:/HanLP/data";

    /**
     * 分类模板索引
     */
    int modelIndex = 0;

    public List<Integer> getSimilar() {
        return similar;
    }

    public ModelProcess() throws Exception {
        questionsPattern = loadQuestionsPattern();
        vocabulary = loadVocabulary();
        nbModel = loadClassifierModel();
        /**
         * 加载自定义的小区字典 == 设置词性 nm 0
         */
        loadCustomerDict(communityDictPath);
    }

    /**
     * 初始化模型输入：问题类别、特征向量、nb模型
     *
     * @param rootDirPath
     * @throws Exception
     */
    public ModelProcess(String rootDirPath) throws Exception {
        this.rootDirPath = rootDirPath + '/';
        questionsPattern = loadQuestionsPattern();
        vocabulary = loadVocabulary();
        nbModel = loadClassifierModel();
        /**
         * 加载自定义的小区字典 == 设置词性 nm 0
         */
        loadCustomerDict(communityDictPath);
        System.out.println("========模型初始化成功========");
    }

    public ArrayList<String> analyQuery(String queryString) throws Exception {

        /**
         * 打印问句
         */
        System.out.println("原始句子：" + queryString);
        System.out.println("========HanLP开始分词========");

        /**
         * 抽象句子，利用HanPL分词，将关键字进行词性抽象
         */
        String abstr = queryAbstract(queryString);
        System.out.println("句子抽象化结果：" + abstr);// nm 的 导演 是 谁

        /**
         * 将抽象的句子与spark训练集中的模板进行匹配，拿到句子对应的模板
         */
        String strPatt = queryClassify(abstr);
        System.out.println("句子套用模板结果：" + strPatt); // nm 制作 导演列表


        /**
         * 模板还原成句子，此时问题已转换为我们熟悉的操作
         */
        String finalPattern = queryExtenstion(strPatt);
        System.out.println("原始句子替换成系统可识别的结果：" + finalPattern);// 但丁密码 制作 导演列表


        ArrayList<String> resultList = new ArrayList<String>();
        resultList.add(String.valueOf(modelIndex));
        String[] finalPattArray = finalPattern.split(" ");
        for (String word : finalPattArray)
            resultList.add(word);
        return resultList;
    }

    public String queryAbstract(String querySentence) {

        // 句子抽象化
        Segment segment = HanLP.newSegment().enableCustomDictionary(true);
        List<Term> terms = segment.seg(querySentence);
        String abstractQuery = "";
        abstractMap = new HashMap<String, String>();
        int nrCount = 0; //nr 人名词性这个 词语出现的频率
        for (Term term : terms) {
            String word = term.word;
            String termStr = term.toString();
            System.out.println(termStr);
            if (termStr.contains("community")) {        //nm 电影名
                abstractQuery += "community ";
                abstractMap.put("community", word);
            } else if (termStr.contains("nr") && nrCount == 0) { //nr 人名
                abstractQuery += "nnt ";
                abstractMap.put("nnt", word);
                nrCount++;
            } else {
                abstractQuery += word + " ";
            }
        }
        System.out.println("========HanLP分词结束========");
        return abstractQuery;
    }

    public String queryExtenstion(String queryPattern) {
        // 句子还原
        Set<String> set = abstractMap.keySet();
        for (String key : set) {
            /**
             * 如果句子模板中含有抽象的词性
             */
            if (queryPattern.contains(key)) {

                /**
                 * 则替换抽象词性为具体的值
                 */
                String value = abstractMap.get(key);
                queryPattern = queryPattern.replace(key, value);
            }
        }
        String extendedQuery = queryPattern;
        /**
         * 当前句子处理完，抽象map清空释放空间并置空，等待下一个句子的处理
         */
        abstractMap.clear();
        abstractMap = null;
        return extendedQuery;
    }


    /**
     * 加载词汇表 == 关键特征 == 与HanLP分词后的单词进行匹配
     *
     * @return
     */
    public Map<String, Integer> loadVocabulary() {
        Map<String, Integer> vocabulary = new HashMap<String, Integer>();
        File file = new File(rootDirPath + "question2/vocabulary.txt");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line;
        try {
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(":");
                int index = Integer.parseInt(tokens[0]);
                String word = tokens[1];
                vocabulary.put(word, index);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return vocabulary;
    }

    /**
     * 加载文件，并读取内容返回
     *
     * @param filename
     * @return
     * @throws IOException
     */
    public String loadFile(String filename) throws IOException {
        File file = new File(rootDirPath + filename);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String content = "";
        String line;
        while ((line = br.readLine()) != null) {
            /**
             * 文本的换行符暂定用"`"代替
             */
            content += line + "`";
        }
        /**
         * 关闭资源
         */
        br.close();
        return content;
    }

    /**
     * 句子分词后与词汇表进行key匹配转换为double向量数组
     *
     * @param sentence
     * @return
     * @throws Exception
     */
    public double[] sentenceToArrays(String sentence) throws Exception {

        double[] vector = new double[vocabulary.size()];
        /**
         * 模板对照词汇表的大小进行初始化，全部为0.0
         */
        for (int i = 0; i < vocabulary.size(); i++) {
            vector[i] = 0;
        }

        /**
         * HanLP分词，拿分词的结果和词汇表里面的关键特征进行匹配
         */
        Segment segment = HanLP.newSegment();
        List<Term> terms = segment.seg(sentence);
//        System.out.println(terms.toString());
        for (Term term : terms) {
            String word = term.word;
            /**
             * 如果命中，0.0 改为 1.0
             */
            if (vocabulary.containsKey(word)) {
                System.out.println("匹配到了：" + word);
                int index = vocabulary.get(word);
                vector[index] = 1;
            }
        }

        return vector;
    }

    /**
     * Spark朴素贝叶斯(naiveBayes)
     * 对特定的模板进行加载并分类
     * 欲了解Spark朴素贝叶斯，可参考地址：https://blog.csdn.net/appleyk/article/details/80348912
     *
     * @return
     * @throws Exception
     */
    public NaiveBayesModel loadClassifierModel() throws Exception {

        /**
         * 生成Spark对象
         * 一、Spark程序是通过SparkContext发布到Spark集群的
         * Spark程序的运行都是在SparkContext为核心的调度器的指挥下进行的
         * Spark程序的结束是以SparkContext结束作为结束
         * JavaSparkContext对象用来创建Spark的核心RDD的
         * 注意：第一个RDD,一定是由SparkContext来创建的
         *
         * 二、SparkContext的主构造器参数为 SparkConf
         * SparkConf必须设置appname和master，否则会报错
         * spark.master   用于设置部署模式
         * local[*] == 本地运行模式[也可以是集群的形式]，如果需要多个线程执行，可以设置为local[2],表示2个线程 ，*表示多个
         * spark.app.name 用于指定应用的程序名称  ==
         */

        /**
         * 题外话
         * 贝叶斯是谁？
         * 贝叶斯(约1701-1763) Thomas Bayes，英国数学家。
         * 1702年出生于伦敦，做过神甫。
         * 1742年成为英国皇家学会会员。
         * 1763年4月7日逝世。
         * 贝叶斯在数学方面主要研究概率论 == 贝叶斯公式是概率论中较为重要的公式
         */
        SparkConf conf = new SparkConf().setAppName("NaiveBayesTest").setMaster("local[*]");
        JavaSparkContext sc = new JavaSparkContext(conf);

        /**
         * 训练集生成
         * labeled point 是一个局部向量，要么是密集型的要么是稀疏型的
         * 用一个label/response进行关联。在MLlib里，labeled points 被用来监督学习算法
         * 我们使用一个double数来存储一个label，因此我们能够使用labeled points进行回归和分类
         */
        List<LabeledPoint> train_list = new LinkedList<LabeledPoint>();
        String[] sentences = null;


        /**
         * 小区的地址是什么
         */
        String community_adress = loadFile("question2/【0】地址.txt");
        sentences = community_adress.split("`");
        for (String sentence : sentences) {
            double[] array = sentenceToArrays(sentence);
            LabeledPoint train_one = new LabeledPoint(0.0, Vectors.dense(array));
            train_list.add(train_one);
        }

        /**
         * 小区附近医院
         */
        String community_hospital = loadFile("question2/【1】医院.txt");
        sentences = community_hospital.split("`");
        for (String sentence : sentences) {
            double[] array = sentenceToArrays(sentence);
            LabeledPoint train_one = new LabeledPoint(1.0, Vectors.dense(array));
            train_list.add(train_one);
        }


        /**
         * 小区附近学校
         */
        String community_school = loadFile("question2/【2】学校.txt");
        sentences = community_school.split("`");
        for (String sentence : sentences) {
            double[] array = sentenceToArrays(sentence);
            LabeledPoint train_one = new LabeledPoint(2.0, Vectors.dense(array));
            train_list.add(train_one);
        }


        /**
         * 小区交通路线
         */
        String community_transport = loadFile("question2/【3】交通.txt");
        sentences = community_transport.split("`");
        for (String sentence : sentences) {
            double[] array = sentenceToArrays(sentence);
            LabeledPoint train_one = new LabeledPoint(3.0, Vectors.dense(array));
            train_list.add(train_one);
        }

        /**
         * 上一个问题
         */
        String again = loadFile("question2/【4】再一遍.txt");
        sentences = again.split("`");
        for (String sentence : sentences) {
            double[] array = sentenceToArrays(sentence);
            LabeledPoint train_one = new LabeledPoint(4.0, Vectors.dense(array));
            train_list.add(train_one);
        }


        /**
         * 商场
         */
        String market = loadFile("question2/【5】购物.txt");
        sentences = market.split("`");
        for (String sentence : sentences) {
            double[] array = sentenceToArrays(sentence);
            LabeledPoint train_one = new LabeledPoint(5.0, Vectors.dense(array));
            train_list.add(train_one);
        }


        /**
         * 银行
         */
        String bank = loadFile("question2/【6】银行.txt");
        sentences = bank.split("`");
        for (String sentence : sentences) {
            double[] array = sentenceToArrays(sentence);
            LabeledPoint train_one = new LabeledPoint(6.0, Vectors.dense(array));
            train_list.add(train_one);
        }

        /**
         * 建造日期
         */
        String built_day = loadFile("question2/【7】建造年份.txt");
        sentences = built_day.split("`");
        for (String sentence : sentences) {
            double[] array = sentenceToArrays(sentence);
            LabeledPoint train_one = new LabeledPoint(7.0, Vectors.dense(array));
            train_list.add(train_one);
        }

        /**
         * SPARK的核心是RDD(弹性分布式数据集)
         * Spark是Scala写的,JavaRDD就是Spark为Java写的一套API
         * JavaSparkContext sc = new JavaSparkContext(sparkConf);    //对应JavaRDD
         * SparkContext	    sc = new SparkContext(sparkConf)    ;    //对应RDD
         */
        JavaRDD<LabeledPoint> trainingRDD = sc.parallelize(train_list);
        NaiveBayesModel nb_model = NaiveBayes.train(trainingRDD.rdd());

        /**
         * 记得关闭资源
         */
        sc.close();

        /**
         * 返回贝叶斯分类器
         */
        return nb_model;

    }

    /**
     * 加载问题模板 == 分类器标签
     *
     * @return
     */
    public Map<Double, String> loadQuestionsPattern() {
        Map<Double, String> questionsPattern = new HashMap<Double, String>();
        File file = new File(rootDirPath + "question2/question_classification.txt");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        String line;
        try {
            while ((line = br.readLine()) != null) {
//				System.out.println(line);
                String[] tokens = line.split(":");
                double index = Double.valueOf(tokens[0]);
                String pattern = tokens[1];
                System.out.println("类别:" + index + "  模板:" + pattern);
                questionsPattern.put(index, pattern);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return questionsPattern;
    }

    /**
     * 贝叶斯分类器分类的结果，拿到匹配的分类标签号，并根据标签号返回问题的模板
     *
     * @param sentence
     * @return
     * @throws Exception
     */
    public String queryClassify(String sentence) throws Exception {

        double[] testArray = sentenceToArrays(sentence);
        System.out.println(Arrays.toString(testArray));
        Vector v = Vectors.dense(testArray);

        /**
         * 对数据进行预测predict
         * 句子模板在 spark贝叶斯分类器中的索引【位置】
         * 根据词汇使用的频率推断出句子对应哪一个模板
         */
        double index = nbModel.predict(v);
        modelIndex = (int) index;
        System.out.println("the model index is " + index);
        /**
         * 计算测试目标向量与训练样本数据集里面对应的各个分类标签匹配的概率结果
         */
        Vector acc = nbModel.predictProbabilities(v);
        System.out.println(acc);
        double[] accuracy = acc.toArray();
        similar = new ArrayList<>();
        for (int i = 0; i < accuracy.length; i++) {
            double distance = accuracy[modelIndex] - accuracy[i];
            if (distance < 0.2 && i != modelIndex) {
                System.out.println(i);
                similar.add(i);
            }

        }
        if (similar != null) {
            for (Integer integer : similar) {
                System.out.println(questionsPattern.get((double) integer));
            }
        }

        if (accuracy[modelIndex] < 0.2) {
            modelIndex = 999;
        }


        return questionsPattern.get(index);
    }

    public List<String> getSimQuestion() {
        List<String> sim_qs = new ArrayList<>();
        if (similar != null) {
            for (Integer integer : similar) {
                sim_qs.add(questionsPattern.get((double) integer));
            }
        }
        return sim_qs;
    }

    /**
     * 加载自定义电影字典
     *
     * @param path
     */
    public void loadCustomerDict(String path) {

        File file = new File(path);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            addCustomDictionary(br, 0);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

    }

    public void addCustomDictionary(BufferedReader br, int type) {

        String dict;
        try {
            while ((dict = br.readLine()) != null) {
                String[] dicts = dict.split(" ");
                String word = dicts[0];
                String property = dicts[1];
                //System.out.println(word+" "+property);
                property = property + " 0";
                CustomDictionary.add(word, property);
            }
            br.close();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] agrs) throws Exception {
        System.out.println("Hello World !");
    }
}
