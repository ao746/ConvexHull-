package ConvexHull.interfaz;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import  ConvexHull.algoritmos.ConvexHull;
import ConvexHull.algoritmos.FuerzaBruta;
import ConvexHull.algoritmos.JarvisMarch;
import ConvexHull.algoritmos.Point;
import ConvexHull.algoritmos.QuickHull;
import ConvexHull.recursos.BotonAnimado;
import ConvexHull.recursos.ComboAnimado;
import ConvexHull.recursos.GenerarDatos;
import ConvexHull.recursos.LectorPuntos;
import ConvexHull.recursos.PanelTransparente;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class VentanaPrincipal extends JFrame {

    private CardLayout cardLayout;
    private JPanel contenedor;
    private File archivo;
    private String algoElegido;
    private String distElegida;
    private int numPuntos;
    private boolean useFile;
    private boolean useGenerador;
    private ArrayList<Point> puntos;
    private ArrayList<Point> hull;
    private PanelDibujo panelDibujo;
    private Double tiempo;

    private JLabel labelTiempo;
    private JLabel labelAlgoritmo;
    private JLabel labelArchivo;
    private JLabel labelDistribucion;
    private JLabel labelNumPuntos;
    private JLabel labelNumHull;

    public VentanaPrincipal() {

        setTitle("Convex Hull");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setIconImage(Toolkit.getDefaultToolkit().getImage(
                getClass().getResource("/ConvexHull/decoration/icono.png")
        ));

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int anchoNormal = (int) (screen.width * 0.75);
        int altoNormal = (int) (screen.height * 0.75);

        setMinimumSize(new Dimension(900, 600));
        setSize(anchoNormal, altoNormal);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        addWindowStateListener(e -> {
            if ((e.getOldState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH
                    && e.getNewState() == Frame.NORMAL) {
                setSize(anchoNormal, altoNormal);
                setLocationRelativeTo(null);
            }
        });

        int altoPantalla = screen.height;
        int tituloSize   = Math.max(28, altoPantalla / 18);
        int textoGrande  = Math.max(18, altoPantalla / 45);
        int textoNormal  = Math.max(14, altoPantalla / 60);

        cardLayout = new CardLayout();
        contenedor = new JPanel(cardLayout);

        Dimension tamañoBoton = new Dimension(220, 45);

        // =========================
        // PANTALLA DE CONFIGURACIÓN
        // =========================
        JPanel fondo = new PanelConFondo("/ConvexHull/decoration/fondu.png");
        fondo.setLayout(new GridBagLayout());

        JPanel entradas = new JPanel();
        entradas.setLayout(new BoxLayout(entradas, BoxLayout.Y_AXIS));
        entradas.setOpaque(false);

        JLabel titulo = new JLabel("Configure Execution");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, tituloSize));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        titulo.setForeground(Color.BLACK);

        entradas.add(Box.createVerticalStrut(40));
        entradas.add(titulo);
        entradas.add(Box.createVerticalStrut(30));

        ComboAnimado algoritmosBoton = new ComboAnimado(new String[]{
                "Brute Force", "Jarvis", "QuickHull", "Graham Scan", "MergeHull"
        });
        algoritmosBoton.setAlignmentX(Component.CENTER_ALIGNMENT);
        algoritmosBoton.setPreferredSize(tamañoBoton);
        algoritmosBoton.setMaximumSize(tamañoBoton);

        JButton exe        = new BotonAnimado("Execute");
        exe.setAlignmentX(Component.CENTER_ALIGNMENT);
        exe.setPreferredSize(tamañoBoton);
        exe.setMaximumSize(tamañoBoton);

        JButton selecArchivo = new BotonAnimado("Select File");
        selecArchivo.setAlignmentX(Component.CENTER_ALIGNMENT);
        selecArchivo.setPreferredSize(tamañoBoton);
        selecArchivo.setMaximumSize(tamañoBoton);

        JButton randomp = new BotonAnimado("Generate random points");
        randomp.setAlignmentX(Component.CENTER_ALIGNMENT);
        randomp.setPreferredSize(tamañoBoton);
        randomp.setMaximumSize(tamañoBoton);

        entradas.add(Box.createVerticalStrut(20));
        entradas.add(algoritmosBoton);
        entradas.add(Box.createVerticalStrut(20));
        entradas.add(selecArchivo);
        entradas.add(Box.createVerticalStrut(20));
        entradas.add(randomp);
        entradas.add(Box.createVerticalStrut(20));
        entradas.add(exe);

        JLabel nombreArchivo = new JLabel("No file selected");
        nombreArchivo.setAlignmentX(Component.CENTER_ALIGNMENT);
        nombreArchivo.setFont(new Font("Segoe UI", Font.BOLD, textoNormal));
        entradas.add(Box.createVerticalStrut(15));
        entradas.add(nombreArchivo);

        JPanel wrapperEntradas = new JPanel(new GridBagLayout());
        wrapperEntradas.setOpaque(false);
        wrapperEntradas.add(entradas);

        fondo.add(wrapperEntradas);
        contenedor.add(fondo, "fondo");

        // =====================
        // PANTALLA DE RESULTADOS
        // =====================
        JPanel fondoSalida = new PanelConFondo("/ConvexHull/decoration/fondu.png");
        fondoSalida.setLayout(new BorderLayout());

        JPanel panelSalida = new JPanel(new BorderLayout(15, 15));
        panelSalida.setOpaque(false);
        panelSalida.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panelDibujo = new PanelDibujo();
        panelDibujo.setOpaque(false);
        panelDibujo.setMinimumSize(new Dimension(400, 300));
        panelSalida.add(panelDibujo, BorderLayout.CENTER);

        JButton repetir = new BotonAnimado("New execution");
        repetir.setPreferredSize(new Dimension(200, 45));
        repetir.addActionListener(y -> cardLayout.show(contenedor, "fondo"));
        panelSalida.add(repetir, BorderLayout.SOUTH);

        fondoSalida.add(panelSalida, BorderLayout.CENTER);

        // =====================
        // PANEL RESULTADOS PRO
        // =====================
        JPanel panelResultados = new PanelResultadosPro();
        panelResultados.setPreferredSize(new Dimension(320, 0));
        panelResultados.setMinimumSize(new Dimension(290, 0));

        // --- Sección: tiempo destacado ---
        JPanel cardTiempo = new MiniCard();
        cardTiempo.setLayout(new BoxLayout(cardTiempo, BoxLayout.Y_AXIS));
        cardTiempo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel etqTiempo = crearEtiqueta("EXECUTION TIME", 10, new Color(255, 255, 255, 110));
        labelTiempo = crearValor("— ms", textoNormal + 6, Color.WHITE);
        cardTiempo.add(etqTiempo);
        cardTiempo.add(Box.createVerticalStrut(4));
        cardTiempo.add(labelTiempo);

        // --- Sección: algoritmo con badge ---
        JPanel filaAlgo = new JPanel();
        filaAlgo.setOpaque(false);
        filaAlgo.setLayout(new BoxLayout(filaAlgo, BoxLayout.Y_AXIS));
        filaAlgo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel etqAlgo = crearEtiqueta("ALGORITHM", 10, new Color(255, 255, 255, 110));
        labelAlgoritmo = crearValor("—", textoNormal, Color.WHITE);
        filaAlgo.add(etqAlgo);
        filaAlgo.add(Box.createVerticalStrut(3));
        filaAlgo.add(labelAlgoritmo);

        // --- Grid: total puntos + hull puntos ---
        JPanel grid = new JPanel(new GridLayout(1, 2, 10, 0));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel cardPuntos = new MiniCard();
        cardPuntos.setLayout(new BoxLayout(cardPuntos, BoxLayout.Y_AXIS));
        JLabel etqPuntos = crearEtiqueta("TOTAL POINTS", 10, new Color(255, 255, 255, 110));
        labelNumPuntos = crearValor("—", textoNormal + 2, Color.WHITE);
        cardPuntos.add(etqPuntos);
        cardPuntos.add(Box.createVerticalStrut(3));
        cardPuntos.add(labelNumPuntos);

        JPanel cardHull = new MiniCard();
        cardHull.setLayout(new BoxLayout(cardHull, BoxLayout.Y_AXIS));
        JLabel etqHull = crearEtiqueta("HULL POINTS", 10, new Color(255, 255, 255, 110));
        labelNumHull = crearValor("—", textoNormal + 2, new Color(93, 202, 165));
        cardHull.add(etqHull);
        cardHull.add(Box.createVerticalStrut(3));
        cardHull.add(labelNumHull);

        grid.add(cardPuntos);
        grid.add(cardHull);

        // --- Distribución ---
        JPanel filaDist = new JPanel();
        filaDist.setOpaque(false);
        filaDist.setLayout(new BoxLayout(filaDist, BoxLayout.Y_AXIS));
        filaDist.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel etqDist = crearEtiqueta("DISTRIBUTION", 10, new Color(255, 255, 255, 110));
        labelDistribucion = crearValor("—", textoNormal, Color.WHITE);
        filaDist.add(etqDist);
        filaDist.add(Box.createVerticalStrut(3));
        filaDist.add(labelDistribucion);

        // --- Archivo ---
        JPanel filaArchivo = new JPanel();
        filaArchivo.setOpaque(false);
        filaArchivo.setLayout(new BoxLayout(filaArchivo, BoxLayout.Y_AXIS));
        filaArchivo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel etqArchivo = crearEtiqueta("SOURCE", 10, new Color(255, 255, 255, 110));
        labelArchivo = crearValor("—", textoNormal, new Color(255, 255, 255, 180));
        filaArchivo.add(etqArchivo);
        filaArchivo.add(Box.createVerticalStrut(3));
        filaArchivo.add(labelArchivo);

        // --- Logo centrado y grande ---
        JPanel logoZona = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        logoZona.setOpaque(false);
        logoZona.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoZona.setMaximumSize(new Dimension(Integer.MAX_VALUE, 155));

        ImageIcon logoMarca = new ImageIcon(getClass().getResource("/ConvexHull/decoration/logoo.png"));
        Image imgEscalada = logoMarca.getImage().getScaledInstance(128, 128, Image.SCALE_SMOOTH);
        JLabel marcaAgua = new JLabel(new ImageIcon(imgEscalada));
        logoZona.add(marcaAgua);

        // --- Ensamblar panel ---
        panelResultados.add(Box.createVerticalStrut(18));
        panelResultados.add(cardTiempo);
        panelResultados.add(Box.createVerticalStrut(14));
        panelResultados.add(new Separador());
        panelResultados.add(Box.createVerticalStrut(14));
        panelResultados.add(filaAlgo);
        panelResultados.add(Box.createVerticalStrut(14));
        panelResultados.add(new Separador());
        panelResultados.add(Box.createVerticalStrut(14));
        panelResultados.add(grid);
        panelResultados.add(Box.createVerticalStrut(14));
        panelResultados.add(new Separador());
        panelResultados.add(Box.createVerticalStrut(14));
        panelResultados.add(filaDist);
        panelResultados.add(Box.createVerticalStrut(14));
        panelResultados.add(filaArchivo);
        panelResultados.add(Box.createVerticalGlue());
        panelResultados.add(new Separador());
        panelResultados.add(logoZona);
        panelResultados.add(Box.createVerticalStrut(10));

        panelSalida.add(panelResultados, BorderLayout.EAST);
        contenedor.add(fondoSalida, "salida");

        add(contenedor);
        setVisible(true);

        // =====================
        // LISTENERS
        // =====================
        selecArchivo.addActionListener(e -> {
            JFileChooser elegir = new JFileChooser();
            int resultado = elegir.showOpenDialog(this);
            if (resultado == JFileChooser.APPROVE_OPTION) {
                archivo = elegir.getSelectedFile();
                nombreArchivo.setText("Selected file: " + archivo.getName());
                useFile = true;
                useGenerador = false;
                distElegida = null;
            }
        });

        randomp.addActionListener(e -> {
            JDialog dialog = new JDialog(this, "Generate random points", true);
            dialog.setSize(420, 220);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());

            ComboAnimado dist = new ComboAnimado(new String[]{
                    "Cuadrado", "Círculo", "Triángulo", "Rombo",
                    "Línea recta Horizontal", "Línea recta Vertical", "Gausiana"
            });
            dist.setPreferredSize(tamañoBoton);
            dist.setMaximumSize(tamañoBoton);
            dist.setAlignmentX(Component.CENTER_ALIGNMENT);

            JSpinner cantidad = new JSpinner(new SpinnerNumberModel(1000, 1, 10000000, 1000));
            ((JSpinner.DefaultEditor) cantidad.getEditor()).getTextField().setColumns(10);

            JButton aceptar = new BotonAnimado("Accept");
            aceptar.setPreferredSize(new Dimension(120, 40));
            aceptar.setAlignmentX(Component.CENTER_ALIGNMENT);

            JPanel pestaña = new JPanel();
            pestaña.setOpaque(true);
            pestaña.setLayout(new BoxLayout(pestaña, BoxLayout.Y_AXIS));
            pestaña.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            pestaña.add(dist);
            pestaña.add(Box.createVerticalStrut(15));
            JPanel panelCantidad = new JPanel();
            panelCantidad.add(new JLabel("Number of points: "));
            panelCantidad.add(cantidad);
            pestaña.add(panelCantidad);
            pestaña.add(Box.createVerticalStrut(20));
            pestaña.add(aceptar);

            aceptar.addActionListener(ex -> {
                distElegida = (String) dist.getSelectedItem();
                numPuntos = (int) cantidad.getValue();
                useGenerador = true;
                useFile = false;
                archivo = null;
                nombreArchivo.setText("Random generation selected");
                dialog.dispose();
            });

            dialog.add(pestaña, BorderLayout.CENTER);
            dialog.setVisible(true);
        });

        exe.addActionListener(z -> {
            algoElegido = (String) algoritmosBoton.getSelectedItem();

            if (archivo != null && useFile) {
                try {
                    puntos = LectorPuntos.leerArchivo(archivo);
                } catch (FileNotFoundException e1) {
                    JOptionPane.showMessageDialog(this, "Could not read the file");
                    return;
                }
            } else if (useGenerador && distElegida != null) {
                puntos = generarRandom(distElegida);
            } else {
                JOptionPane.showMessageDialog(this, "First you need to configure the execution");
                return;
            }

            if (puntos == null || puntos.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No valid points were generated or loaded");
                return;
            }

            long start = System.nanoTime();
            hull = algoritmoElegido(algoElegido, puntos);
            long end = System.nanoTime();
            tiempo = (end - start) / 1_000_000.0;

            if (hull == null) hull = new ArrayList<>();

            panelDibujo.setDatos(puntos, hull);
            panelDibujo.iniciarFadePoints(() -> {
                panelDibujo.animarHull(hull, () -> {
                    String tiempoStr = String.format("%.2f ms", tiempo);
                    labelTiempo.setText(tiempoStr);
                    labelAlgoritmo.setText(algoElegido);
                    labelDistribucion.setText(distElegida != null ? distElegida : "File input");
                    labelNumPuntos.setText(String.format("%,d", puntos.size()));
                    labelNumHull.setText(String.format("%,d", hull.size()));
                    labelArchivo.setText(archivo != null && useFile ? archivo.getName() : "Generated · no file");
                });
            });

            cardLayout.show(contenedor, "salida");
        });
    }

    // =====================
    // HELPERS LABELS
    // =====================

    private JLabel crearEtiqueta(String texto, int size, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.BOLD, size));
        l.setForeground(color);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel crearValor(String texto, int size, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.BOLD, size));
        l.setForeground(color);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    // =====================
    // LÓGICA
    // =====================

    public ArrayList<Point> generarRandom(String dist) {
        switch (dist) {
            case "Cuadrado":               return GenerarDatos.generarCuadrado(numPuntos);
            case "Círculo":                return GenerarDatos.generarCirculo(numPuntos);
            case "Triángulo":              return GenerarDatos.generarTriangulo(numPuntos);
            case "Rombo":                  return GenerarDatos.generarRombo(numPuntos);
            case "Línea recta Horizontal": return GenerarDatos.generarLineaRectaHorizontal(numPuntos);
            case "Línea recta Vertical":   return GenerarDatos.generarLineaRectaVertical(numPuntos);
            case "Gausiana":               return GenerarDatos.generarGaussiana(numPuntos);
            default:
                JOptionPane.showMessageDialog(this, "An error has occurred");
                return new ArrayList<>();
        }
    }

    public ArrayList<Point> algoritmoElegido(String algo, ArrayList<Point> puntos) {
        switch (algo) {
            case "Brute Force": return FuerzaBruta.calcularHull(puntos);
            case "Jarvis":      return JarvisMarch.calcularHull(puntos);
            case "QuickHull":   return QuickHull.calcularHull(puntos);
            case "Graham Scan": return ConvexHull.GrahamScanConvexHull(puntos);
            case "MergeHull":   return ConvexHull.computeMergeHull(puntos);
            default:
                JOptionPane.showMessageDialog(this, "An error has occurred");
                return new ArrayList<>();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(VentanaPrincipal::new);
    }

    // =====================
    // CLASES INTERNAS UI
    // =====================

    private static class PanelResultadosPro extends JPanel {
        private static final Color COLOR_FONDO    = new Color(18, 32, 58, 210);
        private static final Color COLOR_ACENTO_1 = new Color(55, 138, 221);
        private static final Color COLOR_ACENTO_2 = new Color(29, 158, 117);
        private static final int   BARRA_ALTO     = 4;
        private static final int   RADIO          = 14;

        PanelResultadosPro() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(BARRA_ALTO + 10, 16, 16, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            g2.setColor(COLOR_FONDO);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, RADIO, RADIO));

            GradientPaint gp = new GradientPaint(0, 0, COLOR_ACENTO_1, w, 0, COLOR_ACENTO_2);
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, RADIO, RADIO, RADIO));
            g2.fillRect(0, RADIO / 2, w, BARRA_ALTO);

            g2.dispose();
        }
    }

    private static class MiniCard extends JPanel {
        private static final Color BG = new Color(255, 255, 255, 18);

        MiniCard() {
            setOpaque(false);
            setBorder(new EmptyBorder(8, 10, 8, 10));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(BG);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            g2.setColor(new Color(255, 255, 255, 28));
            g2.setStroke(new BasicStroke(0.8f));
            g2.draw(new RoundRectangle2D.Float(0.4f, 0.4f, getWidth() - 0.8f, getHeight() - 0.8f, 10, 10));
            g2.dispose();
        }
    }

    private static class Separador extends JPanel {
        Separador() {
            setOpaque(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            setPreferredSize(new Dimension(1, 1));
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(new Color(255, 255, 255, 40));
            g.fillRect(0, 0, getWidth(), 1);
        }
    }

    private static class PanelConFondo extends JPanel {
        private final Image imagen;

        PanelConFondo(String ruta) {
            this.imagen = new ImageIcon(getClass().getResource(ruta)).getImage();
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(imagen, 0, 0, getWidth(), getHeight(), this);
        }
    }
}