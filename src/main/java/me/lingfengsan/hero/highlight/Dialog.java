package me.lingfengsan.hero.highlight;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Highlighter;

/**
 * Created by maxliaops on 18-1-22.
 */

public class Dialog {
    private static final int SIZE = 18;
    private static final String FONT = "Dialog";

    private int index;
    private JFrame frame;
    private JTextPane textPane;
    private JScrollPane scrollPane;
    private Highlighter highlighter;
    private WordSearcher searcher;

    public Dialog(int index) {
        this.index = index;
        this.frame = new JFrame();
        this.scrollPane = new JScrollPane();
        this.textPane = new JTextPane();
        highlighter = new UnderlineHighlighter(Color.YELLOW);
        scrollPane.setViewportView(textPane);
        textPane.setHighlighter(highlighter);
        textPane.setFont(new Font(FONT, Font.PLAIN, SIZE));
        searcher = new WordSearcher(textPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(scrollPane, "Center");
        frame.setSize(400, 400);
        frame.setVisible(true);
        textPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent evt) {
            }

            @Override
            public void removeUpdate(DocumentEvent evt) {
            }

            @Override
            public void changedUpdate(DocumentEvent evt) {
            }
        });
    }

    public int getIndex() {
        return index;
    }

    public WordSearcher getSearcher() {
        return searcher;
    }

    public JTextPane getTextPane() {
        return textPane;
    }
}
