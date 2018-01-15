package me.lingfengsan.hero;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lenovo on 2018/1/11.
 */

public class InformationGetter {
    private static final String ADB_PATH = "/opt/Java/android-sdk-linux/platform-tools/adb";

    private static final int PARSE_PHASE_START = 0;
    private static final int PARSE_PHASE_GET_QUESTION = 1;
    private static final int PARSE_PHASE_GET_FIRST_OPTION = 2;
    private static final int PARSE_PHASE_END = 3;

    private static final String test = "D/FantasyLiveManager(25177): onReceiveHeartBeat isFromWs:" +
            " true; time: 1515727067380; status: 3; lastStatus: 4; 13241354heartbeat: HeartBeat " +
            "activityId：13241354; timestampMs: 1515727066546; currentUsers: 1639184; " +
            "currentQuestionId: 12; currentActivityStatue: 3; question: Question activityId: " +
            "13241354; questionId: 12; questionStartTimeMs: 1515727066545; timeLimit: 10000; " +
            "text: 湄公河不经过以下哪个国家？";
    private static final String test1 = "; heartBeatTimeMs: 1515727066546; receiveTimeMs: " +
            "1515727067373; commitDelay: 0; optionList: [Option optionId: 291; text: 印度";
    private static final String test2 = "D/FantasyLiveManager(25177): ; choosenUsers: 0; percent0" +
            ".0, Option optionId: 192; text: 缅甸";

    Question question;
    int state;
    long startTime;
    long endTime;
    int optionCount = 0;

    public InformationGetter() {
        this.question = new Question();
    }

    public Question getQuestionAndAnswers() {
        Process process = null;
        String questionText;
        int questionId = 0;
        state = PARSE_PHASE_START;
        optionCount = 0;
//        System.out.print("0->");
        try {
            process = Runtime.getRuntime().exec(ADB_PATH
                    + " shell logcat -c");
            process.waitFor();
            process = Runtime.getRuntime().exec(ADB_PATH
                    + " shell logcat -s FantasyLiveManager");

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "utf-8"));
            String line = null;
            while (state != PARSE_PHASE_END) {
                if (!Main.Debug) {
                    line = bufferedReader.readLine();
                } else {
                    switch (state) {
                        case PARSE_PHASE_START:
                            line = test;
                            break;
                        case PARSE_PHASE_GET_QUESTION:
                            line = test1;
                            break;
                        case PARSE_PHASE_GET_FIRST_OPTION:
                            line = test2;
                            break;
                    }
                }
                if (line == null) continue;
//                System.out.println(line);
                switch (state) {
                    case PARSE_PHASE_START:
//                        line = test;
                        onPhaseStart(line);
                        break;
                    case PARSE_PHASE_GET_QUESTION:
//                        line = test1;
                        onPhaseGetQuestion(line);
                        break;
                    case PARSE_PHASE_GET_FIRST_OPTION:
//                        line = test2;
                        onPhaseGetFirstOption(line);
                        break;
                }
            }
            process.destroy();
            int exitcode = process.waitFor();
//            System.out.println("finish: "+exitcode);
            return question;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    private int getQuestionId(String line) {
//        System.out.println("getQuestionId: line=" + line);
        String strPatter = "currentQuestionId: [0-9]+;";
        Pattern pattern = Pattern.compile(strPatter);
        Matcher matcher = pattern.matcher(line);
        String strQuestionId;
        if (matcher.find()) {
            String str = matcher.group();
            strQuestionId = str.substring(19, str.length() - 1);
//            System.out.println("getQuestionId=" + strQuestionId);
            return Integer.valueOf(strQuestionId);
        }
        return 0;
    }

    private String getQuestionText(String line) {
//        System.out.println("getQuestionText: line=" + line);
//        String strPatter = "timeLimit: 10000; text: ([\\s\\S]*)\\uff1f";
        String strPatter = "(?<=timeLimit: 10000; text: )[\\S\\s]+(?=\\uff1f)";
        Pattern pattern = Pattern.compile(strPatter);
        Matcher matcher = pattern.matcher(line);
        String strQuestionText;
        if (matcher.find()) {
            strQuestionText = matcher.group();
//            strQuestionText = str.substring(24, str.length());
//            System.out.println("getQuestionText=" + strQuestionText);
            return strQuestionText;
        }
        return null;
    }

    private int getOptionId(String line) {
//        System.out.println("getOptionId: line=" + line);
        String strPatter = "optionId: [0-9]+;";
        Pattern pattern = Pattern.compile(strPatter);
        Matcher matcher = pattern.matcher(line);
        String strOptionId;
        if (matcher.find()) {
            String str = matcher.group();
            strOptionId = str.substring(10, str.length() - 1);
//            System.out.println("getQuestionId=" + strOptionId);
            return Integer.valueOf(strOptionId);
        }
        return 0;
    }

    private String getOptionText(String line, int optionId) {
//        System.out.println("getOptionText: line=" + line);
        String strOptionId = String.valueOf(optionId);
        String strPatter = "optionId: " + strOptionId + "; text: ([\\s\\S]*)$";
        Pattern pattern = Pattern.compile(strPatter);
        Matcher matcher = pattern.matcher(line);
        String strOptionText;
        if (matcher.find()) {
            String str = matcher.group();
            strOptionText = str.substring(18 + strOptionId.length(), str.length());
//            System.out.println("getOptionText=" + strOptionText);
            return strOptionText;
        }
        return null;
    }

    private List<Question.Option> getOptions(String line) {
        List<Question.Option> options = new ArrayList<>();

        String strPatter = "optionList:([\\s\\S]*)percent0.0];";
        Pattern pattern = Pattern.compile(strPatter);
        Matcher matcher = pattern.matcher(line);
        String optionsStr = null;
        if (matcher.find()) {
            optionsStr = matcher.group();
        }
//        System.out.println(optionsStr);
        if (optionsStr != null) {
            String[] optionsArray = optionsStr.split("; choosenUsers: 0");
            for (int i = 0; i < optionsArray.length; i++) {
                String inputOptionStr = optionsArray[i];
//                System.out.println(inputOptionStr);
                if (inputOptionStr.contains("optionId")) {
                    int optionId = getOptionId(inputOptionStr);
                    String optionStr = getOptionText(inputOptionStr, optionId);
                    Question.Option option = new Question.Option();
                    option.setOptionId(optionId);
                    option.setOptionText(optionStr);
                    options.add(option);
                }
            }
        }
        return options;
    }

    private void onPhaseStart(String line) {
        if (line.contains("onReceiveHeartBeat")
                && line.contains("currentQuestionId")
                && !line.contains("currentQuestionId: 0;")
                && (line.contains("status: 3; lastStatus: 4;")
                || line.contains("status: 3; lastStatus: 2;"))
                && line.contains("optionList")
                && line.contains("optionId")
                && line.contains("percent0.0];")) {
            startTime = System.currentTimeMillis();
            int questionId = getQuestionId(line);
            String questionText = getQuestionText(line);
            question.setQuestionId(questionId);
            question.setQuestionText(questionText);
            state = PARSE_PHASE_GET_QUESTION;
//            System.out.print("1->");
            System.out.println();
            List<Question.Option> options = getOptions(line);
            question.setOptions(options);
            optionCount = options.size();
            state = PARSE_PHASE_END;
            System.out.println();
        } else if (line.contains("onReceiveHeartBeat")
                && line.contains("currentQuestionId")
                && !line.contains("currentQuestionId: 0;")
                && (line.contains("status: 3; lastStatus: 4;")
                || line.contains("status: 3; lastStatus: 2;"))) {
            startTime = System.currentTimeMillis();
//            System.out.println(line);
            int questionId = getQuestionId(line);
//            System.out.println("questionId=" + questionId);
            String questionText = getQuestionText(line);
            question.setQuestionId(questionId);
            question.setQuestionText(questionText);
            state = PARSE_PHASE_GET_QUESTION;
            System.out.println();
//            System.out.println("PARSE_PHASE_GET_QUESTION");

            if (line.contains("optionList") && line.contains("optionId")) {
//                System.out.println(line);
                int optionId = getOptionId(line);
                String optionText = getOptionText(line, optionId);
                Question.Option option = new Question.Option();
                option.setOptionId(optionId);
                option.setOptionText(optionText);
                question.addOption(option);
                optionCount++;
                state = PARSE_PHASE_GET_FIRST_OPTION;
                System.out.println();
//                System.out.println("PARSE_PHASE_GET_FIRST_OPTION");
            }
        }
    }

    private void onPhaseGetQuestion(String line) {
        if (line.contains("optionList") && line.contains("optionId")) {
//            System.out.println(line);
            int optionId = getOptionId(line);
            String optionText = getOptionText(line, optionId);
            Question.Option option = new Question.Option();
            option.setOptionId(optionId);
            option.setOptionText(optionText);
            question.addOption(option);
            optionCount++;
            state = PARSE_PHASE_GET_FIRST_OPTION;
            System.out.println();
//            System.out.println("PARSE_PHASE_GET_FIRST_OPTION");
        }
    }

    private void onPhaseGetFirstOption(String line) {
        if (line.contains("percent0.0];")) {
//            System.out.println(line);
            state = PARSE_PHASE_END;
            System.out.println();
//            System.out.println("PARSE_PHASE_END");
            endTime = System.currentTimeMillis();
            long excTime = endTime - startTime;
//            System.out.println("解析时间：" + excTime + "ms");
        } else if (line.contains("optionId") && line.contains("text") && !line.contains
                ("optionList")) {
//            System.out.println(line);
            int optionId = getOptionId(line);
            String optionText = getOptionText(line, optionId);
            Question.Option option = new Question.Option();
            option.setOptionId(optionId);
            option.setOptionText(optionText);
            question.addOption(option);
            optionCount++;
            if (optionCount >= 3) {
                state = PARSE_PHASE_END;
                System.out.println();
//                System.out.println("PARSE_PHASE_END");
                endTime = System.currentTimeMillis();
                long excTime = endTime - startTime;
//                System.out.println("解析时间：" + excTime + "ms");
            }
        }
    }

    private void onPhaseEnd(String line) {

    }

}
