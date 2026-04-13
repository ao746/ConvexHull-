package ConvexHull.interfaz;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import ConvexHull.algoritmos.ConvexHull;
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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

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
    private PantallaEspera pantallaEspera;
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
        int altoNormal  = (int) (screen.height * 0.75);

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
        int tituloSize  = Math.max(28, altoPantalla / 18);
        int textoNormal = Math.max(14, altoPantalla / 60);

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

        JButton exe = new BotonAnimado("Execute");
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

        // panelDibujo con layout null para superponer pantallaEspera
        panelDibujo = new PanelDibujo();
        panelDibujo.setOpaque(false);
        panelDibujo.setMinimumSize(new Dimension(400, 300));
        panelDibujo.setLayout(null);

        pantallaEspera = new PantallaEspera();
        pantallaEspera.setVisible(false);
        panelDibujo.add(pantallaEspera);

        panelDibujo.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                pantallaEspera.setBounds(0, 0, panelDibujo.getWidth(), panelDibujo.getHeight());
            }
        });

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

        JPanel cardTiempo = new MiniCard();
        cardTiempo.setLayout(new BoxLayout(cardTiempo, BoxLayout.Y_AXIS));
        cardTiempo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel etqTiempo = crearEtiqueta("EXECUTION TIME", 10, new Color(255, 255, 255, 110));
        labelTiempo = crearValor("— ms", textoNormal + 6, Color.WHITE);
        cardTiempo.add(etqTiempo);
        cardTiempo.add(Box.createVerticalStrut(4));
        cardTiempo.add(labelTiempo);

        JPanel filaAlgo = new JPanel();
        filaAlgo.setOpaque(false);
        filaAlgo.setLayout(new BoxLayout(filaAlgo, BoxLayout.Y_AXIS));
        filaAlgo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel etqAlgo = crearEtiqueta("ALGORITHM", 10, new Color(255, 255, 255, 110));
        labelAlgoritmo = crearValor("—", textoNormal, Color.WHITE);
        filaAlgo.add(etqAlgo);
        filaAlgo.add(Box.createVerticalStrut(3));
        filaAlgo.add(labelAlgoritmo);

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

        JPanel filaDist = new JPanel();
        filaDist.setOpaque(false);
        filaDist.setLayout(new BoxLayout(filaDist, BoxLayout.Y_AXIS));
        filaDist.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel etqDist = crearEtiqueta("DISTRIBUTION", 10, new Color(255, 255, 255, 110));
        labelDistribucion = crearValor("—", textoNormal, Color.WHITE);
        filaDist.add(etqDist);
        filaDist.add(Box.createVerticalStrut(3));
        filaDist.add(labelDistribucion);

        JPanel filaArchivo = new JPanel();
        filaArchivo.setOpaque(false);
        filaArchivo.setLayout(new BoxLayout(filaArchivo, BoxLayout.Y_AXIS));
        filaArchivo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel etqArchivo = crearEtiqueta("SOURCE", 10, new Color(255, 255, 255, 110));
        labelArchivo = crearValor("—", textoNormal, new Color(255, 255, 255, 180));
        filaArchivo.add(etqArchivo);
        filaArchivo.add(Box.createVerticalStrut(3));
        filaArchivo.add(labelArchivo);

        JPanel logoZona = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        logoZona.setOpaque(false);
        logoZona.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoZona.setMaximumSize(new Dimension(Integer.MAX_VALUE, 155));

        ImageIcon logoMarca = new ImageIcon(getClass().getResource("/ConvexHull/decoration/logoo.png"));
        Image imgEscalada = logoMarca.getImage().getScaledInstance(128, 128, Image.SCALE_SMOOTH);
        JLabel marcaAgua = new JLabel(new ImageIcon(imgEscalada));
        logoZona.add(marcaAgua);

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
                useFile     = true;
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
                    "Square", "Circle", "Triangle", "Diamond",
                    "Horizontal straight line", "Vertical straight line", "Gaussian"
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
                distElegida  = (String) dist.getSelectedItem();
                numPuntos    = (int) cantidad.getValue();
                useGenerador = true;
                useFile      = false;
                archivo      = null;
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

            // Limpiar panel y cambiar a pantalla de resultados
            panelDibujo.limpiar();
            cardLayout.show(contenedor, "salida");

            // Capturar variables finales para el worker
            final ArrayList<Point> puntosFinales = new ArrayList<>(puntos);
            final String algoFinal    = algoElegido;
            final String distFinal    = distElegida;
            final File   archivoFinal = archivo;
            final boolean useFileFinal = useFile;

            // Guardar resultado del algoritmo mientras la carga termina
            final ArrayList<Point>[] hullHolder = new ArrayList[1];
            final long[]             tiempos     = new long[2];

            // Mostrar pantalla de carga; el callback se ejecuta cuando barra llega al 100%
            SwingUtilities.invokeLater(() -> {
                pantallaEspera.setBounds(0, 0, panelDibujo.getWidth(), panelDibujo.getHeight());
                pantallaEspera.setVisible(true);
                pantallaEspera.iniciar(algoFinal, puntosFinales.size(), () -> {
                    // Este callback se ejecuta en el EDT cuando la barra llega al 100%
                    // En este punto el algoritmo ya terminó (marcarAlgoritmoTerminado fue llamado)
                    ArrayList<Point> hullFinal = hullHolder[0] != null ? hullHolder[0] : new ArrayList<>();
                    tiempo = (tiempos[1] - tiempos[0]) / 1_000_000.0;

                    pantallaEspera.detener();
                    pantallaEspera.setVisible(false);

                    panelDibujo.setDatos(puntosFinales, hullFinal);

                    Timer inicio = new Timer(50, ev -> {
                        ((Timer) ev.getSource()).stop();
                        panelDibujo.iniciarFadePoints(() ->
                            panelDibujo.animarHull(hullFinal, () -> {
                                labelTiempo.setText(String.format("%.2f ms", tiempo));
                                labelAlgoritmo.setText(algoFinal);
                                labelDistribucion.setText(distFinal != null ? distFinal : "File input");
                                labelNumPuntos.setText(String.format("%,d", puntosFinales.size()));
                                labelNumHull.setText(String.format("%,d", hullFinal.size()));
                                labelArchivo.setText(archivoFinal != null && useFileFinal
                                        ? archivoFinal.getName() : "Generated · no file");
                            })
                        );
                    });
                    inicio.setRepeats(false);
                    inicio.start();
                });
                panelDibujo.repaint();
            });

            // Ejecutar algoritmo en hilo de fondo
            SwingWorker<ArrayList<Point>, Void> worker = new SwingWorker<>() {
                @Override
                protected ArrayList<Point> doInBackground() {
                    tiempos[0] = System.nanoTime();
                    ArrayList<Point> resultado = algoritmoElegido(algoFinal, puntosFinales);
                    tiempos[1] = System.nanoTime();
                    return resultado;
                }

                @Override
                protected void done() {
                    try {
                        hullHolder[0] = get();
                    } catch (InterruptedException | ExecutionException ex) {
                        hullHolder[0] = new ArrayList<>();
                    }
                    if (hullHolder[0] == null) hullHolder[0] = new ArrayList<>();

                    // Avisar a la pantalla de carga que el algoritmo terminó.
                    // La barra completará el 100% cuando ADEMÁS pasen los 3 segundos.
                    SwingUtilities.invokeLater(() -> pantallaEspera.marcarAlgoritmoTerminado());
                }
            };
            worker.execute();
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
            case "Square":               return GenerarDatos.generarCuadrado(numPuntos);
            case "Circle":                return GenerarDatos.generarCirculo(numPuntos);
            case "Triangle":              return GenerarDatos.generarTriangulo(numPuntos);
            case "Diamond":                  return GenerarDatos.generarRombo(numPuntos);
            case "Horizontal straight line": return GenerarDatos.generarLineaRectaHorizontal(numPuntos);
            case "Vertical straight line":   return GenerarDatos.generarLineaRectaVertical(numPuntos);
            case "Gaussian":               return GenerarDatos.generarGaussiana(numPuntos);
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