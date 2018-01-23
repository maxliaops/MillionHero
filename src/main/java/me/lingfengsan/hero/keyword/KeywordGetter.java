package me.lingfengsan.hero.keyword;

import org.apdplat.word.WordSegmenter;
import org.apdplat.word.segmentation.Word;
import org.apdplat.word.tagging.PartOfSpeechTagging;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by maxliaops on 18-1-17.
 */

public class KeywordGetter implements Callable {
    public static final int MODE_BAIDU = 1;
    public static final int MODE_LOCAL = 2;

    private String text;
    private int mode;


    public KeywordGetter(String text) {
        this.text = text;
        this.mode = 0;
    }

    public KeywordGetter(String text, int mode) {
        this.text = text;
        this.mode = mode;
    }

    public static List<String> getKeywords(String text, int mode) {
        long startTime = System.currentTimeMillis();
        KeywordsResponse keywordsResponse = null;
        List<String> keywordList = null;
        if (mode == 0) {
            return null;
        } else if (mode == MODE_BAIDU) {
            keywordsResponse = KeywordsApi.getInstance().getKeywords2(text);
            if (keywordsResponse != null) {
                keywordList = keywordsResponse.getResult().getRes().getKeyword_list();
            }

        } else {
            List<Word> words = WordSegmenter.seg(text);
            PartOfSpeechTagging.process(words);
//            System.out.println(words);
            keywordList = new ArrayList<>();
            for (Word word : words) {
                keywordList.add(word.getText());
//                if (word.getPartOfSpeech().getPos().contains("n")) {
//                    keywordList.add(word.getText());
//                }
            }
        }
        long execTime = System.currentTimeMillis() - startTime;
        System.out.println("耗时: " + execTime + "毫秒");
        return keywordList;
    }

    @Override
    public List<String> call() throws Exception {
        return getKeywords(text, mode);
    }
}
