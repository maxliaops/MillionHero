package me.lingfengsan.hero;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.lingfengsan.hero.keyword.Keyword;

/**
 * Created by lenovo on 2018/1/11.
 */

public class Question {
    private String questionId;
    private String questionText;
    private List<Keyword> keywords;
    private List<Option> options;

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public List<Keyword> getKeywords() {
        return this.keywords;
    }

    public void setKeywords(List<Keyword> keywords) {
        this.keywords = keywords;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    public void addOption(Option option) {
        if (this.options == null) {
            this.options = new ArrayList<>();
        }
        this.options.add(option);
    }

    static class Option {
        private int optionId;
        private String optionText;
        private int count;
        private int count2;
        private Map<String, Keyword> keywordMap;

        public int getOptionId() {
            return optionId;
        }

        public void setOptionId(int optionId) {
            this.optionId = optionId;
        }

        public String getOptionText() {
            return optionText;
        }

        public void setOptionText(String optionText) {
            this.optionText = optionText;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getCount2() {
            return count2;
        }

        public void setCount2(int count2) {
            this.count2 = count2;
        }

        public List<Keyword> getKeywords() {
            List<Keyword> keywords = new ArrayList<>();
            ;
            for (String keywordText : keywordMap.keySet()) {
                keywords.add(keywordMap.get(keywordText));
            }
            return keywords;
        }

        public void setKeywords(List<Keyword> keywords) {
            keywordMap = new HashMap<>();
            for (Keyword keyword : keywords) {
                String text = Search.getKeyword(keyword.getText());
                keyword.setText(text);
                keywordMap.put(text, keyword);
            }
        }

        public boolean containsKeyword(String keywordText) {
            if (keywordMap.containsKey(keywordText)) {
                return true;
            } else {
                return false;
            }
        }

        public void removeKeyword(String keywordText) {
            if (keywordMap.containsKey(keywordText)) {
                keywordMap.remove(keywordText);
            }
        }
    }
}
