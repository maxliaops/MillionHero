package me.lingfengsan.hero;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;

/**
 * Created by 618 on 2018/1/8.
 *
 * @author lingfengsan
 */
public class Search3 implements Callable {
    private final String url;

    Search3(String url) {
        this.url = url;
    }

    String search(String url) throws IOException {
        long startTime = System.currentTimeMillis();
        Document doc = Jsoup.parse(new URL(url).openStream(), "utf-8", url);
        String result = doc.text();
        long execTime = System.currentTimeMillis() - startTime;
//        System.out.println("耗时: " + execTime + "毫秒");
        return result;
    }

    @Override
    public String call() throws Exception {
        return search(url);
    }
}
