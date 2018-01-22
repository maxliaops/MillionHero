package me.lingfengsan.hero;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Highlighter;

import me.lingfengsan.hero.highlight.Dialog;
import me.lingfengsan.hero.highlight.UnderlineHighlighter;
import me.lingfengsan.hero.highlight.WordSearcher;
import me.lingfengsan.hero.keyword.Keyword;
import me.lingfengsan.hero.keyword.KeywordGetter;
import me.lingfengsan.hero.keyword.KeywordsApi;
import me.lingfengsan.hero.keyword.KeywordsResponse;

import org.apdplat.word.WordSegmenter;
import org.apdplat.word.segmentation.Word;
import org.apdplat.word.tagging.PartOfSpeechTagging;
import org.fusesource.jansi.AnsiConsole;

import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;

/**
 * Created by 618 on 2018/1/8.
 *
 * @author lingfengsan
 */
public class Main {
    private static final int SIZE = 18;
    private static final String FONT = "Dialog";

    public static final boolean Debug = false;
    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private int mode;
    private List<Dialog> dialogs;

    public Main(int mode) {
        long startTime = System.currentTimeMillis();
        long execTime;
        this.mode = mode;
        if (mode == 3) {
            dialogs = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                Dialog dialog = new Dialog(i);
                dialogs.add(dialog);
            }
        }
        if (mode == 2 || mode == 3) {
            List<Word> words = WordSegmenter.seg("杨尚川是APDPlat应用级产品开发平台的作者");
            PartOfSpeechTagging.process(words);
        }
        execTime = System.currentTimeMillis() - startTime;
        System.out.println();
        System.out.println("初始化耗时: " + execTime + "毫秒");
    }

    public static void main(String[] args) throws IOException {
        String deviceId = null;
        int mode = 2;
        if (args != null && args.length == 1) {
            mode = Integer.parseInt(args[0]);
        }
        System.out.println("---------------------------------------------------");
        System.out.println("开始执行");
        BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));

        Main main = new Main(mode);
        AnsiConsole.systemInstall();
        while (true) {
            if (Debug) {
                String str = bf.readLine();
                System.out.println("开始执行");
                try {
                    if (str.length() == 0) {
                        main.run();
                    }
                } catch (Exception e) {
                    System.out.println("error");
                }
            } else {
                try {
                    if (mode == 3) {
                        main.run3(deviceId);
                    } else {
                        main.run2(deviceId);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("error");
                }
            }
        }
    }

    private void checkQuestion(Question question) {
        String questionText = question.getQuestionText();
        if (questionText == null) return;
        System.out.println();
        String text = "";
        if (questionText.contains("不")) {
            text = "--------发现否定词： 不";
        } else if (questionText.contains("没有")) {
            text = "--------发现否定词： 没有";
        } else if (questionText.contains("未")) {
            text = "--------发现否定词： 未";
        }
        System.out.println(ansi().fg(WHITE).a(text).reset());
        System.out.println();
    }

    private void run3(String deviceId) throws InterruptedException,
            UnsupportedEncodingException {
        InformationGetter informationGetter = new InformationGetter(deviceId);
        Question question = informationGetter.getQuestionAndAnswers();
        System.out.println(question.getQuestionId() + ". " + question.getQuestionText());
        List<Question.Option> options = question.getOptions();
        for (Question.Option option : options) {
            System.out.println(option.getOptionText());
        }
        checkQuestion(question);

        long startTime = System.currentTimeMillis();
        long execTime;

        Search4 search;
        FutureTask<String> futureSearchTask;
        for (int i = 0; i < 3; i++) {
            Dialog dialog = dialogs.get(i);
            search = new Search4(question, dialog);
            futureSearchTask = new FutureTask<String>(search);
            executorService.submit(futureSearchTask);
        }

        execTime = System.currentTimeMillis() - startTime;
        System.out.println("答题总耗时: " + execTime + "毫秒");
    }


    private void run2(String deviceId) throws InterruptedException {
        InformationGetter informationGetter = new InformationGetter(deviceId);
        Question question = informationGetter.getQuestionAndAnswers();
        System.out.println(question.getQuestionId() + ". " + question.getQuestionText());
        List<Question.Option> options = question.getOptions();
        for (Question.Option option : options) {
            System.out.println(option.getOptionText());
        }
        checkQuestion(question);

        long startTime = System.currentTimeMillis();
        long execTime;

        FutureTask<Long> futureQuestion = new FutureTask<Long>(new SearchAndOpen(question));
        executorService.submit(futureQuestion);

        Map<String, KeywordGetter> keywordGetterMap = new HashMap<>();
        Map<String, FutureTask<List<String>>> futureKeywordMap = new HashMap<>();
        String questionText = question.getQuestionText();
        int keywordMode = 1;
        if (mode == 0) {
            keywordMode = 0;
        } else if (mode == 2) {
            keywordMode = 2;
        } else {
            keywordMode = 1;
        }
        keywordGetterMap.put(questionText, new KeywordGetter(questionText, keywordMode));
        for (Question.Option option : options) {
            String optionText = option.getOptionText();
            keywordGetterMap.put(optionText, new KeywordGetter(optionText, keywordMode));
        }
        for (String text : keywordGetterMap.keySet()) {
            KeywordGetter keywordGetter = keywordGetterMap.get(text);
            FutureTask<List<String>> futureKeywordTask = new FutureTask<List<String>>
                    (keywordGetter);
            futureKeywordMap.put(text, futureKeywordTask);
            executorService.submit(futureKeywordTask);
        }


        Map<String, Search3> searchMap = new HashMap<>();
        Map<String, FutureTask<String>> futureSearchMap = new HashMap<>();
        try {
            String url = null;
            Search3 search = null;
            FutureTask<String> futureSearchTask = null;

//            url = String.format(Search3.URL_BAIDU_ZHIDAO, URLEncoder.encode(question
//                    .getQuestionText(), "gb2312"));
//            search = new Search3(url, "gb2312");
//            futureSearchTask = new FutureTask<String>(search);
//            searchMap.put(Search3.SEARCH_TYPE_HIGH, search);
//            futureSearchMap.put(Search3.SEARCH_TYPE_HIGH, futureSearchTask);
//            executorService.submit(futureSearchTask);

            url = String.format(Search3.URL_SOUGOU_HOME, URLEncoder.encode(question
                    .getQuestionText(), "utf-8"));
            search = new Search3(url, "utf-8");
            futureSearchTask = new FutureTask<String>(search);
            searchMap.put(Search3.SEARCH_TYPE_MIDDLE, search);
            futureSearchMap.put(Search3.SEARCH_TYPE_MIDDLE, futureSearchTask);
            executorService.submit(futureSearchTask);

//            for (Question.Option option : options) {
//                String optionText = option.getOptionText();
//                String searchText = questionText + " " + optionText;
//                url = String.format(Search3.URL_BAIDU_HOME, URLEncoder.encode(searchText,
//                        "gb2312"));
//                search = new Search3(url, "utf-8");
//                FutureTask<String> futureTask = new FutureTask<String>(search);
//                searchMap.put(optionText, search);
//                futureSearchMap.put(optionText, futureTask);
//                executorService.submit(futureTask);
//            }
        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
        }

//        FutureTask<Long> futureQuestion = new FutureTask<Long>(new SearchAndOpen(question));
//        executorService.submit(futureQuestion);

        for (String text : keywordGetterMap.keySet()) {
            List<Keyword> keywords = new ArrayList<>();
            FutureTask<List<String>> futureKeywordTask = futureKeywordMap.get(text);
            while (!futureKeywordTask.isDone()) {
            }
            try {
                List<String> keywordList = futureKeywordTask.get();

                if (keywordList == null) {
                    keywordList = new ArrayList<>();
                }
                for (int i = 0; i < keywordList.size(); i++) {
                    String keywordText = keywordList.get(i);
                    Keyword keyword = new Keyword();
                    keyword.setText(keywordText);
                    keywords.add(keyword);
                }
//                System.out.println(text);
                if (text.equals(questionText)) {
                    question.setKeywords(keywords);
                } else {
                    for (Question.Option option : options) {
                        String optionText = option.getOptionText();
                        if (text.equals(optionText)) {
                            keywords.add(new Keyword(optionText));
//                            System.out.println(text);
                            option.setKeywords(keywords);
                        }
                    }
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
                continue;
            }
        }
        filterKeywords(question);

        FutureTask<String> futureSearchTask;
        String result = null;

//        for (Question.Option option : options) {
//            String optionText = option.getOptionText();
//            futureSearchTask = futureSearchMap.get(optionText);
//            while (!futureSearchTask.isDone()) {
//            }
//            try {
//                result = futureSearchTask.get();
//            } catch (ExecutionException e) {
////                e.printStackTrace();
//            }
////            System.out.println(result);
//            for (Question.Option option1 : options) {
//                for (Keyword keyword : option1.getKeywords()) {
//                    int count = Search3.getCount(result, keyword.getText());
//                    keyword.setCount3(optionText, count);
//                }
//            }
//            printResultLow(question, optionText);
//        }
//        System.out.println("-------------------------------------------------");

        futureSearchTask = futureSearchMap.get(Search3.SEARCH_TYPE_MIDDLE);
        while (!futureSearchTask.isDone()) {
        }
        try {
            result = futureSearchTask.get();
        } catch (ExecutionException e) {
//            e.printStackTrace();
        }
        for (Question.Option option : options) {
            for (Keyword keyword : option.getKeywords()) {
                int count = Search3.getCount(result, keyword.getText());
                keyword.setCount2(count);
            }
        }
        printResultMiddle(question);

//        futureSearchTask = futureSearchMap.get(Search3.SEARCH_TYPE_HIGH);
//        while (!futureSearchTask.isDone()) {
//        }
//        result = null;
//        try {
//            result = futureSearchTask.get();
//        } catch (ExecutionException e) {
////            e.printStackTrace();
//        }
//        for (Question.Option option : options) {
//            for (Keyword keyword : option.getKeywords()) {
//                int count = Search3.getCount(result, keyword.getText());
//                keyword.setCount(count);
//            }
//        }
//        printResultHigh(question);

        execTime = System.currentTimeMillis() - startTime;
        System.out.println("答题总耗时: " + execTime + "毫秒");
    }

    private void run() throws InterruptedException {

        InformationGetter informationGetter = new InformationGetter(null);
        Question question = informationGetter.getQuestionAndAnswers();
        System.out.println(question.getQuestionId() + ". " + question.getQuestionText());
        List<Question.Option> options = question.getOptions();
        for (Question.Option option : options) {
            System.out.println(option.getOptionText());
        }
        checkQuestion(question);

        long startTime = System.currentTimeMillis();
        FutureTask<Long> futureQuestion = new FutureTask<Long>(new SearchAndOpen(question));
        executorService.submit(futureQuestion);

        getKeywordsByBaidu(question);
        long execTime = System.currentTimeMillis() - startTime;
        System.out.println("获取关键字耗时: " + execTime + "毫秒");

//        FutureTask<Long> futureQuestion1 = new FutureTask<Long>(new SearchAndOpen(question));
//        executorService.submit(futureQuestion1);

        FutureTask<Long> futureQuestion0 = new FutureTask<Long>(new Search(question));
        executorService.submit(futureQuestion0);

        FutureTask<Long> futureQuestion3 = new FutureTask<Long>(new Search(question, 3));
        executorService.submit(futureQuestion3);

        FutureTask<Long> futureQuestion2 = new FutureTask<Long>(new Search(question, 2));
        executorService.submit(futureQuestion2);

        while (!futureQuestion0.isDone()) {
        }
        printResultHigh(question);
        execTime = System.currentTimeMillis() - startTime;
//        System.out.println("->H耗时: " + execTime + "毫秒");

        while (!futureQuestion3.isDone()) {
        }
        printResultMiddle(question);
        execTime = System.currentTimeMillis() - startTime;
//        System.out.println("->M耗时: " + execTime + "毫秒");

        while (!futureQuestion2.isDone()) {
        }
        printResultLow(question);
        execTime = System.currentTimeMillis() - startTime;
//        System.out.println("->L耗时: " + execTime + "毫秒");
        System.out.println("答题总耗时: " + execTime + "毫秒");
    }

    public static ExecutorService getExecutorService() {
        return executorService;
    }

    private static void getKeywordsByBaidu(Question question) {
        long startTime = System.currentTimeMillis();
//        List<Keyword> keywords = KeywordsApi.getInstance().getKeywords(question.getQuestionText
// ());
        List<Keyword> keywords = null;
        long execTime = System.currentTimeMillis() - startTime;
//        System.out.println("耗时: " + execTime + "毫秒");
        question.setKeywords(keywords);
        List<Question.Option> options = question.getOptions();
        for (Question.Option option : options) {
            startTime = System.currentTimeMillis();
            keywords = KeywordsApi.getInstance().getKeywords(option.getOptionText());
            execTime = System.currentTimeMillis() - startTime;
            System.out.println("耗时: " + execTime + "毫秒");
            option.setKeywords(keywords);
        }
        filterKeywords(question);
    }

    private static void filterKeywords(Question question) {
        List<Question.Option> options = question.getOptions();
        for (Question.Option option : options) {
            List<Keyword> keywords = option.getKeywords();
            for (Keyword keyword : keywords) {
                String keywordText = keyword.getText();
                if (isCommonKeyword(keywordText, question)) {
                    removeCommonKeyword(keywordText, question);
                }
            }
        }
    }

    private static void removeCommonKeyword(String keywordText, Question question) {
        List<Question.Option> options = question.getOptions();
        for (Question.Option option : options) {
            option.removeKeyword(keywordText);
        }
    }

    private static boolean isCommonKeyword(String keywordText, Question question) {
        int count = 0;
        List<Question.Option> options = question.getOptions();
        for (Question.Option option : options) {
            if (option.containsKeyword(keywordText)) {
                count++;
            }
        }
        if (count >= 3) {
            return true;
        } else {
            return false;
        }
    }

    private static void printResultHigh(Question question) {
        List<Question.Option> options = question.getOptions();
        if (options != null) {
            boolean allZeroFlag = true;
            for (int i = 0; i < options.size(); i++) {
                Question.Option option = options.get(i);

                List<Keyword> keywords = option.getKeywords();
                for (Keyword keyword : keywords) {
                    if (keyword.getCount() != 0) {
                        allZeroFlag = false;
                    }
                }
            }
            if (allZeroFlag) return;
            System.out.println("-----------------------------高");
            for (int i = 0; i < options.size(); i++) {
                Question.Option option = options.get(i);
                String optionFlag = null;
                switch (i) {
                    case 0:
                        optionFlag = "A";
                        break;
                    case 1:
                        optionFlag = "B";
                        break;
                    case 2:
                        optionFlag = "C";
                        break;
                    case 3:
                        optionFlag = "D";
                        break;
                }
                System.out.println(optionFlag + " ---------------");
                List<Keyword> keywords = option.getKeywords();
                for (Keyword keyword : keywords) {
                    if (keyword.getCount() != 0) {
                        System.out.printf("        " + "%3d" + " : " + keyword.getText() + "%n",
                                keyword.getCount());
                    }
                }
            }
            System.out.println("-----------------------------");
        }
    }

    private static void printResultMiddle(Question question) {
        List<Question.Option> options = question.getOptions();
        if (options != null) {
            boolean allZeroFlag = true;
            for (int i = 0; i < options.size(); i++) {
                Question.Option option = options.get(i);

                List<Keyword> keywords = option.getKeywords();
                for (Keyword keyword : keywords) {
                    if (keyword.getCount2() != 0) {
                        allZeroFlag = false;
                    }
                }
            }
            if (allZeroFlag) return;
            System.out.println("-----------------------------中");
            for (int i = 0; i < options.size(); i++) {
                Question.Option option = options.get(i);
                String optionFlag = null;
                switch (i) {
                    case 0:
                        optionFlag = "A";
                        break;
                    case 1:
                        optionFlag = "B";
                        break;
                    case 2:
                        optionFlag = "C";
                        break;
                    case 3:
                        optionFlag = "D";
                        break;
                }
                System.out.println(optionFlag + " ---------------");
                List<Keyword> keywords = option.getKeywords();
                for (Keyword keyword : keywords) {
                    if (keyword.getCount2() != 0) {
                        System.out.printf("        " + "%3d" + " : " + keyword.getText() + "%n",
                                keyword.getCount2());
                    }
                }
            }
            System.out.println("-----------------------------");
        }
    }

    private static void printResultLow(Question question, String optionText) {
        List<Question.Option> options = question.getOptions();
        if (options != null) {
            boolean allZeroFlag = true;
            for (int i = 0; i < options.size(); i++) {
                Question.Option option = options.get(i);

                List<Keyword> keywords = option.getKeywords();
                for (Keyword keyword : keywords) {
                    if (keyword.getCount3(optionText) != 0) {
                        allZeroFlag = false;
                    }
                }
            }
            if (allZeroFlag) return;
            System.out.println(optionText + "--------------");
            for (int i = 0; i < options.size(); i++) {
                Question.Option option = options.get(i);
                String optionFlag = null;
                switch (i) {
                    case 0:
                        optionFlag = "A";
                        break;
                    case 1:
                        optionFlag = "B";
                        break;
                    case 2:
                        optionFlag = "C";
                        break;
                    case 3:
                        optionFlag = "D";
                        break;
                }
                System.out.println(optionFlag + " ---------------");
//                if(optionText.equals(option.getOptionText())) {
//                    continue;
//                }
                List<Keyword> keywords = option.getKeywords();
                for (Keyword keyword : keywords) {
                    if (keyword.getCount3(optionText) != 0) {
                        System.out.printf("        " + "%3d" + " : " + keyword.getText() + "%n",
                                keyword.getCount3(optionText));
                    }
                }
            }
            System.out.println("-----------------------------");
        }
    }

    private static void printResultLow(Question question) {
        List<Question.Option> options = question.getOptions();
        if (options != null) {
            System.out.println("-----------------------------低");
            for (int i = 0; i < options.size(); i++) {
                Question.Option option = options.get(i);
                if (option.getCount2() != 0) {
                    System.out.printf("        %3d" + " : " + option.getOptionText() + "%n", option
                            .getCount2());
                }
            }
            System.out.println("-----------------------------");
        }
    }

}
