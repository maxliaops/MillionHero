package me.lingfengsan.hero;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by maxliaops on 18-1-30.
 */

public class HaoKanQuestionGetter {
    private static final String ADB_PATH = "/opt/Java/android-sdk-linux/platform-tools/adb";

    private static final int PARSE_PHASE_START = 0;
    private static final int PARSE_PHASE_GET_QUESTION = 1;
    private static final int PARSE_PHASE_GET_FIRST_OPTION = 2;
    private static final int PARSE_PHASE_END = 3;

    private static final String test = "E/imlog   (15018): 下发题目: 1.以下哪个是清朝皇帝的年号？ , 第几轮: [康熙, 乾隆, " +
            "同治] , 题号: 539";
    private static final String test1 = "; heartBeatTimeMs: 1515727066546; receiveTimeMs: " +
            "1515727067373; commitDelay: 0; optionList: [Option optionId: 291; text: 印度";
    private static final String test2 = "D/FantasyLiveManager(25177): ; choosenUsers: 0; percent0" +
            ".0, Option optionId: 192; text: 缅甸";

    private Question question;
    private String deviceId;
    private int state;
    private long startTime;
    private long endTime;
    private int optionCount = 0;

    public HaoKanQuestionGetter(String deviceId) {
        this.deviceId = deviceId;
        this.question = new Question();
    }

    public Question getQuestionAndAnswers() {
        String adbPath;
        if (deviceId == null) {
            adbPath = ADB_PATH;
        } else {
            adbPath = ADB_PATH + " -s " + deviceId;
        }
        Process process = null;
        String questionText;
        int questionId = 0;
        state = PARSE_PHASE_START;
        optionCount = 0;
//        System.out.print("0->");
        try {
            process = Runtime.getRuntime().exec(adbPath
                    + " shell logcat -c");
            process.waitFor();
            process = Runtime.getRuntime().exec(adbPath
                    + " shell logcat -s imlog");

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
                            break;
                        case PARSE_PHASE_GET_FIRST_OPTION:
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

    private String getQuestionId(String line) {
//        System.out.println("getQuestionId: line=" + line);
        String strPatter = "(?<=下发题目: )[0-9]+(?=\\.)";
        Pattern pattern = Pattern.compile(strPatter);
        Matcher matcher = pattern.matcher(line);
        String strQuestionId;
        if (matcher.find()) {
            String str = matcher.group();
//            strQuestionId = str.substring(19, str.length() - 1);
//            System.out.println("getQuestionId=" + str);
            return str;
        }
        return null;
    }

    private String getQuestionText(String line, String questionId) {
//        System.out.println("getQuestionText: line=" + line);
        String strPatter = "(?<=下发题目: " + questionId + "\\.)[\\S\\s]+(?= , 第几轮)";
        Pattern pattern = Pattern.compile(strPatter);
        Matcher matcher = pattern.matcher(line);
        String strQuestionText;
        if (matcher.find()) {
            strQuestionText = matcher.group();
//            System.out.println("getQuestionText: strQuestionText=" + strQuestionText);
            return strQuestionText;
        }
//        System.out.println("getQuestionText: line=" + line);
        return null;
    }

    private List<Question.Option> getOptions(String line) {
        List<Question.Option> options = new ArrayList<>();

//        String strPatter = "第几轮: \\[([\\s\\S]*)percent0.0];";
        String strPatter = "(?<=第几轮: \\[)[\\S\\s]+(?=\\] , 题号)";
        Pattern pattern = Pattern.compile(strPatter);
        Matcher matcher = pattern.matcher(line);
        String optionsStr = null;
        if (matcher.find()) {
            optionsStr = matcher.group();
        }
//        System.out.println(optionsStr);
        if (optionsStr != null) {
            String[] optionsArray = optionsStr.split(", ");
            for (int i = 0; i < optionsArray.length; i++) {
                String inputOptionStr = optionsArray[i];
//                System.out.println(inputOptionStr);
                Question.Option option = new Question.Option();
                option.setOptionId(i);
                option.setOptionText(inputOptionStr);
                options.add(option);
            }
        }
        return options;
    }

    private void onPhaseStart(String line) {
        if (line.contains("下发题目:")) {
            startTime = System.currentTimeMillis();
            String questionId = getQuestionId(line);
            if(questionId == null) return;
            String questionText = getQuestionText(line, questionId);
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
        }
    }

    private void onPhaseGetQuestion(String line) {
    }

    private void onPhaseGetFirstOption(String line) {

    }

    private void onPhaseEnd(String line) {

    }

}
