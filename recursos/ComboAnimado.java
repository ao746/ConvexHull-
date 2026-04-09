package ConvexHull.recursos;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxUI;

public class ComboAnimado extends JComboBox<String> {

    private boolean hover = false;

    public ComboAnimado(String[] opciones) {
        super(opciones);

        setOpaque(false);
        setFocusable(false);
        setForeground(Color.WHITE);
        setFont(new Font("Segoe UI", Font.BOLD, 14));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                b.setVisible(false);
                return b;
            }

            @Override
            public void paint(Graphics g, JComponent c) {
                paintCurrentValue(g, rectangleForCurrentValue(), false);
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle r, boolean hasFocus) {
                // no pintamos nada
            }

            @Override
            public void update(Graphics g, JComponent c) {
                paint(g, c);
            }
        });

        setBackground(new Color(0, 0, 0, 0));
        setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    label.setBackground(new Color(50, 50, 50, 255));
                } else {
                    label.setBackground(new Color(30, 30, 30, 255));
                }
                label.setForeground(Color.WHITE);
                label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                label.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
                label.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                return label;
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(hover ? new Color(0, 0, 0, 180) : new Color(0, 0, 0, 140));
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

        int cx = getWidth() - 18;
        int cy = getHeight() / 2;
        g2d.setColor(Color.WHITE);
        g2d.fillPolygon(
            new int[]{cx - 5, cx + 5, cx},
            new int[]{cy - 3, cy - 3, cy + 4},
            3
        );

        g2d.dispose();
        super.paintComponent(g);
    }
}