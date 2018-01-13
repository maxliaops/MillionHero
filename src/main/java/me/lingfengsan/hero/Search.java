package me.lingfengsan.hero;

import org.apdplat.word.WordSegmenter;
import org.apdplat.word.segmentation.Word;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by 618 on 2018/1/8.
 *
 * @author lingfengsan
 */
public class Search implements Callable {
//    private static final Logger LOGGER = LoggerFactory.getLogger(Search.class);
    private final Question question;
    int type;

    Search(Question question) {
        this.question = question;
        this.type = 0;
    }

    Search(Question question, int type) {
        this.question = question;
        this.type = type;
    }

    public static String getKeyword(String line) {
        //        System.out.println("getQuestionText: line=" + line);
        String strPatter = "^\\u300a([\\s\\S]*)\\u300b$";
        Pattern pattern = Pattern.compile(strPatter);
        Matcher matcher = pattern.matcher(line);
        String strQuestionText;
        while (matcher.find()) {
            String str = matcher.group();
            strQuestionText = str.substring(1, str.length() - 1);
//            System.out.println("getKeyword=" + strQuestionText + " " + line);
            return strQuestionText;
        }
        return line;
    }

    Long search(Question question) throws IOException {
        long startTime = System.currentTimeMillis();
        String url = "https://zhidao.baidu.com/search?lm=0&rn=10&pn=0&fr=search&ie=gbk&word=" +
                URLEncoder.encode(question.getQuestionText(), "gb2312");
        Document doc = Jsoup.parse(new URL(url).openStream(), "gb2312", url);
        String result = doc.text();
//        String result = getUTF8BytesFromGBKString(temp);
//        System.out.println(result);
        List<Question.Option> options = question.getOptions();
//        System.out.println("--------默认方法结果-------");
        System.out.println("-----------------------------高");
//        LOGGER.info("构造分词实现类：");
//        List<Word> words = WordSegmenter.seg("杨尚川是APDPlat应用级产品开发平台的作者");
//        for (Word word : words) {
//            System.out.println(word);
//        }
        int maxIndex = 0;
        boolean allZeroFlag = true;
        for (int i = 0; i < options.size(); i++) {
            Question.Option option = options.get(i);
            String keyword = getKeyword(option.getOptionText());
            int count = getCount(result, keyword);
            option.setCount(count);
            maxIndex = (count > options.get(maxIndex).getCount()) ? i : maxIndex;
            if (count != 0) {
                allZeroFlag = false;
            }
//            System.out.println("        " + option.getOptionText() + ": " + count);
        }
        for (int i = 0; i < options.size(); i++) {
            Question.Option option = options.get(i);
            if (i == maxIndex && !allZeroFlag) {
                System.out.printf("---->   " + "%3d" + " : " + option.getOptionText()
                        + "    <----" + "%n", option.getCount());
            } else {
                System.out.printf("        " + "%3d" + " : " + option.getOptionText() + "%n",
                        option.getCount());
            }
        }
        long execTime = System.currentTimeMillis() - startTime;
        System.out.println();
//        System.out.println("搜索时间：" + execTime + "ms");
        System.out.println("-----------------------------");
//        IKAnalyzer analyzer = new IKAnalyzer();
//        analyzer.setUseSmart(true);
//        String test = "IKAnalyzer的分词效果到底怎么样呢，我们来看一下吧";
//        try {
//            printAnalysisResult(analyzer, test);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return Long.valueOf(1);
    }

    Long search3(Question question) throws IOException {
        long startTime = System.currentTimeMillis();
        String url = "http://www.baidu.com/s?tn=ichuner&lm=-1&word=" +
                URLEncoder.encode(question.getQuestionText(), "gb2312") + "&rn=20";
        Document doc = Jsoup.parse(new URL(url).openStream(), "utf-8", url);
        String result = doc.text();
//        String result = getUTF8BytesFromGBKString(temp);
//        System.out.println(result);
        List<Question.Option> options = question.getOptions();
//        System.out.println("--------默认方法结果-------");
        System.out.println("-----------------------------中");
        int maxIndex = 0;
        for (int i = 0; i < options.size(); i++) {
            Question.Option option = options.get(i);
            String keyword = getKeyword(option.getOptionText());
            int count = getCount(result, keyword);
            option.setCount(count);
            maxIndex = (count > options.get(maxIndex).getCount()) ? i : maxIndex;
            System.out.printf("%3d" + " : " + option.getOptionText() + "%n", option.getCount());
//            System.out.println("        " + option.getOptionText() + ": " + count);
        }
//        for (int i = 0; i < options.size(); i++) {
//            Question.Option option = options.get(i);
//            if (i != maxIndex) {
//                System.out.println(option.getCount() + ": " + option.getOptionText());
//            } else {
//                System.out.println(option.getCount() + ": " + option.getOptionText());
//            }
//        }
        long execTime = System.currentTimeMillis() - startTime;
        System.out.println();
//        System.out.println("搜索时间：" + execTime + "ms");
        System.out.println("-----------------------------");
        return Long.valueOf(1);
    }

    /*
     * 两个明确： 返回值类型：int 参数列表：两个字符串
     */
    public static int getCount(String maxString, String minString) {
        // 定义一个统计变量，初始化值是0
        int count = 0;

        /*
        // 先在大串中查找一次小串第一次出现的位置
        int index = maxString.indexOf(minString);
        // 索引不是-1，说明存在，统计变量++
        while (index != -1) {
            count++;
            // 把刚才的索引+小串的长度作为开始位置截取上一次的大串，返回一个新的字符串，并把该字符串的值重新赋值给大串
            // int startIndex = index + minString.length();
            // maxString = maxString.substring(startIndex);
            maxString = maxString.substring(index + minString.length());
            // 继续查
            index = maxString.indexOf(minString);
        }
        */

        int index;
        //先查，赋值，判断
        while ((index = maxString.indexOf(minString)) != -1) {
            count++;
            maxString = maxString.substring(index + minString.length());
        }

        return count;
    }


    Long search1(Question question) throws IOException {
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        // 记录结束时间
        long endTime;
        List<Question.Option> options = question.getOptions();
        String questionText = question.getQuestionText();
        int NUM_OF_ANSWERS = options.size();
        //搜索
        long countQuestion = 1;
        long[] countQA = new long[NUM_OF_ANSWERS];
        long[] countAnswer = new long[NUM_OF_ANSWERS];

        int maxIndex = 0;
        int minIndex = 0;

        Search1[] searchQA = new Search1[NUM_OF_ANSWERS];
        Search1[] searchAnswers = new Search1[NUM_OF_ANSWERS];
        FutureTask<Long>[] futureQA = new FutureTask[NUM_OF_ANSWERS];
        FutureTask<Long>[] futureAnswers = new FutureTask[NUM_OF_ANSWERS];
        FutureTask<Long> futureQuestion = new FutureTask<Long>(new Search1(questionText));
        new Thread(futureQuestion).start();
//        System.out.println();
        for (int i = 0; i < NUM_OF_ANSWERS; i++) {
            String optionText = options.get(i).getOptionText();
//            System.out.println(i + " " + optionText);
            searchQA[i] = new Search1(questionText + " " + optionText);
            searchAnswers[i] = new Search1(optionText);
            futureQA[i] = new FutureTask<Long>(searchQA[i]);
            futureAnswers[i] = new FutureTask<Long>(searchAnswers[i]);
            new Thread(futureQA[i]).start();
            new Thread(futureAnswers[i]).start();
        }
        try {
            while (!futureQuestion.isDone()) {
            }
            countQuestion = futureQuestion.get();
            for (int i = 0; i < NUM_OF_ANSWERS; i++) {
                while (!futureQA[i].isDone()) {
                }
                countQA[i] = futureQA[i].get();
                while (!futureAnswers[i].isDone()) {
                }
                countAnswer[i] = futureAnswers[i].get();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        float[] ans = new float[NUM_OF_ANSWERS];
        for (int i = 0; i < NUM_OF_ANSWERS; i++) {
            ans[i] = (float) countQA[i] / (float) (countQuestion * countAnswer[i]);
            maxIndex = (ans[i] > ans[maxIndex]) ? i : maxIndex;
            minIndex = (ans[i] < ans[minIndex]) ? i : minIndex;
            String optionText = options.get(i).getOptionText();
//            System.out.print(optionText);
//            System.out.print(" countQA:" + countQA[i]);
//            System.out.print(" countAnswer:" + countAnswer[i]);
//            System.out.println(" ans:" + ans[i]);
        }
        //根据pmi值进行打印搜索结果
        int[] rank = rank(ans, NUM_OF_ANSWERS);
//        System.out.println("--------第一方法结果-------");
        for (int i : rank) {
            String optionText = options.get(i).getOptionText();
            System.out.print(optionText);
            System.out.print(" ans:" + ans[i]);
            System.out.print(" countQA:" + countQA[i]);
            System.out.println(" countAnswer:" + countAnswer[i]);
        }

//        System.out.println("--------方法一结果-------");
        System.out.println();
        System.out.println("最大相关：" + options.get(maxIndex).getOptionText());
        System.out.println("最小相关：" + options.get(minIndex).getOptionText());
        endTime = System.currentTimeMillis();
        long excTime = endTime - startTime;
        System.out.println();
        System.out.println("搜索时间：" + excTime + "ms");
        System.out.println("-----------------------------");
//        System.out.println("---------------我是分隔符--------------");
        return Long.valueOf(1);
    }

    /**
     * @param floats pmi值
     * @return 返回排序的rank
     */
    private static int[] rank(float[] floats, int num_of_answers) {
        int[] rank = new int[num_of_answers];
        float[] f = Arrays.copyOf(floats, num_of_answers);
        Arrays.sort(f);
        for (int i = 0; i < num_of_answers; i++) {
            for (int j = 0; j < num_of_answers; j++) {
                if (f[i] == floats[j]) {
                    rank[i] = j;
                }
            }
        }
        return rank;
    }


    Long search2(Question question) throws IOException {
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        // 记录结束时间
        long endTime;
        List<Question.Option> options = question.getOptions();
        int num_of_options = options.size();
        String questionText = question.getQuestionText();

        long[] countQA = new long[num_of_options];
        Search2[] searchQA = new Search2[num_of_options];
        FutureTask<Long>[] futureQA = new FutureTask[num_of_options];
        for (int i = 0; i < num_of_options; i++) {
            String optionText = options.get(i).getOptionText();
            searchQA[i] = new Search2(questionText, optionText);
            futureQA[i] = new FutureTask<Long>(searchQA[i]);
            new Thread(futureQA[i]).start();
        }

        try {
            for (int i = 0; i < num_of_options; i++) {
//                String optionText = options.get(i).getOptionText();
                while (!futureQA[i].isDone()) {
                }
                countQA[i] = futureQA[i].get();
//                System.out.println(optionText + ": " + countQA[i]);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

//        System.out.println("--------第二方法结果-------");
        System.out.println("-----------------------------低");
        for (int i = 0; i < num_of_options; i++) {
            String optionText = options.get(i).getOptionText();
//            System.out.println(countQA[i] + ": " + optionText);
            System.out.printf("%3d" + " : " + optionText + "%n", countQA[i]);
        }
        endTime = System.currentTimeMillis();
        long excTime = endTime - startTime;
        System.out.println();
//        System.out.println("执行时间：" + excTime + "ms");
        System.out.println("-----------------------------");
        return Long.valueOf(1);
    }

    @Override
    public Long call() throws Exception {
        switch (type) {
            case 0:
                return search(question);
            case 1:
                return search1(question);
            case 2:
                return search2(question);
            case 3:
                return search3(question);
        }
        return Long.valueOf(1);
    }
}
