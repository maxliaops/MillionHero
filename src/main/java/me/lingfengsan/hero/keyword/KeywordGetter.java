package me.lingfengsan.hero.keyword;

import java.util.concurrent.Callable;

/**
 * Created by maxliaops on 18-1-17.
 */

public class KeywordGetter implements Callable {
    private String text;

    public KeywordGetter(String text) {
        this.text = text;
    }

    private KeywordsResponse getKeywords(String text) {
        long startTime = System.currentTimeMillis();
        KeywordsResponse keywordsResponse =  KeywordsApi.getInstance().getKeywords2(text);
        long execTime = System.currentTimeMillis() - startTime;
//        System.out.println("耗时: " + execTime + "毫秒");
        return keywordsResponse;
    }

    @Override
    public KeywordsResponse call() throws Exception {
        return getKeywords(text);
    }
}
