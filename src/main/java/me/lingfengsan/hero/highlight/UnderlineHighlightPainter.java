package me.lingfengsan.hero.highlight;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.Position;
import javax.swing.text.View;

/**
 * Created by lenovo on 2018/1/21.
 */

public class UnderlineHighlightPainter extends LayeredHighlighter.LayerPainter {
    protected Color color;

    public UnderlineHighlightPainter(Color c) {
        color = c;
    }

    @Override
    public void paint(Graphics g, int offs0, int offs1, Shape bounds,
                      JTextComponent c) {
    }

    @Override
    public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds,
                            JTextComponent c, View view) {
        g.setColor(color == null ? c.getSelectionColor() : color);
        Rectangle rect = null;
        if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) {
            if (bounds instanceof Rectangle) {
                rect = (Rectangle) bounds;
            } else {
                rect = bounds.getBounds();
            }
        } else {
            try {
                Shape shape = view.modelToView(offs0, Position.Bias.Forward, offs1,
                        Position.Bias.Backward, bounds);
                rect = (shape instanceof Rectangle) ? (Rectangle) shape : shape
                        .getBounds();
            } catch (BadLocationException e) {
                return null;
            }
        }
        FontMetrics fm = c.getFontMetrics(c.getFont());
        int baseline = rect.y + rect.height - fm.getDescent() + 1;
        g.drawLine(rect.x, baseline, rect.x + rect.width, baseline);
        g.drawLine(rect.x, baseline + 1, rect.x + rect.width, baseline + 1);
        return rect;
    }
}
