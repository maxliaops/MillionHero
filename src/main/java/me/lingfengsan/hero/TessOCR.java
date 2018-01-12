package me.lingfengsan.hero;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.LoadLibs;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by 618 on 2018/1/8.
 * @author lingfengsan
 */
public class TessOCR {
    String getOCR(File imageFile){
        InputStream is = null;
        BufferedImage src = null;
        int width = 720;
        int height = 1280;
        try {
            is = new FileInputStream(imageFile);
            src = javax.imageio.ImageIO.read(is);
            width = src.getWidth(null); // 得到源图宽
            height = src.getHeight(null);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ITesseract instance = new Tesseract();
        File tessDataFolder = LoadLibs.extractTessResources("tessdata");
        instance.setLanguage("chi_sim");
        //Set the tessdata path
        instance.setDatapath(tessDataFolder.getAbsolutePath());
        try {
//            int x = 67 * width / 720;
//            int y = 200 * height / 1280;
//            int w = 600 * width / 720;
//            int h = 600 * height / 1280;
            Rectangle rectangle = new Rectangle(67, 200, 600, 600);
            return instance.doOCR(imageFile,rectangle)
                    .replace(" ",".").replace(",","");
        } catch (TesseractException e) {
            System.err.println("提取文字失败："+e.getMessage());
        }
        return null;
    }

    public static void main(String[] args) {
        File imageFile=new File("D:\\23910392848779368.png");
        TessOCR tessOCR=new TessOCR();
        System.out.println(tessOCR.getOCR(imageFile));
    }
}
