package me.lingfengsan.hero.highlight;

import java.awt.Color;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

/**
 * Created by lenovo on 2018/1/21.
 */

public class UnderlineHighlighter extends DefaultHighlighter {

    protected static final Highlighter.HighlightPainter sharedPainter = new MyHighightPainter(
            null);
    protected Highlighter.HighlightPainter painter;

    public UnderlineHighlighter(Color c) {
        painter = (c == null ? sharedPainter : new MyHighightPainter(c));
    }

    public Object addHighlight(int p0, int p1) throws BadLocationException {
        return addHighlight(p0, p1, painter);
    }

    @Override
    public void setDrawsLayeredHighlights(boolean newValue) {
        super.setDrawsLayeredHighlights(true);
    }

}