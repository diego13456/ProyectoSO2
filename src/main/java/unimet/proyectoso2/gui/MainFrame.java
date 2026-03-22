/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package unimet.proyectoso2.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import unimet.proyectoso2.sistema.*;
import unimet.proyectoso2.procesos.*;
import unimet.proyectoso2.disco.*;
import unimet.proyectoso2.concurrencia.*;
import unimet.proyectoso2.estructuras.*;

public class MainFrame extends JFrame {

    // Componentes del Sistema
    private FileSystemManager fsManager;
    private SimuladorProcesos simulador;
    private Thread hiloSimulador;

    // Componentes de la Interfaz
    private JTree treeFileSystem;
    private JTable tableProcesos;
    private JTable tableFAT;
    private JPanel panelDisco;
    private JLabel[] etiquetasBloques;
    private JTextArea areaJournal;
    private JComboBox<String> comboPolitica;
    private JLabel lblModo;

    public MainFrame() {
        // 1. Inicializar Lógica
        this.fsManager = new FileSystemManager();
        this.simulador = new SimuladorProcesos(fsManager.getPlanificador(), fsManager.getDisco(), fsManager.getJournal());
        this.hiloSimulador = new Thread(simulador);
        this.hiloSimulador.start();

        // 2. Configurar Ventana
        setTitle("Simulador de Sistema de Archivos Concurrente");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setupUI(); // Construir la interfaz
        inicializarVisualizacionDisco();
        actualizarJTree(fsManager.getRoot());
        iniciarTimerRefresco(); // El reloj que actualiza la pantalla cada segundo

        setLocationRelativeTo(null);
    }

    private void setupUI() {
        // Panel Superior: Controles
        JPanel panelNorte = new JPanel();
        JButton btnCrear = new JButton("Nuevo Archivo");
        JButton btnNuevaCarpeta = new JButton("Nueva Carpeta"); 
        JButton btnRenombrar = new JButton("Renombrar");
        JButton btnGuardar = new JButton("GUARDAR ESTADO"); // Nuevo botón
        btnGuardar.setBackground(new Color(200, 255, 200)); // Color verde clarito
        JButton btnEliminar = new JButton("Eliminar Seleccionado");
        JButton btnFallo = new JButton("SIMULAR FALLO (CRASH)");
        JButton btnModo = new JButton("Cambiar Modo Admin/User");
        comboPolitica = new JComboBox<>(new String[]{"FIFO", "SSTF", "SCAN", "C-SCAN"});
        lblModo = new JLabel(" MODO: ADMINISTRADOR ");
        lblModo.setForeground(Color.RED);

        panelNorte.add(btnCrear);
        panelNorte.add(btnNuevaCarpeta); 
        panelNorte.add(btnRenombrar);    
        panelNorte.add(btnEliminar);
        panelNorte.add(btnGuardar);
        panelNorte.add(new JLabel(" Política:"));
        panelNorte.add(comboPolitica);
        panelNorte.add(btnFallo);
        panelNorte.add(btnModo);
        panelNorte.add(lblModo);
        add(panelNorte, BorderLayout.NORTH);

        // Panel Izquierdo: Arbol de Archivos
        treeFileSystem = new JTree();
        JScrollPane scrollTree = new JScrollPane(treeFileSystem);
        scrollTree.setPreferredSize(new Dimension(250, 0));
        add(scrollTree, BorderLayout.WEST);

        // Panel Central: Visualización del Disco y Tablas
        JPanel panelCentral = new JPanel(new GridLayout(2, 1));
        
        panelDisco = new JPanel(); // El GridLayout se pone en inicializarVisualizacionDisco
        JScrollPane scrollDisco = new JScrollPane(panelDisco);
        panelCentral.add(scrollDisco);

        // Tablas
        JPanel panelTablas = new JPanel(new GridLayout(1, 2));
        tableProcesos = new JTable(new DefaultTableModel(new Object[]{"ID", "Op", "Estado", "Bloque"}, 0));
        tableFAT = new JTable(new DefaultTableModel(new Object[]{"Archivo", "Bloques", "Inicio"}, 0));
        panelTablas.add(new JScrollPane(tableProcesos));
        panelTablas.add(new JScrollPane(tableFAT));
        panelCentral.add(panelTablas);
        add(panelCentral, BorderLayout.CENTER);

        // Panel Inferior: Journal
        areaJournal = new JTextArea(5, 0);
        areaJournal.setEditable(false);
        areaJournal.setBackground(Color.BLACK);
        areaJournal.setForeground(Color.GREEN);
        add(new JScrollPane(areaJournal), BorderLayout.SOUTH);

        // --- EVENTOS ---
        btnCrear.addActionListener(e -> menuCrearArchivo());
        comboPolitica.addActionListener(e -> {
        String seleccion = (String) comboPolitica.getSelectedItem();
        simulador.setPoliticaActual(seleccion);
        });
        btnNuevaCarpeta.addActionListener(e -> menuCrearCarpeta()); 
        btnRenombrar.addActionListener(e -> menuRenombrar());       
        btnEliminar.addActionListener(e -> ejecutarEliminacion());  
        btnFallo.addActionListener(e -> simularFallo());
        btnGuardar.addActionListener(e -> {fsManager.guardarEstado();
        JOptionPane.showMessageDialog(this, "¡Sistema de archivos guardado en JSON!");
        });
        btnModo.addActionListener(e -> cambiarModo());
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
    // Importante: Asegúrate de importar javax.swing.Timer
    Timer timer = new Timer(500, (ActionEvent e) -> {
        refrescarColoresDisco();    // Actualiza el cabezal rojo y bloques cian
        actualizarTablaProcesos();  // Muestra qué proceso está "Ejecutando"
        actualizarTablaFAT();       // Muestra los archivos y su bloque inicial
        actualizarJournalView();    // Muestra los logs en verde
    });
    timer.start();
}
    public void actualizarJTree(Directorio raiz) {
        DefaultMutableTreeNode nodoRaiz = new DefaultMutableTreeNode(raiz.getNombre());
        llenarNodoRecursivo(raiz, nodoRaiz);
        treeFileSystem.setModel(new DefaultTreeModel(nodoRaiz));
    }

   private void llenarNodoRecursivo(Directorio dirActual, DefaultMutableTreeNode nodoVisual) {
    // 1. Agregar Subdirectorios
    for (int i = 0; i < dirActual.getSubdirectorios().size(); i++) {
        Directorio sub = dirActual.getSubdirectorios().get(i);
        // GUARDAMOS EL OBJETO 'sub' EN EL NODO
        DefaultMutableTreeNode nuevoNodoDir = new DefaultMutableTreeNode(sub); 
        nodoVisual.add(nuevoNodoDir);
        llenarNodoRecursivo(sub, nuevoNodoDir);
    }
    
    // 2. Agregar Archivos
    for (int i = 0; i < dirActual.getArchivos().size(); i++) {
        Archivo arc = dirActual.getArchivos().get(i);
        // GUARDAMOS EL OBJETO 'arc' EN EL NODO
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
        modelo.addRow(new Object[]{
            p.getIdProceso(), 
            p.getOperacion(), 
            p.getEstado(), 
            p.getBloqueObjetivo()
        });
    }
}

    // Este limpia la tabla y lanza la búsqueda desde la raíz
    private void actualizarTablaFAT() {
    DefaultTableModel modelo = (DefaultTableModel) tableFAT.getModel();
    modelo.setRowCount(0); // Limpiar la tabla antes de volver a llenarla
    
    // Empezamos a buscar archivos desde la raíz "/"
    llenarTablaFATRecursivo(fsManager.getRoot(), modelo);
}

    // Este busca archivos en carpetas y subcarpetas (Recursividad)
   private void llenarTablaFATRecursivo(Directorio dirActual, DefaultTableModel modelo) {
    // 1. Archivos de la carpeta actual
    for (int i = 0; i < dirActual.getArchivos().size(); i++) {
        Archivo a = dirActual.getArchivos().get(i);
        
        // Lógica visual: Si aún no tiene bloque, mostramos un mensaje pendiente
        Object inicio = (a.getBloqueInicial() != null) 
                        ? a.getBloqueInicial().getId() 
                        : "Esperando disco..."; // Esto se verá mientras el proceso está en la cola
        
        modelo.addRow(new Object[]{
            a.getNombre(), 
            a.getTamanoEnBloques(), 
            inicio 
        });
    }

    // 2. Recursión para subcarpetas
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

    // --- EL CAMBIO ESTÁ AQUÍ ---
    Directorio destino = obtenerDirectorioSeleccionado();
    
    String nombre = JOptionPane.showInputDialog("Crear archivo en " + destino.getNombre() + ":");
    String tamStr = JOptionPane.showInputDialog("Tamaño (bloques):");

    if (nombre != null && tamStr != null) {
        int tam = Integer.parseInt(tamStr);
        
        // 1. Crear el objeto Archivo
        Archivo nuevo = new Archivo(nombre, "Admin", tam, null);
        
        // 2. Agregarlo a la carpeta DESTINO
        destino.agregarArchivo(nuevo); 
        
        // 3. Simular el proceso de I/O
        PCB pcb = new PCB((int)(Math.random()*1000), OperacionCRUD.CREAR, nuevo, (int)(Math.random()*199));
        fsManager.getPlanificador().agregarProceso(pcb);
        
        actualizarJTree(fsManager.getRoot());
        System.out.println("Archivo creado en la carpeta: " + destino.getNombre());
    }
}

    private void simularFallo() {
        simulador.detener();
        JOptionPane.showMessageDialog(this, "¡SISTEMA CAÍDO! Aplicando recuperación por Journal...");
        fsManager.getJournal().ejecutarRecuperacion(fsManager.getDisco());
        
        // Reiniciar simulador
        simulador = new SimuladorProcesos(fsManager.getPlanificador(), fsManager.getDisco(), fsManager.getJournal());
        hiloSimulador = new Thread(simulador);
        hiloSimulador.start();
    }

    private void cambiarModo() {
        fsManager.setModoAdmin(!fsManager.isModoAdmin());
        lblModo.setText(fsManager.isModoAdmin() ? " MODO: ADMINISTRADOR " : " MODO: USUARIO ");
        lblModo.setForeground(fsManager.isModoAdmin() ? Color.RED : Color.BLUE);
    }

    // 1. MÉTODO PARA CREAR CARPETA (CRUD - CREAR)
    private void menuCrearCarpeta() {
    if (!fsManager.isModoAdmin()) return;
    
    Directorio destino = obtenerDirectorioSeleccionado();
    String nombre = JOptionPane.showInputDialog("Nueva carpeta dentro de " + destino.getNombre() + ":");
    
    if (nombre != null && !nombre.isEmpty()) {
        Directorio nueva = new Directorio(nombre, "Admin");
        destino.agregarSubdirectorio(nueva); // <--- SE AGREGA AL DESTINO SELECCIONADO
        actualizarJTree(fsManager.getRoot());
    }
}

    // 2. MÉTODO PARA RENOMBRAR (CRUD - ACTUALIZAR)
    private void menuRenombrar() {
    // 1. Validar Permisos (Requerimiento 3 del PDF)
    if (!fsManager.isModoAdmin()) {
        JOptionPane.showMessageDialog(this, "Error: Solo los administradores pueden modificar el nombre.");
        return;
    }

    // 2. Obtener el nodo seleccionado en el JTree
    DefaultMutableTreeNode nodoSeleccionado = (DefaultMutableTreeNode) treeFileSystem.getLastSelectedPathComponent();
    
    if (nodoSeleccionado == null || nodoSeleccionado.isRoot()) {
        JOptionPane.showMessageDialog(this, "Por favor, seleccione un archivo o carpeta para renombrar.");
        return;
    }

    // 3. Obtener el OBJETO real que está dentro del nodo
    Object objetoRecuperado = nodoSeleccionado.getUserObject();
    String nombreActual = objetoRecuperado.toString();

    // 4. Pedir el nuevo nombre al usuario
    String nuevoNombre = JOptionPane.showInputDialog(this, "Cambiar nombre de '" + nombreActual + "' a:", nombreActual);

    if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
        
        // 5. ACTUALIZACIÓN LÓGICA: Cambiamos el nombre en el objeto (Archivo o Directorio)
        if (objetoRecuperado instanceof ElementoFS) {
            ElementoFS elemento = (ElementoFS) objetoRecuperado;
            elemento.setNombre(nuevoNombre); // Cambia el atributo 'nombre' internamente
            
            // 6. ACTUALIZACIÓN VISUAL: Notificar al modelo del árbol que el nodo cambió
            DefaultTreeModel modelo = (DefaultTreeModel) treeFileSystem.getModel();
            modelo.nodeChanged(nodoSeleccionado); 
            
            // 7. Refrescar otros componentes (como la tabla FAT)
            actualizarTablaFAT();
            
            System.out.println("Renombrado con éxito a: " + nuevoNombre);
        }
    }
}

// 3. MÉTODO PARA ELIMINAR (CRUD - ELIMINAR RECURSIVO)
    private void ejecutarEliminacion() {
    if (!fsManager.isModoAdmin()) {
        JOptionPane.showMessageDialog(this, "Error: Solo administradores pueden eliminar.");
        return;
    }

    // 1. Obtener el nodo seleccionado y su padre
    DefaultMutableTreeNode nodoSeleccionado = (DefaultMutableTreeNode) treeFileSystem.getLastSelectedPathComponent();
    
    if (nodoSeleccionado == null || nodoSeleccionado.isRoot()) {
        JOptionPane.showMessageDialog(this, "Seleccione un archivo o carpeta (no la raíz).");
        return;
    }

    DefaultMutableTreeNode nodoPadre = (DefaultMutableTreeNode) nodoSeleccionado.getParent();
    Directorio directorioPadre = (Directorio) nodoPadre.getUserObject();
    Object objetoAEliminar = nodoSeleccionado.getUserObject();

    // 2. Lógica según el tipo de objeto
    if (objetoAEliminar instanceof Archivo) {
        Archivo arc = (Archivo) objetoAEliminar;
        
        // A. Liberar físicamente en el disco (SD)
        if (arc.getBloqueInicial() != null) {
            fsManager.getDisco().freeBlocks(arc.getBloqueInicial());
        }
        
        // B. Quitar lógicamente de la lista de la carpeta padre
        directorioPadre.getArchivos().remove(arc);
        
        System.out.println("Archivo eliminado: " + arc.getNombre());

    } else if (objetoAEliminar instanceof Directorio) {
        Directorio dir = (Directorio) objetoAEliminar;
        
        // A. Liberar recursivamente todos los archivos dentro de esta carpeta
        eliminarContenidoDiscoRecursivo(dir);
        
        // B. Quitar la carpeta de la lista de subdirectorios del padre
        directorioPadre.getSubdirectorios().remove(dir);
        
        System.out.println("Directorio eliminado: " + dir.getNombre());
    }

    // 3. ACTUALIZAR TODO
    actualizarJTree(fsManager.getRoot());
    refrescarColoresDisco();
    actualizarTablaFAT();
}
    
   private void eliminarContenidoDiscoRecursivo(Directorio dir) {
    // Liberar bloques de los archivos de esta carpeta
    for (int i = 0; i < dir.getArchivos().size(); i++) {
        Archivo arc = dir.getArchivos().get(i);
        if (arc.getBloqueInicial() != null) {
            fsManager.getDisco().freeBlocks(arc.getBloqueInicial());
        }
    }
    
    // Hacer lo mismo con las subcarpetas (recursión)
    for (int i = 0; i < dir.getSubdirectorios().size(); i++) {
        eliminarContenidoDiscoRecursivo(dir.getSubdirectorios().get(i));
    }
}
   
    
    private Directorio obtenerDirectorioSeleccionado() {
    DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) treeFileSystem.getLastSelectedPathComponent();
    
    if (nodo == null) return fsManager.getRoot(); // Si no hay nada, usar la raíz

    Object userObject = nodo.getUserObject();
    
    if (userObject instanceof Directorio) {
        return (Directorio) userObject;
    } else if (userObject instanceof Archivo) {
        // Si seleccionaste un archivo, podrías devolver su carpeta padre 
        // pero por simplicidad, buscaremos el nodo padre:
        DefaultMutableTreeNode padre = (DefaultMutableTreeNode) nodo.getParent();
        if (padre != null && padre.getUserObject() instanceof Directorio) {
            return (Directorio) padre.getUserObject();
        }
    }
    
    return fsManager.getRoot();
}
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        java.awt.EventQueue.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
    
    private void refrescarColoresDisco() {
    VirtualDisk disco = fsManager.getDisco();
    // Obtenemos la posición actual del cabezal desde el planificador
    int posCabezal = fsManager.getPlanificador().getCabezalActual();

    for (int i = 0; i < disco.getTotalBlocks(); i++) {
        // 1. Si es la posición del cabezal, lo pintamos de ROJO (o Naranja)
        if (i == posCabezal) {
            etiquetasBloques[i].setBackground(Color.RED);
            etiquetasBloques[i].setForeground(Color.WHITE); // Texto blanco para que se vea
        } 
        // 2. Si el bloque está libre, BLANCO
        else if (disco.getBlocks()[i].isFree()) {
            etiquetasBloques[i].setBackground(Color.WHITE);
            etiquetasBloques[i].setForeground(Color.BLACK);
        } 
        // 3. Si está ocupado por un archivo, CIAN
        else {
            etiquetasBloques[i].setBackground(Color.CYAN);
            etiquetasBloques[i].setForeground(Color.BLACK);
        }
    }
    }
}