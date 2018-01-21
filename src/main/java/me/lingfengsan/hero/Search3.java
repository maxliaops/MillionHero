package me.lingfengsan.hero;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

/**
 * Created by 618 on 2018/1/8.
 *
 * @author lingfengsan
 */
public class Search3 implements Callable {
    public final static String SEARCH_TYPE_HIGH = "High";
    public final static String SEARCH_TYPE_MIDDLE = "Middle";
    public final static String SEARCH_TYPE_LOW_A = "Low_A";
    public final static String SEARCH_TYPE_LOW_B = "Low_B";
    public final static String SEARCH_TYPE_LOW_C = "Low_C";

    public final static String URL_BAIDU_ZHIDAO = "https://zhidao.baidu" +
            ".com/search?lm=0&rn=10&pn=0&fr=search&ie=gbk&word=%s";
    public final static String URL_BAIDU_HOME = "http://www.baidu" +
            ".com/s?tn=ichuner&rn=20&lm=-1&word=%s";
    public final static String URL_SOUGOU_HOME = "https://www.sogou.com/web?query=%s&ie=utf8";

    private final String url;
    private final String charsetName;

    Search3(String url, String charsetName) {
        this.url = url;
        this.charsetName = charsetName;
    }

    String search(String url, String charsetName) throws IOException {
        long startTime = System.currentTimeMillis();
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
        httpURLConnection.setConnectTimeout(5000);
        httpURLConnection.setReadTimeout(5000);
        Document doc = Jsoup.parse(httpURLConnection.getInputStream(), charsetName, url);
        String result = doc.text();
        long execTime = System.currentTimeMillis() - startTime;
//        System.out.println("耗时: " + execTime + "毫秒");
        return result;
    }

    /*
     * 两个明确： 返回值类型：int 参数列表：两个字符串
     */
    public static int getCount(String maxString, String minString) {
        if(maxString == null) {
            return 0;
        }
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

    @Override
    public String call() throws Exception {
        return search(url, charsetName);
    }
}
