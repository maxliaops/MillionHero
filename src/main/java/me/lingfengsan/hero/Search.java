package me.lingfengsan.hero;

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
import java.util.Map;
import java.util.concurrent.Callable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import org.wltea.analyzer.lucene.IKAnalyzer;

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
        String url = "https://zhidao.baidu.com/search?lm=0&rn=10&pn=0&fr=search&ie=gbk&word=" +
                URLEncoder.encode(question.getQuestionText(), "gb2312");
        Document doc = Jsoup.parse(new URL(url).openStream(), "gb2312", url);
        String result = doc.text();
//        String result = getUTF8BytesFromGBKString(temp);
        System.out.println(result);
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

    private static void printAnalysisResult(Analyzer analyzer, String keyWord)
            throws Exception {
//        System.out.println("[" + keyWord + "]分词效果如下");
        TokenStream tokenStream = analyzer.tokenStream("content",
                new StringReader(keyWord));
        tokenStream.addAttribute(CharTermAttribute.class);
        while (tokenStream.incrementToken()) {
            CharTermAttribute charTermAttribute = tokenStream
                    .getAttribute(CharTermAttribute.class);
            System.out.println(charTermAttribute.toString());

        }
    }
    //有损转换
    public String getUTF8BytesFromGBKString(String gbkStr) throws UnsupportedEncodingException {
        int n = gbkStr.length();
        byte[] utfBytes = new byte[3 * n];
        int k = 0;
        for (int i = 0; i < n; i++) {
            int m = gbkStr.charAt(i);
            if (m < 128 && m >= 0) {
                utfBytes[k++] = (byte) m;
                continue;
            }
            utfBytes[k++] = (byte) (0xe0 | (m >> 12));
            utfBytes[k++] = (byte) (0x80 | ((m >> 6) & 0x3f));
            utfBytes[k++] = (byte) (0x80 | (m & 0x3f));
        }
        if (k < utfBytes.length) {
            byte[] tmp = new byte[k];
            System.arraycopy(utfBytes, 0, tmp, 0, k);
            utfBytes = tmp;


        }
        return new String(utfBytes,"UTF-8");
    }
    @Override
    public Long call() throws Exception {
        return search(question);
    }
}
