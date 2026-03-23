package unimet.proyectoso2.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;

import unimet.proyectoso2.sistema.*;
import unimet.proyectoso2.procesos.*;
import unimet.proyectoso2.disco.*;
import unimet.proyectoso2.concurrencia.*;
import unimet.proyectoso2.estructuras.*;

public class MainFrame extends JFrame {

    private FileSystemManager fsManager;
    private SimuladorProcesos simulador;
    private Thread hiloSimulador;

    private JTree treeFileSystem;
    private JTable tableProcesos;
    private JTable tableFAT;
    private JPanel panelDisco;
    private JLabel[] etiquetasBloques;
    private JTextArea areaJournal;
    private JComboBox<String> comboPolitica;
    private JLabel lblModo;

    public MainFrame() {
        this.fsManager = new FileSystemManager();
        this.simulador = new SimuladorProcesos(fsManager.getPlanificador(), fsManager.getDisco(), fsManager.getJournal());
        this.hiloSimulador = new Thread(simulador);
        this.hiloSimulador.start();

        setTitle("Simulador de Sistema de Archivos Concurrente");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setupUI();
        inicializarVisualizacionDisco();
        actualizarJTree(fsManager.getRoot());
        iniciarTimerRefresco();

        setLocationRelativeTo(null);
    }

    private void setupUI() {
        JPanel panelNorte = new JPanel();
        JButton btnCrear = new JButton("Nuevo Archivo");
        JButton btnNuevaCarpeta = new JButton("Nueva Carpeta"); 
        JButton btnRenombrar = new JButton("Renombrar");
        JButton btnGuardar = new JButton("GUARDAR ESTADO"); 
        btnGuardar.setBackground(new Color(200, 255, 200)); 
        JButton btnEliminar = new JButton("Eliminar Seleccionado");
        
        JButton btnLeer = new JButton("Leer Archivo");
        btnLeer.setBackground(new Color(173, 216, 230)); 
        btnLeer.addActionListener(e -> menuLeerArchivo());
        
        JButton btnFallo = new JButton("SIMULAR FALLO (CRASH)");
        JButton btnModo = new JButton("Cambiar Modo Admin/User");
        comboPolitica = new JComboBox<>(new String[]{"FIFO", "SSTF", "SCAN", "C-SCAN"});
        lblModo = new JLabel(" MODO: ADMINISTRADOR ");
        lblModo.setForeground(Color.RED);
        
        JButton btnCargarJSON = new JButton("Cargar Prueba PDF");
        btnCargarJSON.setBackground(new Color(255, 200, 255));
        
        JButton btnPlayPause = new JButton("▶ INICIAR / PAUSAR");
        btnPlayPause.setBackground(Color.GREEN);

        panelNorte.add(btnCargarJSON);
        panelNorte.add(btnPlayPause);

        btnCargarJSON.addActionListener(e -> {
            simulador.pausar();
            btnPlayPause.setBackground(Color.YELLOW);
            btnPlayPause.setText("▶ REANUDAR");

            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                String ruta = fileChooser.getSelectedFile().getAbsolutePath();
                TestConfig config = PersistenceManager.cargarJsonPrueba(ruta);
                
                if (config != null) {
                    fsManager.aplicarPrueba(config);
                    actualizarJTree(fsManager.getRoot());
                    JOptionPane.showMessageDialog(this, "JSON Cargado exitosamente.\n"
                            );
                } else {
                    JOptionPane.showMessageDialog(this, "Error al cargar el archivo JSON.");
                }
            }
        });

        btnPlayPause.addActionListener(e -> {
            if (simulador.isPausado()) {
                simulador.reanudar();
                btnPlayPause.setBackground(Color.RED);
                btnPlayPause.setText("⏸ PAUSAR");
            } else {
                simulador.pausar();
                btnPlayPause.setBackground(Color.GREEN);
                btnPlayPause.setText("▶ REANUDAR");
            }
        });

        panelNorte.add(btnCrear);
        panelNorte.add(btnNuevaCarpeta); 
        panelNorte.add(btnLeer);
        panelNorte.add(btnRenombrar);    
        panelNorte.add(btnEliminar);
        panelNorte.add(btnGuardar);
        panelNorte.add(new JLabel(" Política:"));
        panelNorte.add(comboPolitica);
        panelNorte.add(btnFallo);
        panelNorte.add(btnModo);
        panelNorte.add(lblModo);
        add(panelNorte, BorderLayout.NORTH);

        treeFileSystem = new JTree();
        
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value,
                    boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                
                if (value instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) value;
                    Object objetoUsuario = nodo.getUserObject();
                    
                    if (objetoUsuario instanceof Directorio) {
                        setIcon(expanded ? getDefaultOpenIcon() : getDefaultClosedIcon());
                    } else if (objetoUsuario instanceof Archivo) {
                        setIcon(getLeafIcon());
                    }
                }
                return this;
            }
        };
        treeFileSystem.setCellRenderer(renderer);

        JScrollPane scrollTree = new JScrollPane(treeFileSystem);
        scrollTree.setPreferredSize(new Dimension(250, 0));
        add(scrollTree, BorderLayout.WEST); 

        JPanel panelCentral = new JPanel(new GridLayout(2, 1));
        
        panelDisco = new JPanel(); 
        JScrollPane scrollDisco = new JScrollPane(panelDisco);
        panelCentral.add(scrollDisco);

        JPanel panelTablas = new JPanel(new GridLayout(1, 2));
        tableProcesos = new JTable(new DefaultTableModel(new Object[]{"ID", "Op", "Estado", "Bloque", "Archivo"}, 0));
        tableFAT = new JTable(new DefaultTableModel(new Object[]{"Archivo", "Bloques", "Inicio"}, 0));
        
        panelTablas.add(new JScrollPane(tableProcesos));
        panelTablas.add(new JScrollPane(tableFAT));
        panelCentral.add(panelTablas);
        add(panelCentral, BorderLayout.CENTER);

        areaJournal = new JTextArea(5, 0);
        areaJournal.setEditable(false);
        areaJournal.setBackground(Color.BLACK);
        areaJournal.setForeground(Color.GREEN);
        add(new JScrollPane(areaJournal), BorderLayout.SOUTH);

        btnCrear.addActionListener(e -> menuCrearArchivo());
        comboPolitica.addActionListener(e -> {
            String seleccion = (String) comboPolitica.getSelectedItem();
            simulador.setPoliticaActual(seleccion);
        });
        btnNuevaCarpeta.addActionListener(e -> menuCrearCarpeta()); 
        btnRenombrar.addActionListener(e -> menuRenombrar());       
        btnEliminar.addActionListener(e -> ejecutarEliminacion());  
        btnFallo.addActionListener(e -> simularFallo());
        btnGuardar.addActionListener(e -> {
            fsManager.guardarEstado();
            JOptionPane.showMessageDialog(this, "¡Sistema de archivos guardado en JSON!");
        });
        btnModo.addActionListener(e -> cambiarModo());
    }
    
    private void menuLeerArchivo() {
        DefaultMutableTreeNode nodoSeleccionado = (DefaultMutableTreeNode) treeFileSystem.getLastSelectedPathComponent();
        
        if (nodoSeleccionado == null || nodoSeleccionado.isRoot()) {
            JOptionPane.showMessageDialog(this, "Seleccione un archivo para leer.");
            return;
        }

        Object objetoSeleccionado = nodoSeleccionado.getUserObject();

        if (objetoSeleccionado instanceof Archivo) {
            Archivo arc = (Archivo) objetoSeleccionado;
            
            if (arc.getBloqueInicial() == null) {
                JOptionPane.showMessageDialog(this, "El archivo aún no tiene bloques asignados en el disco (creación pendiente en cola).");
                return;
            }

            int bloqueObjetivo = arc.getBloqueInicial().getId();
            int pcbId = (int)(Math.random() * 1000); 
            
            PCB procesoLectura = new PCB(pcbId, OperacionCRUD.LEER, arc, bloqueObjetivo);
            fsManager.getPlanificador().agregarProceso(procesoLectura);
            System.out.println("Enviada solicitud de LECTURA para el archivo: " + arc.getNombre());
            
        } else {
            JOptionPane.showMessageDialog(this, "Seleccionó una carpeta. Solo se pueden leer archivos.");
        }
    }
    
    private void inicializarVisualizacionDisco() {
        int total = fsManager.getDisco().getTotalBlocks();
        panelDisco.setLayout(new GridLayout(0, 20, 2, 2));
        etiquetasBloques = new JLabel[total];
        for (int i = 0; i < total; i++) {
            JLabel l = new JLabel(String.valueOf(i), JLabel.CENTER);
            l.setOpaque(true);
            l.setBackground(Color.WHITE);
            l.setFont(new Font("Arial", Font.PLAIN, 9));
            l.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            etiquetasBloques[i] = l;
            panelDisco.add(l);
        }
    }

    private void iniciarTimerRefresco() {
        Timer timer = new Timer(100, (ActionEvent e) -> {
            verificarArchivosEliminados();
            refrescarColoresDisco();    
            actualizarTablaProcesos();  
            actualizarTablaFAT();       
            actualizarJournalView();    
        });
        timer.start();
    }
    
    private void verificarArchivosEliminados() {
        boolean[] huboCambios = {false};
        limpiarEliminadosRecursivo(fsManager.getRoot(), huboCambios);
        if (huboCambios[0]) {
            actualizarJTree(fsManager.getRoot());
        }
    }

    private void limpiarEliminadosRecursivo(Directorio dir, boolean[] huboCambios) {
        for (int i = 0; i < dir.getArchivos().size(); i++) {
            Archivo a = dir.getArchivos().get(i);
            if (a.getTamanoEnBloques() == -1) { 
                dir.getArchivos().remove(a);
                huboCambios[0] = true;
                i--;
            }
        }
        for (int i = 0; i < dir.getSubdirectorios().size(); i++) {
            limpiarEliminadosRecursivo(dir.getSubdirectorios().get(i), huboCambios);
        }
    }

    public void actualizarJTree(Directorio raiz) {
        DefaultMutableTreeNode nodoRaiz = new DefaultMutableTreeNode(raiz);
        llenarNodoRecursivo(raiz, nodoRaiz);
        treeFileSystem.setModel(new DefaultTreeModel(nodoRaiz));
        
        for (int i = 0; i < treeFileSystem.getRowCount(); i++) {
            treeFileSystem.expandRow(i);
        }
    }

    private void llenarNodoRecursivo(Directorio dirActual, DefaultMutableTreeNode nodoVisual) {
        for (int i = 0; i < dirActual.getSubdirectorios().size(); i++) {
            Directorio sub = dirActual.getSubdirectorios().get(i);
            DefaultMutableTreeNode nuevoNodoDir = new DefaultMutableTreeNode(sub); 
            nodoVisual.add(nuevoNodoDir);
            llenarNodoRecursivo(sub, nuevoNodoDir);
        }
        
        for (int i = 0; i < dirActual.getArchivos().size(); i++) {
            Archivo arc = dirActual.getArchivos().get(i);
            DefaultMutableTreeNode nodoArchivo = new DefaultMutableTreeNode(arc);
            nodoVisual.add(nodoArchivo);
        }
    }

    private void actualizarTablaProcesos() {
        DefaultTableModel modelo = (DefaultTableModel) tableProcesos.getModel();
        modelo.setRowCount(0);
        
        LinkedList<PCB> procesos = fsManager.getPlanificador().getColaProcesos();
        
        for (int i = 0; i < procesos.size(); i++) {
            PCB p = procesos.get(i);
            
            String nombreArchivo = (p.getArchivoObjetivo() != null) ? p.getArchivoObjetivo().getNombre() : "N/A";
            
            modelo.addRow(new Object[]{
                p.getIdProceso(), 
                p.getOperacion().toString(), 
                p.getEstado().toString(), 
                p.getBloqueObjetivo(),
                nombreArchivo
            });
        }
    }

    private void actualizarTablaFAT() {
        DefaultTableModel modelo = (DefaultTableModel) tableFAT.getModel();
        modelo.setRowCount(0); 
        llenarTablaFATRecursivo(fsManager.getRoot(), modelo);
    }

    private void llenarTablaFATRecursivo(Directorio dirActual, DefaultTableModel modelo) {
        for (int i = 0; i < dirActual.getArchivos().size(); i++) {
            Archivo a = dirActual.getArchivos().get(i);
            Object inicio = (a.getBloqueInicial() != null) ? a.getBloqueInicial().getId() : "Esperando..."; 
            
            modelo.addRow(new Object[]{
                a.getNombre(), 
                a.getTamanoEnBloques(), 
                inicio 
            });
        }

        for (int i = 0; i < dirActual.getSubdirectorios().size(); i++) {
            llenarTablaFATRecursivo(dirActual.getSubdirectorios().get(i), modelo);
        }
    }

    private void actualizarJournalView() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- LOG DEL SISTEMA (JOURNAL) ---\n");
        for (int i = 0; i < fsManager.getJournal().getTransacciones().size(); i++) {
            JournalManager.LogEntry t = fsManager.getJournal().getTransacciones().get(i);
            sb.append("[").append(t.estado).append("] ").append(t.operacion).append(": ").append(t.nombreArchivo).append("\n");
        }
        areaJournal.setText(sb.toString());
    }

    private void menuCrearArchivo() {
        if (!fsManager.isModoAdmin()) {
            JOptionPane.showMessageDialog(this, "Error: Acceso denegado.");
            return;
        }

        Directorio destino = obtenerDirectorioSeleccionado();
        String nombre = JOptionPane.showInputDialog("Crear archivo en " + destino.getNombre() + ":");
        String tamStr = JOptionPane.showInputDialog("Tamaño (bloques):");

        if (nombre != null && tamStr != null && !tamStr.trim().isEmpty()) {
            try {
                int tam = Integer.parseInt(tamStr);
                int libres = fsManager.getDisco().getFreeBlocksCount();
                int maxContiguo = fsManager.getDisco().getLargestContiguousFreeSpace();
                
                if (tam <= 0) {
                    JOptionPane.showMessageDialog(this, "Error: El tamaño debe ser mayor a 0 bloques.");
                    return;
                }
                
                if (tam > maxContiguo) {
                    JOptionPane.showMessageDialog(this, 
                        "Error de I/O: Espacio fragmentado o insuficiente.\n\n" +
                        "Bloques libres totales: " + libres + "\n" +
                        "Mayor hueco contiguo disponible: " + maxContiguo + " bloques.\n" +
                        "Bloques requeridos: " + tam, 
                        "Disco Lleno / Fragmentado", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Archivo nuevo = new Archivo(nombre, "Admin", tam, null);
                destino.agregarArchivo(nuevo); 
                
                PCB pcb = new PCB((int)(Math.random()*1000), OperacionCRUD.CREAR, nuevo, (int)(Math.random()*199));
                fsManager.getPlanificador().agregarProceso(pcb);
                
                actualizarJTree(fsManager.getRoot());
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Error: Por favor ingrese un número válido para el tamaño.");
            }
        }
    }

    private void simularFallo() {
        boolean estabaPausado = simulador.isPausado();
        String politica = (String) comboPolitica.getSelectedItem();
        
        simulador.detener();
        JOptionPane.showMessageDialog(this, "¡SISTEMA CAÍDO! (CRASH)\nSe interrumpieron los procesos. Aplicando recuperación por Journal...");
        
        fsManager.getPlanificador().vaciarColaPorCrash();
        
        fsManager.getJournal().ejecutarRecuperacion(fsManager.getDisco());
        fsManager.limpiarArchivosFallidos(fsManager.getRoot()); 
        
        simulador = new SimuladorProcesos(fsManager.getPlanificador(), fsManager.getDisco(), fsManager.getJournal());
        simulador.setPoliticaActual(politica);
        
        if (!estabaPausado) {
            simulador.reanudar(); 
        }
        
        hiloSimulador = new Thread(simulador);
        hiloSimulador.start();
        
        actualizarJTree(fsManager.getRoot());
        actualizarTablaFAT();
        actualizarTablaProcesos();
    }

    private void cambiarModo() {
        fsManager.setModoAdmin(!fsManager.isModoAdmin());
        lblModo.setText(fsManager.isModoAdmin() ? " MODO: ADMINISTRADOR " : " MODO: USUARIO ");
        lblModo.setForeground(fsManager.isModoAdmin() ? Color.RED : Color.BLUE);
    }

    private void menuCrearCarpeta() {
        if (!fsManager.isModoAdmin()) return;
        
        Directorio destino = obtenerDirectorioSeleccionado();
        String nombre = JOptionPane.showInputDialog("Nueva carpeta dentro de " + destino.getNombre() + ":");
        
        if (nombre != null && !nombre.isEmpty()) {
            Directorio nueva = new Directorio(nombre, "Admin");
            destino.agregarSubdirectorio(nueva); 
            actualizarJTree(fsManager.getRoot());
        }
    }

    private void menuRenombrar() {
        if (!fsManager.isModoAdmin()) {
            JOptionPane.showMessageDialog(this, "Error: Solo los administradores pueden modificar el nombre.");
            return;
        }

        DefaultMutableTreeNode nodoSeleccionado = (DefaultMutableTreeNode) treeFileSystem.getLastSelectedPathComponent();
        
        if (nodoSeleccionado == null || nodoSeleccionado.isRoot()) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un archivo o carpeta para renombrar.");
            return;
        }

        Object objetoRecuperado = nodoSeleccionado.getUserObject();
        String nombreActual = objetoRecuperado.toString();
        String nuevoNombre = JOptionPane.showInputDialog(this, "Cambiar nombre de '" + nombreActual + "' a:", nombreActual);

        if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
            if (objetoRecuperado instanceof ElementoFS) {
                ElementoFS elemento = (ElementoFS) objetoRecuperado;
                elemento.setNombre(nuevoNombre); 
                
                DefaultTreeModel modelo = (DefaultTreeModel) treeFileSystem.getModel();
                modelo.nodeChanged(nodoSeleccionado); 
                actualizarTablaFAT();
            }
        }
    }

    private void ejecutarEliminacion() {
        if (!fsManager.isModoAdmin()) {
            JOptionPane.showMessageDialog(this, "Error: Solo administradores pueden eliminar.");
            return;
        }

        DefaultMutableTreeNode nodoSeleccionado = (DefaultMutableTreeNode) treeFileSystem.getLastSelectedPathComponent();
        
        if (nodoSeleccionado == null || nodoSeleccionado.isRoot()) {
            JOptionPane.showMessageDialog(this, "Seleccione un archivo o carpeta (no la raíz).");
            return;
        }

        DefaultMutableTreeNode nodoPadre = (DefaultMutableTreeNode) nodoSeleccionado.getParent();
        if (!(nodoPadre.getUserObject() instanceof Directorio)) return;
        
        Directorio directorioPadre = (Directorio) nodoPadre.getUserObject();
        Object objetoAEliminar = nodoSeleccionado.getUserObject();

        if (objetoAEliminar instanceof Archivo) {
            Archivo arc = (Archivo) objetoAEliminar;
            int bloqueObjetivo = (arc.getBloqueInicial() != null) ? arc.getBloqueInicial().getId() : 0;
            
            PCB proceso = new PCB((int)(Math.random()*1000), OperacionCRUD.ELIMINAR, arc, bloqueObjetivo);
            fsManager.getPlanificador().agregarProceso(proceso);
            directorioPadre.getArchivos().remove(arc);

        } else if (objetoAEliminar instanceof Directorio) {
            Directorio dir = (Directorio) objetoAEliminar;
            eliminarContenidoDiscoRecursivo(dir);
            directorioPadre.getSubdirectorios().remove(dir);
        }

        actualizarJTree(fsManager.getRoot());
    }
    
    private void eliminarContenidoDiscoRecursivo(Directorio dir) {
        for (int i = 0; i < dir.getArchivos().size(); i++) {
            Archivo arc = dir.getArchivos().get(i);
            int bloqueObjetivo = (arc.getBloqueInicial() != null) ? arc.getBloqueInicial().getId() : 0;
            PCB proceso = new PCB((int)(Math.random()*1000), OperacionCRUD.ELIMINAR, arc, bloqueObjetivo);
            fsManager.getPlanificador().agregarProceso(proceso);
        }
        
        for (int i = 0; i < dir.getSubdirectorios().size(); i++) {
            eliminarContenidoDiscoRecursivo(dir.getSubdirectorios().get(i));
        }
    }
    
    private Directorio obtenerDirectorioSeleccionado() {
        DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) treeFileSystem.getLastSelectedPathComponent();
        if (nodo == null) return fsManager.getRoot(); 

        Object userObject = nodo.getUserObject();
        if (userObject instanceof Directorio) {
            return (Directorio) userObject;
        } else if (userObject instanceof Archivo) {
            DefaultMutableTreeNode padre = (DefaultMutableTreeNode) nodo.getParent();
            if (padre != null && padre.getUserObject() instanceof Directorio) {
                return (Directorio) padre.getUserObject();
            }
        }
        return fsManager.getRoot();
    }

    private void refrescarColoresDisco() {
        VirtualDisk disco = fsManager.getDisco();
        int posCabezal = fsManager.getPlanificador().getCabezalActual();

        for (int i = 0; i < disco.getTotalBlocks(); i++) {
            if (i == posCabezal) {
                etiquetasBloques[i].setBackground(Color.RED);
                etiquetasBloques[i].setForeground(Color.WHITE); 
            } else if (disco.getBlocks()[i].isFree()) {
                etiquetasBloques[i].setBackground(Color.WHITE);
                etiquetasBloques[i].setForeground(Color.BLACK);
            } else {
                etiquetasBloques[i].setBackground(Color.CYAN);
                etiquetasBloques[i].setForeground(Color.BLACK);
            }
        }
    }
}