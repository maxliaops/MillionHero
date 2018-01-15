package me.lingfengsan.hero;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import me.lingfengsan.hero.keyword.Keyword;
import me.lingfengsan.hero.keyword.KeywordsApi;

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
                    main.run();
                } catch (Exception e) {
                    System.out.println("error");
                }
            }
        }
    }

    private void run() throws InterruptedException {

        InformationGetter informationGetter = new InformationGetter();
        Question question = informationGetter.getQuestionAndAnswers();
        System.out.println(question.getQuestionId() + ". " + question.getQuestionText());
        List<Question.Option> options = question.getOptions();
        for (Question.Option option : options) {
            System.out.println(option.getOptionText());
        }

        long startTime = System.currentTimeMillis();
        FutureTask<Long> futureQuestion = new FutureTask<Long>(new SearchAndOpen(question));
        executorService.submit(futureQuestion);

        getKeywordsByBaidu(question);
        long execTime = System.currentTimeMillis() - startTime;
        System.out.println("获取关键字耗时: " + execTime + "毫秒");

        FutureTask<Long> futureQuestion0 = new FutureTask<Long>(new Search(question));
        executorService.submit(futureQuestion0);

        FutureTask<Long> futureQuestion3 = new FutureTask<Long>(new Search(question, 3));
        executorService.submit(futureQuestion3);

        FutureTask<Long> futureQuestion2 = new FutureTask<Long>(new Search(question, 2));
        executorService.submit(futureQuestion2);

        while (!futureQuestion0.isDone()) {
        }
        printResultHigh(question);

        while (!futureQuestion3.isDone()) {
        }
        printResultMiddle(question);

        while (!futureQuestion2.isDone()) {
        }
        printResultLow(question);
        execTime = System.currentTimeMillis() - startTime;
        System.out.println("答题总耗时: " + execTime + "毫秒");
    }

    public static ExecutorService getExecutorService() {
        return executorService;
    }

    private static void getKeywordsByBaidu(Question question) {
        List<Question.Option> options = question.getOptions();
        for (Question.Option option : options) {
            List<Keyword> keywords = KeywordsApi.getInstance().getKeywords(option.getOptionText());
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
            System.out.println();
            System.out.println("-----------------------------高");
            System.out.println();
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
            System.out.println();
            System.out.println("-----------------------------");
        }
    }

    private static void printResultMiddle(Question question) {
        List<Question.Option> options = question.getOptions();
        if (options != null) {
            System.out.println();
            System.out.println("-----------------------------中");
            System.out.println();
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
            System.out.println();
            System.out.println("-----------------------------");
        }
    }

    private static void printResultLow(Question question) {
        List<Question.Option> options = question.getOptions();
        if (options != null) {
            System.out.println();
            System.out.println("-----------------------------低");
            System.out.println();
            for (int i = 0; i < options.size(); i++) {
                Question.Option option = options.get(i);
                if (option.getCount2() != 0) {
                    System.out.printf("        %3d" + " : " + option.getOptionText() + "%n", option
                            .getCount2());
                }
            }
            System.out.println();
            System.out.println("-----------------------------");
        }
    }

}
