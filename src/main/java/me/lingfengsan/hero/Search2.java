package me.lingfengsan.hero;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Callable;

/**
 * Created by 618 on 2018/1/8.
 *
 * @author lingfengsan
 */
public class Search2 implements Callable {
    private final Question question;
    private final Question.Option option;

    Search2(Question question, Question.Option option) {
        this.question = question;
        this.option = option;

    }

    Long search(Question question,  Question.Option option) throws IOException {
        String questionText = question.getQuestionText();
        String optionText = option.getOptionText();
        String url = "https://zhidao.baidu.com/search?lm=0&rn=10&pn=0&fr=search&ie=gbk&word=" +
                URLEncoder.encode(questionText + " " + optionText, "gb2312");
        Document doc = Jsoup.parse(new URL(url).openStream(), "gb2312", url);
        String result = doc.text();
//        System.out.println(result);
        String keyword = Search.getKeyword(optionText);
        int count = Search.getCount(result, keyword);
        if(count >= 1) {
            count--;
        }
        return Long.valueOf(count);
    }

    @Override
    public Long call() throws Exception {
        return search(question, option);
    }
}
