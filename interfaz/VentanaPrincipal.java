package ConvexHull.interfaz;
import javax.swing.*;

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
        int tituloSize = Math.max(28, altoPantalla / 18);
        int textoGrande = Math.max(18, altoPantalla / 45);
        int textoNormal = Math.max(14, altoPantalla / 60);

        cardLayout = new CardLayout();
        contenedor = new JPanel(cardLayout);

        Dimension tamañoBoton = new Dimension(220, 45);

        // =========================
        // PANTALLA DE CONFIGURACIÓN
        // =========================
        JPanel fondo = new PanelConFondo("/ConvexHull/decoration/fondo.png");
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
                "Brute Force",
                "Jarvis",
                "QuickHull",
                "Graham Scan",
                "MergeHull"
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
        JPanel fondoSalida = new PanelConFondo("/ConvexHull/decoration/fondo10.png");
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

        JPanel panelResultados = new PanelTransparente(0.55f);
        panelResultados.setOpaque(false);
        panelResultados.setLayout(new BoxLayout(panelResultados, BoxLayout.Y_AXIS));
        panelResultados.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panelResultados.setPreferredSize(new Dimension(260, 0));
        panelResultados.setMinimumSize(new Dimension(220, 0));

        JLabel tituloResultados = new JLabel("Results:");
        tituloResultados.setFont(new Font("Segoe UI", Font.BOLD, textoGrande));
        tituloResultados.setAlignmentX(Component.LEFT_ALIGNMENT);
        tituloResultados.setForeground(Color.WHITE);

        labelTiempo = crearLabelResultado("Execution time: ", textoNormal);
        labelAlgoritmo = crearLabelResultado("Algorithm: ", textoNormal);
        labelArchivo = crearLabelResultado("File: ", textoNormal);
        labelDistribucion = crearLabelResultado("Distribution: ", textoNormal);
        labelNumPuntos = crearLabelResultado("Points: ", textoNormal);
        labelNumHull = crearLabelResultado("Hull points: ", textoNormal);

        panelResultados.add(tituloResultados);
        panelResultados.add(Box.createVerticalStrut(35));
        panelResultados.add(labelTiempo);
        panelResultados.add(Box.createVerticalStrut(25));
        panelResultados.add(labelAlgoritmo);
        panelResultados.add(Box.createVerticalStrut(25));
        panelResultados.add(labelArchivo);
        panelResultados.add(Box.createVerticalStrut(25));
        panelResultados.add(labelDistribucion);
        panelResultados.add(Box.createVerticalStrut(25));
        panelResultados.add(labelNumPuntos);
        panelResultados.add(Box.createVerticalStrut(25));
        panelResultados.add(labelNumHull);

        ImageIcon logoMarca = new ImageIcon(getClass().getResource("/ConvexHull/decoration/logoo.png"));
        Image imgEscalada = logoMarca.getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH);
        JLabel marcaAgua = new JLabel(new ImageIcon(imgEscalada));
        marcaAgua.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelResultados.add(Box.createVerticalGlue());
        panelResultados.add(Box.createVerticalStrut(20));
        panelResultados.add(marcaAgua);

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
                    "Cuadrado",
                    "Círculo",
                    "Triángulo",
                    "Rombo",
                    "Línea recta Horizontal",
                    "Línea recta Vertical",
                    "Línea recta Creciente",
                    "Línea recta Decreciente",
                    "Gausiana"
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

            if (hull == null) {
                hull = new ArrayList<>();
            }

            panelDibujo.setDatos(puntos, hull);
            cardLayout.show(contenedor, "salida");

            labelTiempo.setText("Time: " + tiempo + " ms");
            labelAlgoritmo.setText("Algorithm: " + algoElegido);
            labelDistribucion.setText("Distribution: " + (distElegida != null ? distElegida : "File input"));
            labelNumPuntos.setText("Points: " + puntos.size());
            labelNumHull.setText("Hull Points: " + hull.size());

            if (archivo != null && useFile) {
                labelArchivo.setText("File name: " + archivo.getName());
            } else {
                labelArchivo.setText("File name: No file");
            }
        });
    }

    private JLabel crearLabelResultado(String texto, int size) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.BOLD, size));
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    public ArrayList<Point> generarRandom(String dist) {
        if (dist.equals("Cuadrado")) {
            return GenerarDatos.generarCuadrado(numPuntos);
        } else if (dist.equals("Círculo")) {
            return GenerarDatos.generarCirculo(numPuntos);
        } else if (dist.equals("Triángulo")) {
            return GenerarDatos.generarTriangulo(numPuntos);
        } else if (dist.equals("Rombo")) {
            return GenerarDatos.generarRombo(numPuntos);
        } else if (dist.equals("Línea recta Horizontal")) {
            return GenerarDatos.generarLineaRectaHorizontal(numPuntos);
        } else if (dist.equals("Línea recta Vertical")) {
            return GenerarDatos.generarLineaRectaVertical(numPuntos);
        } else if (dist.equals("Línea recta Creciente")) {
            return GenerarDatos.generarLineaRectaCreciente(numPuntos);
        } else if (dist.equals("Línea recta Decreciente")) {
            return GenerarDatos.generarLineaRectaDecreciente(numPuntos);
        } else if (dist.equals("Gausiana")) {
            return GenerarDatos.generarGaussiana(numPuntos);
        } else {
            JOptionPane.showMessageDialog(this, "An error has occurred");
            return new ArrayList<>();
        }
    }

    public ArrayList<Point> algoritmoElegido(String algo, ArrayList<Point> puntos) {
        if (algo.equals("Brute Force")) {
            return FuerzaBruta.calcularHull(puntos);
        } else if (algo.equals("Jarvis")) {
            return JarvisMarch.calcularHull(puntos);
        } else if (algo.equals("QuickHull")) {
            return QuickHull.calcularHull(puntos);
        } else if (algo.equals("Graham Scan")) {
            return ConvexHull.GrahamScanConvexHull(puntos);
        } else if (algo.equals("MergeHull")) {
            return ConvexHull.computeMergeHull(puntos);
        } else {
            JOptionPane.showMessageDialog(this, "An error has occurred");
            return new ArrayList<>();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(VentanaPrincipal::new);
    }

    private static class PanelConFondo extends JPanel {
        private final Image imagen;

        public PanelConFondo(String ruta) {
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