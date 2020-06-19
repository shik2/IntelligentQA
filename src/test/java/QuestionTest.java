import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuestionTest {
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
    public void getQuestions() {
        String data = "";
        Map<String, Integer> vocabulary = null;
        String strPath = "E:\\question";
        List<File> fileList;
        fileList = getFileList(strPath);
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
                    String question = "\"" + line + "\"";
                    data = data+question+",";
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        data = data.substring(0,data.length()-1);

        data = "[" + data + "]";
        System.out.println(data);

    }
}
