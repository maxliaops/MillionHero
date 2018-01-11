package me.lingfengsan.hero;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by lenovo on 2018/1/11.
 */

public class InformationGetter {
    private static final String ADB_PATH = "C:\\Program Files (x86)\\Android\\adt-bundle-windows-x86-20131030\\sdk\\platform-tools\\adb.exe";

    private static final int PARSE_PHASE_START = 0;
    private static final int PARSE_PHASE_GET_QUESTION = 1;
    private static final int PARSE_PHASE_GET_FIRST_OPTION = 2;
    private static final int PARSE_PHASE_END = 3;

    int state;

    public Question getQuestionAndAnswers() {
        Process process = null;
        Question question = new Question();
        String questionText;
        state = PARSE_PHASE_START;
        try {
            process = Runtime.getRuntime().exec(ADB_PATH
                    + " shell logcat -c");
            process.waitFor();
            process = Runtime.getRuntime().exec(ADB_PATH
                    + " shell logcat -s FantasyLiveManager");

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if(state == PARSE_PHASE_END){
                    return question;
                }
                System.out.println(line);
                switch (state) {
                    case PARSE_PHASE_START:
                        onPhaseStart(line);
                        break;
                    case PARSE_PHASE_GET_QUESTION:
                        onPhaseGetQuestion(line);
                        break;
                    case PARSE_PHASE_GET_FIRST_OPTION:
                        onPhaseGetFirstOption(line);
                        break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void onPhaseStart(String line) {
        if (line.contains("onReceiveHeartBeat") && line.contains("currentQuestionId")) {
            String patter1 = "currentQuestionId: 12;";
            state = PARSE_PHASE_GET_QUESTION;
        }
    }

    private void onPhaseGetQuestion(String line) {
        if (line.contains("optionList") && line.contains("optionId")) {
            state = PARSE_PHASE_GET_FIRST_OPTION;
        }
    }

    private void onPhaseGetFirstOption(String line) {
        if (line.contains("countDownTime") && line.contains("answerStartTsMs")) {
            state = PARSE_PHASE_END;
        } else if(line.contains("optionId") && line.contains("text")) {

        }
    }

    private void onPhaseEnd(String line) {

    }

}