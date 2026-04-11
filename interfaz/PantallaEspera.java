package ConvexHull.interfaz;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class PantallaEspera extends JPanel {

    private Timer timerAnimacion;
    private String algoritmo;
    private int numPuntos;

    // Progreso visual suavizado (0.0 - 1.0)
    private float progresoVisual = 0f;

    // Bandera: el algoritmo terminó su cálculo
    private volatile boolean algoritmoTerminado = false;

    // Tiempo mínimo garantizado: 3000 ms a 16 ms/tick = ~187 ticks
    private static final int TIEMPO_MINIMO_MS = 1500;
    private static final int TICK_MS          = 16;
    private static final int TICKS_MINIMOS    = TIEMPO_MINIMO_MS / TICK_MS;

    private int ticksTranscurridos = 0;

    // Callback invocado cuando la barra llega visualmente al 100%
    private Runnable onCompletado;

    public PantallaEspera() {
        setOpaque(false);
        setLayout(null);
    }

    // =========================================================
    // API pública
    // =========================================================

    /**
     * Inicia la pantalla de carga.
     *
     * @param algoritmo    nombre del algoritmo seleccionado
     * @param numPuntos    número de puntos a procesar
     * @param onCompletado callback que se ejecuta cuando la barra llega al 100%
     */
    public void iniciar(String algoritmo, int numPuntos, Runnable onCompletado) {
        this.algoritmo          = algoritmo;
        this.numPuntos          = numPuntos;
        this.onCompletado       = onCompletado;
        this.progresoVisual     = 0f;
        this.algoritmoTerminado = false;
        this.ticksTranscurridos = 0;

        if (timerAnimacion != null) timerAnimacion.stop();

        timerAnimacion = new Timer(TICK_MS, e -> tick());
        timerAnimacion.start();
        repaint();
    }

    /**
     * Llamar desde el SwingWorker cuando el algoritmo haya terminado.
     * La barra completará el 100% solo si además han pasado los 3 segundos.
     */
    public void marcarAlgoritmoTerminado() {
        algoritmoTerminado = true;
    }

    /** Para el timer interno sin ejecutar el callback. */
    public void detener() {
        if (timerAnimacion != null) timerAnimacion.stop();
    }

    // =========================================================
    // Lógica de avance del progreso
    // =========================================================

    private void tick() {
        ticksTranscurridos++;

        // Fracción de tiempo transcurrida respecto al mínimo
        float fraccionTiempo = Math.min(1f, (float) ticksTranscurridos / TICKS_MINIMOS);

        // Objetivo: solo llega al 100% cuando el algoritmo terminó Y pasaron 3 s
        float objetivo;
        if (algoritmoTerminado && fraccionTiempo >= 1f) {
            objetivo = 1f;
        } else {
            // Antes de que ambas condiciones se cumplan, avanza hasta 95% como máximo
            objetivo = Math.min(0.95f, fraccionTiempo * 0.97f);
            // Si el algoritmo ya terminó pero el tiempo no, dejamos avanzar más rápido
            if (algoritmoTerminado) {
                objetivo = Math.max(objetivo, Math.min(0.95f, progresoVisual + 0.004f));
            }
        }

        // Suavizado con easing: más lento cerca del objetivo
        float distancia   = objetivo - progresoVisual;
        float incremento  = distancia * 0.04f + 0.0015f;
        progresoVisual   += incremento;
        progresoVisual    = Math.min(progresoVisual, objetivo);

        repaint();

        // Cuando llega al 100%, pausar 200 ms en "Done!" y luego avisar
        if (progresoVisual >= 0.999f && algoritmoTerminado && fraccionTiempo >= 1f) {
            progresoVisual = 1f;
            timerAnimacion.stop();
            repaint();

            Timer fin = new Timer(250, ev -> {
                ((Timer) ev.getSource()).stop();
                if (onCompletado != null) onCompletado.run();
            });
            fin.setRepeats(false);
            fin.start();
        }
    }

    // =========================================================
    // Pintado
    // =========================================================

    @Override



    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // Fondo oscuro semitransparente sobre toda la superficie
        g2.setColor(new Color(5, 15, 30, 215));
        g2.fillRect(0, 0, w, h);

        // ---- Tarjeta central ----
        int cardW = 360, cardH = 230;
        int cardX = w / 2 - cardW / 2;
        int cardY = h / 2 - cardH / 2;

        g2.setColor(new Color(12, 24, 48, 245));
        g2.fill(new RoundRectangle2D.Float(cardX, cardY, cardW, cardH, 20, 20));

        g2.setColor(new Color(255, 255, 255, 18));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(cardX, cardY, cardW, cardH, 20, 20));

        // Barra de acento superior (teal → azul)
        GradientPaint gpTop = new GradientPaint(cardX, cardY, new Color(55, 138, 221),
                cardX + cardW, cardY, new Color(93, 202, 165));
        g2.setPaint(gpTop);
        g2.fill(new RoundRectangle2D.Float(cardX, cardY, cardW, 5, 5, 5));
        g2.fillRect(cardX, cardY + 3, cardW, 4);

        int cx = w / 2;

        // ---- Título ----
        g2.setFont(new Font("Segoe UI", Font.BOLD, 17));
        g2.setColor(Color.WHITE);
        String tituloStr = "Computing convex hull\u2026";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(tituloStr, cx - fm.stringWidth(tituloStr) / 2, cardY + 46);

        // ---- Badge algoritmo · puntos ----
        if (algoritmo != null) {
            String badge = algoritmo + " \u00b7 " + String.format("%,d", numPuntos) + " pts";
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            fm = g2.getFontMetrics();
            int bw = fm.stringWidth(badge) + 24;
            int bh = 24;
            int bx = cx - bw / 2;
            int by = cardY + 58;
            g2.setColor(new Color(93, 202, 165, 35));
            g2.fill(new RoundRectangle2D.Float(bx, by, bw, bh, 12, 12));
            g2.setColor(new Color(93, 202, 165, 80));
            g2.setStroke(new BasicStroke(0.8f));
            g2.draw(new RoundRectangle2D.Float(bx, by, bw, bh, 12, 12));
            g2.setColor(new Color(93, 202, 165));
            g2.drawString(badge, bx + 12, by + bh / 2 + fm.getAscent() / 2 - 2);
        }

        // ---- Barra de progreso ----
        int barW = cardW - 48;
        int barH = 10;
        int barX = cardX + 24;
        int barY = cardY + 108;

        // Pista (fondo)
        g2.setColor(new Color(255, 255, 255, 22));
        g2.fill(new RoundRectangle2D.Float(barX, barY, barW, barH, barH, barH));

        // Relleno animado
        int rellenoW = Math.max(0, (int) (barW * progresoVisual));
        if (rellenoW > 0) {
            GradientPaint gpBar = new GradientPaint(
                    barX,        barY, new Color(55, 138, 221),
                    barX + barW, barY, new Color(93, 202, 165));
            g2.setPaint(gpBar);
            g2.fill(new RoundRectangle2D.Float(barX, barY, rellenoW, barH, barH, barH));

            // Brillo en el frente de la barra
            if (rellenoW >= barH * 2) {
                g2.setColor(new Color(255, 255, 255, 70));
                g2.fill(new RoundRectangle2D.Float(
                        barX + rellenoW - barH, barY + 1, barH, barH / 2, barH / 2, barH / 2));
            }
        }

        // ---- Porcentaje grande ----
        int porcentaje = Math.min(100, (int) (progresoVisual * 100));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 32));
        String pctStr = porcentaje + "%";
        boolean done  = porcentaje >= 100;
        g2.setColor(done ? new Color(93, 202, 165) : Color.WHITE);
        fm = g2.getFontMetrics();
        g2.drawString(pctStr, cx - fm.stringWidth(pctStr) / 2, barY + 58);

        // ---- Estado textual ----
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        String estado = done ? "Done!" : "Please wait\u2026";
        g2.setColor(done ? new Color(93, 202, 165, 200) : new Color(255, 255, 255, 100));
        fm = g2.getFontMetrics();
        g2.drawString(estado, cx - fm.stringWidth(estado) / 2, barY + 78);

        g2.dispose();
    }
}