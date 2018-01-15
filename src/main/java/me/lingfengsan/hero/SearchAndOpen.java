package me.lingfengsan.hero;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by 618 on 2018/1/8.
 *
 * @author lingfengsan
 */
public class SearchAndOpen implements Callable {
    private final Question question;

    SearchAndOpen(Question question) {
        this.question = question;
    }

    private void open(String url) {
        //获取操作系统的名字
        String osName = System.getProperty("os.name", "");
        if (osName.startsWith("Windows")) {
            //windows的打开方式。
            try {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //判断当前系统是否支持Java AWT Desktop扩展
            if (java.awt.Desktop.isDesktopSupported()) {
                try {
                    //创建一个URI实例,注意不是URL
                    java.net.URI uri = java.net.URI.create(url);
                    //获取当前系统桌面扩展
                    java.awt.Desktop dp = java.awt.Desktop.getDesktop();
                    //判断系统桌面是否支持要执行的功能
                    if (dp.isSupported(java.awt.Desktop.Action.BROWSE)) {
                        //获取系统默认浏览器打开链接
                        dp.browse(uri);
                    }
                } catch (java.lang.NullPointerException e) {
                    //此为uri为空时抛出异常
                } catch (java.io.IOException e) {
                    //此为无法获取系统默认浏览器
                }
            }
        }
    }

    private Long searchAndOpen(Question question) throws IOException {
        String queryText1 = question.getQuestionText();
        String queryText2 = question.getQuestionText();
        List<Question.Option> options = question.getOptions();
        for(Question.Option option : options) {
            queryText2 = queryText2 + " " + option.getOptionText();
        }

        String url1 = null;
        String url2 = null;
        try {
            url1 = "http://www.baidu.com/s?tn=ichuner&lm=-1&word=" +
                    URLEncoder.encode(queryText2, "gb2312") + "&rn=20";
            url2 = "https://zhidao.baidu.com/search?lm=0&rn=10&pn=0&fr=search&ie=gbk&word=" +
                    URLEncoder.encode(queryText1, "gb2312");

            open(url1);
//            open(url2);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Long.valueOf(1);
    }

    @Override
    public Long call() throws Exception {
        return searchAndOpen(question);
    }
}
