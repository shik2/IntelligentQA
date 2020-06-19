import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import org.junit.Test;

import java.io.*;
import java.util.*;

/**
 * 从所有问题训练样本中提取词汇，用作为特征向量
 */
public class FileTest {

    /**
     * 读取文件夹下的所有文件
     *
     * @param strPath
     * @return
     */
    public static List<File> getFileList(String strPath) {
        File dir = new File(strPath);
        List<File> filelist = new ArrayList<>();
        File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String fileName = files[i].getName();
                if (files[i].isDirectory()) { // 判断是文件还是文件夹
                    getFileList(files[i].getAbsolutePath()); // 获取文件绝对路径
                } else {
                    String strFileName = files[i].getAbsolutePath();
                    System.out.println("---" + strFileName);
                    filelist.add(files[i]);
                }
            }
        }
        return filelist;
    }

    @Test
    public void testFile() throws Exception {
        Map<String, Integer> vocabulary = null;
        String strPath = "E:\\question";
        List<File> fileList;
        fileList = getFileList(strPath);
        Set<String> tz = new TreeSet<>(); //保存特征词
        for (File file : fileList) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            String line;
            try {
                while ((line = br.readLine()) != null) {
                    try {
                        Segment segment = HanLP.newSegment();
                        segment.enableCustomDictionary(true);
                        /**
                         * 自定义分词+词性
                         */
                        List<Term> seg = segment.seg(line);
                        for (Term term : seg) {
                            // 去掉“的”和“有”
                            String nature = term.nature.toString();
                            if ("ude1".equals(nature) || "vyou".equals(nature) || "ry".equals(nature) ||
                                    "vshi".equals(nature) || "ule".equals(nature) || "y".equals(nature)) {
                                continue;
                            } else {
                                System.out.println(term.word + ":" + term.nature);
                                tz.add(term.word);
                            }
                        }
                    } catch (Exception ex) {
                        System.out.println(ex.getClass() + "," + ex.getMessage());
                    }
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        Iterator<String> value = tz.iterator();

        File file = new File("D:\\HanLP\\data\\question2\\vocabulary.txt");
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
        int count = 0;

        while (value.hasNext()) {
            String tzz = value.next();
            if(!tzz.equals("\r\n")){
                String w_tz = count + ":" + tzz;
                System.out.println(tzz);
                bw.write(w_tz + "\n");
                count++;
            }

        }
        bw.close();
        fw.close();
    }


}
