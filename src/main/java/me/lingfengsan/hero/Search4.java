package me.lingfengsan.hero;

import org.apdplat.search.JSoupBaiduSearcher;
import org.apdplat.search.SearchResult;
import org.apdplat.search.Searcher;
import org.apdplat.search.Webpage;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.lingfengsan.hero.highlight.Dialog;
import me.lingfengsan.hero.keyword.KeywordGetter;

/**
 * Created by lenovo on 2018/1/21.
 */

public class Search4 implements Callable {
    Question question;
    Dialog dialog;

    public Search4(Question question, Dialog dialog) {
        this.question = question;
        this.dialog = dialog;
    }

    public String search(Question question, Dialog dialog) {
        int index = dialog.getIndex();
        List<Question.Option> options = question.getOptions();
        Question.Option targetOption = options.get(index);
        String optionText = targetOption.getOptionText();
        String keyword = optionText + " " + question.getQuestionText();
        String result = "";
        Searcher searcher = new JSoupBaiduSearcher();
        SearchResult searchResult = searcher.search(keyword, 1);
        List<Webpage> webpages = searchResult.getWebpages();
        if (webpages != null) {
            for (Webpage webpage : webpages) {
                result = result + "标题：" + webpage.getTitle() + "\n";
                result = result + "摘要：" + webpage.getSummary() + "\n\n";
            }
        }

        if (result != null && result.length() > 0) {
            result = filterResult(result);
            result = keyword + "\n" + result;
            dialog.getTextPane().setText(result);
            String optionKeyword = Search.getKeyword(optionText);
            for (Question.Option option : options) {
                String word = Search.getKeyword(option.getOptionText());
                if (optionKeyword.equals(word)) {
                    dialog.getSearcher().search(word, Color.YELLOW);
                } else {
                    dialog.getSearcher().search(word, Color.GREEN);
                }
            }
            List<String> questionKeywords = KeywordGetter.getKeywords(question.getQuestionText(),
                    2);
            for (String word : questionKeywords) {
                dialog.getSearcher().search(word, Color.CYAN);
            }
        }
        return result;
    }

    private String filterResult(String line) {
        String result;
        String strPatter = "更多关于([\\s\\S]*)的问题";
        Pattern pattern = Pattern.compile(strPatter);
        Matcher matcher = pattern.matcher(line);
        result = matcher.replaceAll("");
        return result;
    }

    @Override
    public String call() throws Exception {
        return search(question, dialog);
    }
}
