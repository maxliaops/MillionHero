package me.lingfengsan.hero;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Callable;

/**
 * Created by 618 on 2018/1/8.
 *
 * @author lingfengsan
 */
public class SearchAndOpen implements Callable {
    private final Question question;

    SearchAndOpen(Question question) {
        this.question = question;
    }

    private Long searchAndOpen(Question question) throws IOException {
        String path = null;
        try {
//            path = "http://www.baidu.com/s?tn=ichuner&lm=-1&word=" +
//                    URLEncoder.encode(question, "gb2312") + "&rn=20";
            path = "https://zhidao.baidu.com/search?lm=0&rn=10&pn=0&fr=search&ie=gbk&word=" +
                    URLEncoder.encode(question.getQuestionText(), "gb2312");
//            boolean findIt = false;
//            String line = null;
//            URL url = new URL(path);
//            BufferedReader breaded = new BufferedReader(new InputStreamReader(url.openStream()));
//            while ((line = breaded.readLine()) != null) {
//                System.out.println(decodeUnicode(line));
////                printJson(line);
//            }

            //获取操作系统的名字
            String osName = System.getProperty("os.name", "");
            if (osName.startsWith("Windows")) {
                //windows的打开方式。
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + path);
            } else {
            //判断当前系统是否支持Java AWT Desktop扩展
            if (java.awt.Desktop.isDesktopSupported()) {
                try {
                    //创建一个URI实例,注意不是URL
                    java.net.URI uri = java.net.URI.create(path);
                    //获取当前系统桌面扩展
                    java.awt.Desktop dp = java.awt.Desktop.getDesktop();
                    //判断系统桌面是否支持要执行的功能
                    if (dp.isSupported(java.awt.Desktop.Action.BROWSE)) {
                        //获取系统默认浏览器打开链接
                        dp.browse(uri);
                    }
                } catch (java.lang.NullPointerException e) {
                    //此为uri为空时抛出异常
                } catch (java.io.IOException e) {
                    //此为无法获取系统默认浏览器
                }
            }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + path);
//        return new Search(question).search(question);
        return Long.valueOf(1);
    }

    public static String decodeUnicode(String theString) {
        char aChar;
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len);
        for (int x = 0; x < len; ) {
            aChar = theString.charAt(x++);
            if (aChar == '\\') {
                aChar = theString.charAt(x++);
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = theString.charAt(x++);
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed   \\uxxxx   encoding.");
                        }

                    }
                    outBuffer.append((char) value);
                } else {
                    if (aChar == 't')
                        aChar = '\t';
                    else if (aChar == 'r')
                        aChar = '\r';
                    else if (aChar == 'n')
                        aChar = '\n';
                    else if (aChar == 'f')
                        aChar = '\f';
                    outBuffer.append(aChar);
                }
            } else
                outBuffer.append(aChar);
        }
        return outBuffer.toString();
    }

    private static String getLevelStr(int level) {
        StringBuffer levelStr = new StringBuffer();
        for (int levelI = 0; levelI < level; levelI++) {
            levelStr.append("\t");
        }
        return levelStr.toString();
    }

    private void printJson(String jsonStr) {
        int level = 0;
        StringBuffer jsonForMatStr = new StringBuffer();
        for (int index = 0; index < jsonStr.length(); index++)//将字符串中的字符逐个按行输出
        {
            //获取s中的每个字符
            char c = jsonStr.charAt(index);
//          System.out.println(s.charAt(index));

            //level大于0并且jsonForMatStr中的最后一个字符为\n,jsonForMatStr加入\t
            if (level > 0 && '\n' == jsonForMatStr.charAt(jsonForMatStr.length() - 1)) {
                jsonForMatStr.append(getLevelStr(level));
//                System.out.println("123"+jsonForMatStr);
            }
            //遇到"{"和"["要增加空格和换行，遇到"}"和"]"要减少空格，以对应，遇到","要换行
            switch (c) {
                case '{':
                case '[':
                    jsonForMatStr.append(c + "\n");
                    level++;
                    break;
                case ',':
                    jsonForMatStr.append(c + "\n");
                    break;
                case '}':
                case ']':
                    jsonForMatStr.append("\n");
                    level--;
                    jsonForMatStr.append(getLevelStr(level));
                    jsonForMatStr.append(c);
                    break;
                default:
                    jsonForMatStr.append(c);
                    break;
            }
        }
//        System.out.println(jsonForMatStr);

    }

//    public static void main(String[] args) throws Exception {
//        SearchAndOpen search = new SearchAndOpen("阿尔茨海默症又被称为什么?");
//        System.out.println(search.call());
//    }

    @Override
    public Long call() throws Exception {
        return searchAndOpen(question);
    }
}
