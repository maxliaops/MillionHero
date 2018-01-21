package me.lingfengsan.hero.highlight;

import java.awt.Color;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

/**
 * Created by lenovo on 2018/1/21.
 */

public class WordSearcher {

    protected JTextComponent comp;

    public WordSearcher(JTextComponent comp) {
        this.comp = comp;
    }

    public int search(String word, Color color) {
        Highlighter.HighlightPainter painter = new MyHighightPainter(color);
        int firstOffset = -1;
        Highlighter highlighter = comp.getHighlighter();
        Highlighter.Highlight[] highlights = highlighter.getHighlights();
        for (int i = 0; i < highlights.length; i++) {
            Highlighter.Highlight h = highlights[i];
            if (h.getPainter() instanceof UnderlineHighlightPainter) {
//                highlighter.removeHighlight(h);
            }
        }
        if (word == null || word.equals("")) {
            return -1;
        }
        String content = null;
        try {
            Document d = comp.getDocument();
            content = d.getText(0, d.getLength()).toLowerCase();
        } catch (BadLocationException e) {
            return -1;
        }
        word = word.toLowerCase();
        int lastIndex = 0;
        int wordSize = word.length();
        while ((lastIndex = content.indexOf(word, lastIndex)) != -1) {
            int endIndex = lastIndex + wordSize;
            try {
                highlighter.addHighlight(lastIndex, endIndex, painter);
            } catch (BadLocationException e) {
            }
            if (firstOffset == -1) {
                firstOffset = lastIndex;
            }
            lastIndex = endIndex;
        }
        return firstOffset;
    }
}
