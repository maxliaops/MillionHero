package me.lingfengsan.hero.highlight;

import java.awt.Color;

import javax.swing.text.DefaultHighlighter;

/**
 * Created by lenovo on 2018/1/21.
 */

class MyHighightPainter extends DefaultHighlighter.DefaultHighlightPainter{
    MyHighightPainter(Color color){
        super(color);
    }
}
