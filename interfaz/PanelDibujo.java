package ConvexHull.interfaz;

import  ConvexHull.algoritmos.Point;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class PanelDibujo extends JPanel implements MouseWheelListener, MouseListener, MouseMotionListener {

    private ArrayList<Point> puntos = new ArrayList<>();
    private ArrayList<Point> hull   = new ArrayList<>();

    private double zoom           = 1.0;
    private double desplazamientoX = 0;
    private double desplazamientoY = 0;
    private int ultimoMouseX;
    private int ultimoMouseY;
    private boolean arrastrando = false;

    public PanelDibujo() {
        addMouseWheelListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void setDatos(ArrayList<Point> puntos, ArrayList<Point> hull) {
        this.puntos = puntos;
        this.hull   = hull;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (puntos == null || puntos.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Bounding box
        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (Point p : puntos) {
            if (p.getX() < minX) minX = p.getX();
            if (p.getX() > maxX) maxX = p.getX();
            if (p.getY() < minY) minY = p.getY();
            if (p.getY() > maxY) maxY = p.getY();
        }

        int margen    = 40;
        int anchoBase = getWidth()  - margen * 2;
        int altoBase  = getHeight() - margen * 2;

        // Calcular escala para mantener proporción
        double rangoX = maxX - minX == 0 ? 1 : maxX - minX;
        double rangoY = maxY - minY == 0 ? 1 : maxY - minY;
        double escalaX = (anchoBase * zoom) / rangoX;
        double escalaY = (altoBase  * zoom) / rangoY;
        double escala  = Math.min(escalaX, escalaY);

        double ancho   = rangoX * escala;
        double alto    = rangoY * escala;
        double offsetX = (anchoBase - ancho) / 2.0;
        double offsetY = (altoBase  - alto)  / 2.0;

        // Dibujar puntos
        g2.setColor(Color.BLACK);
        for (Point p : puntos) {
            int px = transformarX(p.getX(), minX, escala, offsetX, margen);
            int py = transformarY(p.getY(), minY, escala, offsetY, margen, altoBase);
            g2.fillOval(px - 3, py - 3, 7, 7);
            g2.drawString(String.valueOf(p.getId()), px + 5, py - 5);
        }

        // Dibujar hull
        if (hull != null && hull.size() >= 2) {
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            for (int i = 0; i < hull.size(); i++) {
                Point a = hull.get(i);
                Point b = hull.get((i + 1) % hull.size());
                int ax = transformarX(a.getX(), minX, escala, offsetX, margen);
                int ay = transformarY(a.getY(), minY, escala, offsetY, margen, altoBase);
                int bx = transformarX(b.getX(), minX, escala, offsetX, margen);
                int by = transformarY(b.getY(), minY, escala, offsetY, margen, altoBase);
                g2.drawLine(ax, ay, bx, by);
            }
        }
    }

    private int transformarX(double x, double minX, double escala, double offsetX, int margen) {
        return (int)((x - minX) * escala + margen + offsetX + desplazamientoX);
    }

    private int transformarY(double y, double minY, double escala, double offsetY, int margen, int altoBase) {
        return (int)(altoBase - (y - minY) * escala + margen - offsetY + desplazamientoY);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int rotacion = e.getWheelRotation();
        if (rotacion < 0) zoom *= 1.1;
        else              zoom /= 1.1;
        zoom = Math.max(0.1, Math.min(zoom, 50.0));
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        arrastrando  = true;
        ultimoMouseX = e.getX();
        ultimoMouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (arrastrando) {
            desplazamientoX += e.getX() - ultimoMouseX;
            desplazamientoY += e.getY() - ultimoMouseY;
            ultimoMouseX = e.getX();
            ultimoMouseY = e.getY();
            repaint();
        }
    }

    @Override public void mouseReleased(MouseEvent e) { arrastrando = false; }
    @Override public void mouseClicked(MouseEvent e)  {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
    @Override public void mouseMoved(MouseEvent e)    {}
}