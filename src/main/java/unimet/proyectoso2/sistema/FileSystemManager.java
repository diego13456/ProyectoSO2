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
    private boolean modoAdministrador = true; // Requerimiento 5

    public FileSystemManager() {
        this.root = new Directorio("/", "Admin");
        this.disco = new VirtualDisk(200); // Tamaño ejemplo de 200 bloques
        this.planificador = new PlanificadorDisco(50, 1, 200);
        this.journal = new JournalManager();
    }

    // Getters para que la GUI pueda acceder a ellos
    public Directorio getRoot() { return root; }
    public VirtualDisk getDisco() { return disco; }
    public PlanificadorDisco getPlanificador() { return planificador; }
    public JournalManager getJournal() { return journal; }
    public boolean isModoAdmin() { return modoAdministrador; }
    public void setModoAdmin(boolean modo) { this.modoAdministrador = modo; }
}