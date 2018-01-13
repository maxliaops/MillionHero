package me.lingfengsan.hero;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;


/**
 * Created by 618 on 2018/1/8.
 *
 * @author lingfengsan
 */
public class Search implements Callable {
    private final Question question;

    Search(Question question) {
        this.question = question;
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
        for(Question.Option option : options) {
            int count = getCount(result, option.getOptionText());
            System.out.println(option.getOptionText() + ": " + count);
        }
        long execTime = System.currentTimeMillis() - startTime;
        System.out.println("搜索时间：" + execTime + "ms");
        System.out.println("---------------我是分隔符--------------");
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

    @Override
    public Long call() throws Exception {
        return search(question);
    }
}
