package ConvexHull.interfaz;

import ConvexHull.algoritmos.Point;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PanelDibujo extends JPanel implements MouseWheelListener, MouseListener, MouseMotionListener {

    private ArrayList<Point> puntos   = new ArrayList<>();
    private ArrayList<Point> hull     = new ArrayList<>();

    private double zoom            = 1.0;
    private double desplazamientoX = 0;
    private double desplazamientoY = 0;
    private int    ultimoMouseX;
    private int    ultimoMouseY;
    private boolean arrastrando    = false;

    // --- Fade puntos ---
    private float alphaPoints = 1f;

    // --- Hull parcial (animación de trazado) ---
    private ArrayList<Point> hullParcial = new ArrayList<>();

    // --- Timers ---
    private Timer fadeTimer;
    private Timer hullTimer;
    private Timer pulsoTimer;
    private Timer destelloTimer;
    private Timer rellenoTimer;

    // --- Callbacks ---
    private Runnable onFadeDone;
    private Runnable onHullDone;

    // --- Colores hull ---
    private static final Color COLOR_LINEA_HULL   = new Color(30, 100, 255);
    private static final Color COLOR_RELLENO_BASE =new Color(255, 160, 90);

    // --- Puntos hull identificados por coordenadas "x,y" ---
    private Set<String> hullIds = new HashSet<>();  // CORREGIDO: usa coordenadas en vez de ID

    // --- Pulso: último punto añadido al hull ---
    private Point   puntoPulso   = null;
    private float   pulsoRadio   = 0f;
    private float   pulsoAlpha   = 0f;
    private boolean pulsoActivo  = false;

    // --- Destello cierre ---
    private boolean destelloActivo = false;
    private float   destelloAlpha  = 0f;
    private Point   puntoDestello  = null;

    // --- Relleno del polígono al cerrar (fade-in) ---
    private float   alphaRelleno   = 0f;
    private boolean rellenoVisible = false;
    private boolean hullCerrado    = false;

    // =========================================================

    public PanelDibujo() {
        addMouseWheelListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    // Clave única por coordenadas para evitar falsos positivos por IDs repetidos
    private String clave(Point p) {
        return p.getX() + "," + p.getY();
    }

    public void setDatos(ArrayList<Point> puntos, ArrayList<Point> hull) {
        this.puntos          = puntos;
        this.hull            = new ArrayList<>();
        this.hullParcial     = new ArrayList<>();
        this.hullIds         = new HashSet<>();
        this.hullCerrado     = false;
        this.rellenoVisible  = false;
        this.alphaRelleno    = 0f;
        repaint();
    }

    // =========================================================
    // PAINT
    // =========================================================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (puntos == null || puntos.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g.create();
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

        int    margen    = 40;
        int    anchoBase = getWidth()  - margen * 2;
        int    altoBase  = getHeight() - margen * 2;
        double rangoX    = maxX - minX == 0 ? 1 : maxX - minX;
        double rangoY    = maxY - minY == 0 ? 1 : maxY - minY;
        double escalaX   = (anchoBase * zoom) / rangoX;
        double escalaY   = (altoBase  * zoom) / rangoY;
        double escala    = Math.min(escalaX, escalaY);
        double ancho     = rangoX * escala;
        double alto      = rangoY * escala;
        double offsetX   = (anchoBase - ancho) / 2.0;
        double offsetY   = (altoBase  - alto)  / 2.0;

        // ---- Relleno azul transparente ----
        if (rellenoVisible && hullParcial != null && hullParcial.size() >= 3 && alphaRelleno > 0f) {
            ArrayList<Point> poligono = hullParcial;
            int n = poligono.size();
            if (n > 1 && clave(poligono.get(0)).equals(clave(poligono.get(n - 1)))) {
                n = n - 1;
            }
            int[] xs = new int[n];
            int[] ys = new int[n];
            for (int i = 0; i < n; i++) {
                xs[i] = transformarX(poligono.get(i).getX(), minX, escala, offsetX, margen);
                ys[i] = transformarY(poligono.get(i).getY(), minY, escala, offsetY, margen, altoBase);
            }
            int alphaInt = Math.min(255, (int)(alphaRelleno * 255));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g2.setColor(new Color(
                COLOR_RELLENO_BASE.getRed(),
                COLOR_RELLENO_BASE.getGreen(),
                COLOR_RELLENO_BASE.getBlue(),
                alphaInt
            ));
            g2.fillPolygon(xs, ys, n);
        }

        // ---- Dibujar puntos ----
        for (Point p : puntos) {
            int px = transformarX(p.getX(), minX, escala, offsetX, margen);
            int py = transformarY(p.getY(), minY, escala, offsetY, margen, altoBase);
            boolean esHull = hullIds.contains(clave(p)); // CORREGIDO: compara por coordenadas

            if (esHull) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                g2.setColor(new Color(255, 100, 30));
                g2.fillOval(px - 5, py - 5, 11, 11);
                g2.setColor(new Color(255, 60, 0));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(px - 5, py - 5, 11, 11);
            } else {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaPoints));
                g2.setColor(Color.BLACK);
                g2.fillOval(px - 3, py - 3, 7, 7);
            }

            if (puntos.size() <= 60) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaPoints));
                g2.setColor(esHull ? new Color(255, 140, 0) : Color.DARK_GRAY);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.drawString(String.valueOf(p.getId()), px + 6, py - 5);
            }
        }

        // ---- Dibujar hull parcial ----
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        if (hullParcial != null && hullParcial.size() >= 2) {
            g2.setColor(COLOR_LINEA_HULL);
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < hullParcial.size() - 1; i++) {
                Point a = hullParcial.get(i);
                Point b = hullParcial.get(i + 1);
                int ax = transformarX(a.getX(), minX, escala, offsetX, margen);
                int ay = transformarY(a.getY(), minY, escala, offsetY, margen, altoBase);
                int bx = transformarX(b.getX(), minX, escala, offsetX, margen);
                int by = transformarY(b.getY(), minY, escala, offsetY, margen, altoBase);
                g2.drawLine(ax, ay, bx, by);
            }
        }

        // ---- Pulso en el último punto añadido ----
        if (pulsoActivo && puntoPulso != null && pulsoAlpha > 0f) {
            int px = transformarX(puntoPulso.getX(), minX, escala, offsetX, margen);
            int py = transformarY(puntoPulso.getY(), minY, escala, offsetY, margen, altoBase);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulsoAlpha));
            g2.setColor(new Color(255, 200, 50));
            g2.setStroke(new BasicStroke(2f));
            int r = (int) pulsoRadio;
            g2.drawOval(px - r, py - r, r * 2, r * 2);
        }

        // ---- Destello al cerrar hull ----
        if (destelloActivo && puntoDestello != null && destelloAlpha > 0f) {
            int px = transformarX(puntoDestello.getX(), minX, escala, offsetX, margen);
            int py = transformarY(puntoDestello.getY(), minY, escala, offsetY, margen, altoBase);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, destelloAlpha));
            for (int i = 1; i <= 3; i++) {
                float r = 8 + i * 10 * (1f - destelloAlpha);
                g2.setColor(new Color(255, 255, 100));
                g2.setStroke(new BasicStroke(2.5f - i * 0.5f));
                g2.drawOval((int)(px - r), (int)(py - r), (int)(r * 2), (int)(r * 2));
            }
        }

        g2.dispose();
    }

    // =========================================================
    // ANIMACIONES PÚBLICAS
    // =========================================================

    public void iniciarFadePoints(Runnable onDone) {
        if (fadeTimer != null) fadeTimer.stop();
        alphaPoints     = 0f;
        hullParcial     = new ArrayList<>();
        hullIds         = new HashSet<>();
        hullCerrado     = false;
        rellenoVisible  = false;
        alphaRelleno    = 0f;
        this.onFadeDone = onDone;

        fadeTimer = new Timer(16, e -> {
            alphaPoints = Math.min(alphaPoints + 0.035f, 1f);
            repaint();
            if (alphaPoints >= 1f) {
                ((Timer) e.getSource()).stop();
                if (onFadeDone != null) onFadeDone.run();
            }
        });
        fadeTimer.start();
    }

    public void animarHull(ArrayList<Point> hullCompleto, Runnable onDone) {
        if (hullTimer != null) hullTimer.stop();
        hullParcial     = new ArrayList<>();
        this.hull       = hullCompleto;
        this.onHullDone = onDone;

        if (hullCompleto == null || hullCompleto.isEmpty()) {
            if (onDone != null) onDone.run();
            return;
        }

        int delay  = hullCompleto.size() > 20 ? 90 : 160;
        int[] paso = {0};

        hullTimer = new Timer(delay, e -> {
            if (paso[0] < hullCompleto.size()) {
                Point p = hullCompleto.get(paso[0]);
                hullParcial.add(p);
                hullIds.add(clave(p)); // CORREGIDO: añade por coordenadas
                paso[0]++;
                dispararPulso(p);
                repaint();
            } else {
                if (!hullParcial.isEmpty()) {
                    hullParcial.add(hullCompleto.get(0));
                }
                hullCerrado = true;
                repaint();
                ((Timer) e.getSource()).stop();

                dispararDestello(hullCompleto.get(0));

                Timer fin = new Timer(400, ev -> {
                    ((Timer) ev.getSource()).stop();
                    aparecerRelleno(() -> {
                        if (onHullDone != null) onHullDone.run();
                    });
                });
                fin.setRepeats(false);
                fin.start();
            }
        });
        hullTimer.start();
    }

    // =========================================================
    // EFECTOS INTERNOS
    // =========================================================

    private void dispararPulso(Point p) {
        if (pulsoTimer != null) pulsoTimer.stop();
        puntoPulso  = p;
        pulsoRadio  = 6f;
        pulsoAlpha  = 0.9f;
        pulsoActivo = true;

        pulsoTimer = new Timer(20, e -> {
            pulsoRadio += 2.5f;
            pulsoAlpha -= 0.07f;
            if (pulsoAlpha <= 0f) {
                pulsoAlpha  = 0f;
                pulsoActivo = false;
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });
        pulsoTimer.start();
    }

    private void dispararDestello(Point p) {
        if (destelloTimer != null) destelloTimer.stop();
        puntoDestello  = p;
        destelloAlpha  = 1f;
        destelloActivo = true;

        destelloTimer = new Timer(16, e -> {
            destelloAlpha -= 0.04f;
            if (destelloAlpha <= 0f) {
                destelloAlpha  = 0f;
                destelloActivo = false;
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });
        destelloTimer.start();
    }

    private void aparecerRelleno(Runnable onDone) {
        if (rellenoTimer != null) rellenoTimer.stop();
        rellenoVisible = true;
        alphaRelleno   = 0f;
        final float TARGET = 0.22f;

        rellenoTimer = new Timer(16, e -> {
            alphaRelleno += 0.008f;
            if (alphaRelleno >= TARGET) {
                alphaRelleno = TARGET;
                ((Timer) e.getSource()).stop();
                if (onDone != null) onDone.run();
            }
            repaint();
        });
        rellenoTimer.start();
    }

    // =========================================================
    // LIMPIEZA
    // =========================================================

    public void limpiar() {
        puntos          = new ArrayList<>();
        hull            = new ArrayList<>();
        hullParcial     = new ArrayList<>();
        hullIds         = new HashSet<>();
        alphaPoints     = 0f;
        alphaRelleno    = 0f;
        rellenoVisible  = false;
        hullCerrado     = false;
        destelloActivo  = false;
        pulsoActivo     = false;
        zoom            = 1.0;
        desplazamientoX = 0;
        desplazamientoY = 0;
        for (Timer t : new Timer[]{fadeTimer, hullTimer, pulsoTimer, destelloTimer, rellenoTimer}) {
            if (t != null) t.stop();
        }
        repaint();
    }

    // =========================================================
    // TRANSFORMACIONES
    // =========================================================

    private int transformarX(double x, double minX, double escala, double offsetX, int margen) {
        return (int)((x - minX) * escala + margen + offsetX + desplazamientoX);
    }

    private int transformarY(double y, double minY, double escala, double offsetY, int margen, int altoBase) {
        return (int)(altoBase - (y - minY) * escala + margen - offsetY + desplazamientoY);
    }

    // =========================================================
    // MOUSE
    // =========================================================

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int rotacion = e.getWheelRotation();
        if (rotacion < 0) zoom *= 1.1;
        else              zoom /= 1.1;
        zoom = Math.max(0.1, Math.min(zoom, 50.0));
        repaint();
    }

    @Override public void mousePressed(MouseEvent e)  { arrastrando = true; ultimoMouseX = e.getX(); ultimoMouseY = e.getY(); }
    @Override public void mouseReleased(MouseEvent e) { arrastrando = false; }

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

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e)  {}
    @Override public void mouseMoved(MouseEvent e)   {}
}