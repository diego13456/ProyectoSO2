/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package unimet.proyectoso2.sistema;

import unimet.proyectoso2.disco.VirtualDisk;
import unimet.proyectoso2.procesos.PlanificadorDisco;
import unimet.proyectoso2.concurrencia.JournalManager;

public class FileSystemManager {
    private Directorio root;
    private VirtualDisk disco;
    private PlanificadorDisco planificador;
    private JournalManager journal;
    private boolean modoAdministrador = true;

    public FileSystemManager() {
        // 1. Intentar cargar el sistema desde el JSON
        Directorio cargado = PersistenceManager.cargar();
        
        if (cargado != null) {
            this.root = cargado;
        } else {
            // Si no hay archivo, crear raíz por defecto
            this.root = new Directorio("/", "Admin");
        }

        // 2. Inicializar los componentes físicos y de control
        this.disco = new VirtualDisk(200); // 200 bloques
        this.planificador = new PlanificadorDisco(50, 1, 200); // Cabezal en 50
        this.journal = new JournalManager();
    }

    // --- ESTOS SON LOS MÉTODOS QUE NECESITA EL MAINFRAME PARA NO ESTAR EN ROJO ---
    public Directorio getRoot() { return root; }
    public VirtualDisk getDisco() { return disco; }
    public PlanificadorDisco getPlanificador() { return planificador; }
    public JournalManager getJournal() { return journal; }
    public boolean isModoAdmin() { return modoAdministrador; }
    public void setModoAdmin(boolean modo) { this.modoAdministrador = modo; }

    // Método para guardar manualmente
    public void guardarEstado() {
        PersistenceManager.guardar(this.root);
    }
}