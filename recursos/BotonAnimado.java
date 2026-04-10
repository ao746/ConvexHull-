package ConvexHull
.recursos;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BotonAnimado extends JButton {

    private Color colorNormal;
    private Color colorHover;
    private Color colorClick;
    private Color colorActual;

    public BotonAnimado(String texto) {
        super(texto);

        colorNormal = new Color(18, 32, 58, 210);
        colorHover  = new Color(30, 50, 120);
        colorClick  = new Color(30,50,120);;
        colorActual = colorNormal;

        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setForeground(Color.WHITE);
        setFont(new Font("Segoe UI", Font.BOLD, 14));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                colorActual = colorHover;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                colorActual = colorNormal;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                colorActual = colorClick;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                colorActual = colorHover;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(colorActual);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        g2.dispose();
        super.paintComponent(g);
    }
}