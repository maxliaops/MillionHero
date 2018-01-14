package me.lingfengsan.hero;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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

    public static void main(String[] args) throws IOException {
        System.out.println("---------------------------------------------------");
        System.out.println("开始执行");
        BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            if (Debug) {
                String str = bf.readLine();
                System.out.println("开始执行");
                try {
                    if (str.length() == 0) {
                        run();
                    }
                } catch (Exception e) {
                    System.out.println("error");
                }
            } else {
                try {
                    run();
                } catch (Exception e) {
                    System.out.println("error");
                }
            }
        }
    }

    private static void run() throws InterruptedException {

        InformationGetter informationGetter = new InformationGetter();
        Question question = informationGetter.getQuestionAndAnswers();
        System.out.println(question.getQuestionId() + ". " + question.getQuestionText());
        List<Question.Option> options = question.getOptions();
        for (Question.Option option : options) {
            System.out.println(option.getOptionText());
        }

        FutureTask<Long> futureQuestion = new FutureTask<Long>(new SearchAndOpen(question));
        new Thread(futureQuestion).start();

        getKeywordsByBaidu(question);

        FutureTask<Long> futureQuestion0 = new FutureTask<Long>(new Search(question));
        new Thread(futureQuestion0).start();

        FutureTask<Long> futureQuestion3 = new FutureTask<Long>(new Search(question, 3));
        new Thread(futureQuestion3).start();

        FutureTask<Long> futureQuestion2 = new FutureTask<Long>(new Search(question, 2));
        new Thread(futureQuestion2).start();

        while (!futureQuestion0.isDone()) {
        }
        printResultHigh(question);

        while (!futureQuestion3.isDone()) {
        }
        printResultMiddle(question);

        while (!futureQuestion2.isDone()) {
        }
        printResultLow(question);
    }

    private static void getKeywordsByBaidu(Question question) {
        List<Question.Option> options = question.getOptions();
        for (Question.Option option : options) {
            List<Keyword> keywords = KeywordsApi.getInstance().getKeywords(option.getOptionText());
            if (keywords == null) {
                keywords = new ArrayList<>();
            }
            Keyword keyword2 = new Keyword();
            keyword2.setText(option.getOptionText());
            keywords.add(keyword2);

            option.setKeywords(keywords);
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
