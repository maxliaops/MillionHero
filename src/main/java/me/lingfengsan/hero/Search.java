package me.lingfengsan.hero;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.lingfengsan.hero.keyword.Keyword;
import me.lingfengsan.hero.keyword.KeywordsApi;


/**
 * Created by 618 on 2018/1/8.
 *
 * @author lingfengsan
 */
public class Search implements Callable {

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
        String strPatter = "^\\u300a([\\s\\S]*)\\u300b$";
        Pattern pattern = Pattern.compile(strPatter);
        Matcher matcher = pattern.matcher(line);
        String strQuestionText;
        while (matcher.find()) {
            String str = matcher.group();
            strQuestionText = str.substring(1, str.length() - 1);
            return strQuestionText;
        }
        return line;
    }

    Long search(Question question) throws IOException {
        long startTime = System.currentTimeMillis();
        String url = "https://zhidao.baidu.com/search?lm=0&rn=10&pn=0&fr=search&ie=gbk&word=" +
                URLEncoder.encode(question.getQuestionText(), "gb2312");
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
        httpURLConnection.setConnectTimeout(1500);
        httpURLConnection.setReadTimeout(1500);
        Document doc = Jsoup.parse(httpURLConnection.getInputStream(), "gb2312", url);
        String result = doc.text();
//        System.out.println(result);
        List<Question.Option> options = question.getOptions();
        for (Question.Option option : options) {
            for (Keyword keyword : option.getKeywords()) {
                int count = getCount(result, keyword.getText());
                keyword.setCount(count);
            }
        }
        long execTime = System.currentTimeMillis() - startTime;
        System.out.println("H耗时: " + execTime + "毫秒");
        return Long.valueOf(1);
    }

    Long search3(Question question) throws IOException {
        long startTime = System.currentTimeMillis();
        String url = "http://www.baidu.com/s?tn=ichuner&lm=-1&word=" +
                URLEncoder.encode(question.getQuestionText(), "gb2312") + "&rn=50";
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
        httpURLConnection.setConnectTimeout(1500);
        httpURLConnection.setReadTimeout(1500);
        Document doc = Jsoup.parse(httpURLConnection.getInputStream(), "utf-8", url);
        String result = doc.text();
//        System.out.println(result);
        List<Question.Option> options = question.getOptions();
        for (Question.Option option : options) {
            for (Keyword keyword : option.getKeywords()) {
                int count = getCount(result, keyword.getText());
                keyword.setCount2(count);
            }
        }
        long execTime = System.currentTimeMillis() - startTime;
        System.out.println("M耗时: " + execTime + "毫秒");
        return Long.valueOf(1);
    }

    /*
     * 两个明确： 返回值类型：int 参数列表：两个字符串
     */
    public static int getCount(String maxString, String minString) {
        // 定义一个统计变量，初始化值是0
        int count = 0;
        int index;
        //先查，赋值，判断
        while ((index = maxString.indexOf(minString)) != -1) {
            count++;
            maxString = maxString.substring(index + minString.length());
        }

        return count;
    }

    Long search2(Question question) throws IOException {
        List<Question.Option> options = question.getOptions();
        int num_of_options = options.size();
        long startTime = System.currentTimeMillis();
        long[] countQA = new long[num_of_options];
        Search2[] searchQA = new Search2[num_of_options];
        FutureTask<Long>[] futureQA = new FutureTask[num_of_options];
        for (int i = 0; i < num_of_options; i++) {
            Question.Option option = options.get(i);
            searchQA[i] = new Search2(question, option);
            futureQA[i] = new FutureTask<Long>(searchQA[i]);
//            new Thread(futureQA[i]).start();
            Main.getExecutorService().submit(futureQA[i]);
        }

        try {
            for (int i = 0; i < num_of_options; i++) {
                while (!futureQA[i].isDone()) {
                }
                Question.Option option = options.get(i);
                countQA[i] = futureQA[i].get();
                option.setCount2((int) countQA[i]);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        long execTime = System.currentTimeMillis() - startTime;
        System.out.println("3L耗时: " + execTime + "毫秒");
        return Long.valueOf(1);
    }

    @Override
    public Long call() throws Exception {
        switch (type) {
            case 0:
                return search(question);
            case 2:
                return search2(question);
            case 3:
                return search3(question);
        }
        return Long.valueOf(1);
    }
}
