package me.lingfengsan.hero;

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

import me.lingfengsan.hero.keyword.Keyword;
import me.lingfengsan.hero.keyword.KeywordGetter;
import me.lingfengsan.hero.keyword.KeywordsApi;
import me.lingfengsan.hero.keyword.KeywordsResponse;

/**
 * Created by 618 on 2018/1/8.
 *
 * @author lingfengsan
 */
public class Main {
    public static final boolean Debug = false;
    private static ExecutorService executorService = Executors.newCachedThreadPool();

    public static void main(String[] args) throws IOException {
        System.out.println("---------------------------------------------------");
        System.out.println("开始执行");
        BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));

        Main main = new Main();
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
                    main.run2();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("error");
                }
            }
        }
    }

    private void checkQuestion(Question question) {
        String questionText = question.getQuestionText();
        System.out.println();
        if (questionText.contains("不")) {
            System.out.println("--------发现否定词： 不");
        } else if (questionText.contains("没有")) {
            System.out.println("--------发现否定词： 没有");
        } else if (questionText.contains("喂")) {
            System.out.println("--------发现否定词： 喂");
        }
        System.out.println();
    }

    private void run2() throws InterruptedException {
        InformationGetter informationGetter = new InformationGetter();
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
        Map<String, FutureTask<KeywordsResponse>> futureKeywordMap = new HashMap<>();
        String questionText = question.getQuestionText();
        keywordGetterMap.put(questionText, new KeywordGetter(questionText));
        for (Question.Option option : options) {
            String optionText = option.getOptionText();
            keywordGetterMap.put(optionText, new KeywordGetter(optionText));
        }
        for (String text : keywordGetterMap.keySet()) {
            KeywordGetter keywordGetter = keywordGetterMap.get(text);
            FutureTask<KeywordsResponse> futureKeywordTask = new FutureTask<KeywordsResponse>
                    (keywordGetter);
            futureKeywordMap.put(text, futureKeywordTask);
            executorService.submit(futureKeywordTask);
        }


        Map<String, Search3> searchMap = new HashMap<>();
        Map<String, FutureTask<String>> futureSearchMap = new HashMap<>();
        try {
            String url = String.format(Search3.URL_BAIDU_ZHIDAO, URLEncoder.encode(question
                    .getQuestionText(), "gb2312"));
            Search3 search = new Search3(url, "gb2312");
            FutureTask<String> futureSearchTask = new FutureTask<String>(search);
            searchMap.put(Search3.SEARCH_TYPE_HIGH, search);
            futureSearchMap.put(Search3.SEARCH_TYPE_HIGH, futureSearchTask);
            executorService.submit(futureSearchTask);

            url = String.format(Search3.URL_BAIDU_HOME, URLEncoder.encode(question
                    .getQuestionText(), "gb2312"));
            search = new Search3(url, "utf-8");
            futureSearchTask = new FutureTask<String>(search);
            searchMap.put(Search3.SEARCH_TYPE_MIDDLE, search);
            futureSearchMap.put(Search3.SEARCH_TYPE_MIDDLE, futureSearchTask);
            executorService.submit(futureSearchTask);

            for (Question.Option option : options) {
                String optionText = option.getOptionText();
                String searchText = questionText + " " + optionText;
                url = String.format(Search3.URL_BAIDU_HOME, URLEncoder.encode(searchText,
                        "gb2312"));
                search = new Search3(url, "utf-8");
                FutureTask<String> futureTask = new FutureTask<String>(search);
                searchMap.put(optionText, search);
                futureSearchMap.put(optionText, futureTask);
                executorService.submit(futureTask);
            }
        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
        }

        for (String text : keywordGetterMap.keySet()) {
            List<Keyword> keywords = new ArrayList<>();
            FutureTask<KeywordsResponse> futureKeywordTask = futureKeywordMap.get(text);
            while (!futureKeywordTask.isDone()) {
            }
            try {
                KeywordsResponse keywordsResponse = futureKeywordTask.get();
                List<String> keywordList = keywordsResponse.getResult().getRes().getKeyword_list();

                if (keywordList == null) {
                    keywordList = new ArrayList<>();
                }
                for (int i = 0; i < keywordList.size(); i++) {
                    String keywordText = keywordList.get(i);
                    Keyword keyword = new Keyword();
                    keyword.setText(keywordText);
                    keywords.add(keyword);
                }
                if (text.equals(questionText)) {
                    question.setKeywords(keywords);
                } else {
                    for (Question.Option option : options) {
                        String optionText = option.getOptionText();
                        if (text.equals(optionText)) {
                            keywords.add(new Keyword(optionText));
                            option.setKeywords(keywords);
                        }
                    }
                }
            } catch (ExecutionException e) {
//                e.printStackTrace();
                continue;
            }
        }
        filterKeywords(question);

        FutureTask<String> futureSearchTask;
        String result = null;

        for (Question.Option option : options) {
            String optionText = option.getOptionText();
            futureSearchTask = futureSearchMap.get(optionText);
            while (!futureSearchTask.isDone()) {
            }
            try {
                result = futureSearchTask.get();
            } catch (ExecutionException e) {
//                e.printStackTrace();
            }
            for (Question.Option option1 : options) {
                for (Keyword keyword : option1.getKeywords()) {
                    int count = Search3.getCount(result, keyword.getText());
                    keyword.setCount3(optionText, count);
                }
            }
            printResultLow(question, optionText);
        }

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

        futureSearchTask = futureSearchMap.get(Search3.SEARCH_TYPE_HIGH);
        while (!futureSearchTask.isDone()) {
        }
        result = null;
        try {
            result = futureSearchTask.get();
        } catch (ExecutionException e) {
//            e.printStackTrace();
        }
        for (Question.Option option : options) {
            for (Keyword keyword : option.getKeywords()) {
                int count = Search3.getCount(result, keyword.getText());
                keyword.setCount(count);
            }
        }
        printResultHigh(question);

        execTime = System.currentTimeMillis() - startTime;
        System.out.println("答题总耗时: " + execTime + "毫秒");
    }

    private void run() throws InterruptedException {

        InformationGetter informationGetter = new InformationGetter();
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
                if(optionText.equals(option.getOptionText())) {
                    continue;
                }
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
