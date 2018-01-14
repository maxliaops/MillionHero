package me.lingfengsan.hero.keyword;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by maxliaops on 18-1-14.
 */

public class KeywordsApi {
    private static KeywordsApi INSTANCE;
    private KeywordsService keywordsService;

    public static synchronized KeywordsApi getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new KeywordsApi();
        }
        return INSTANCE;
    }

    public KeywordsApi() {
        keywordsService = KeywordsService.Creator.newHireMeService();
    }

    public List<Keyword> getKeywords(String text) {
        List<Keyword> keywords = new ArrayList<>();
        Call<KeywordsResponse> call = keywordsService.getKeywords(text);
        try {
            Response<KeywordsResponse> response = call.execute();
            KeywordsResponse keywordsResponse = response.body();
            List<String> keywordList = keywordsResponse.getResult().getRes().getKeyword_list();

            if (keywordList == null) return keywords;
            for (int i = 0; i < keywordList.size(); i++) {
                String keywordText = keywordList.get(i);
                Keyword keyword = new Keyword();
                keyword.setText(keywordText);
                keywords.add(keyword);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return keywords;
    }
}
