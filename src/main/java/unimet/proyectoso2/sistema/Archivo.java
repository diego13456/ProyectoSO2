/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package unimet.proyectoso2.sistema;
import unimet.proyectoso2.disco.DiskBlock; // Asumiendo que DiskBlock está en el paquete disco
import unimet.proyectoso2.concurrencia.FileLock; // <--- Importar el Lock


/**
 * Representa un archivo lógico en el sistema.
 */
public class Archivo extends ElementoFS {
    private int tamanoEnBloques;
    private DiskBlock bloqueInicial;
     private final FileLock lock; 


    public Archivo(String nombre, String propietario, int tamanoEnBloques, DiskBlock bloqueInicial) {
        super(nombre, propietario);
        this.tamanoEnBloques = tamanoEnBloques;
        this.bloqueInicial = bloqueInicial;
        this.lock = new FileLock(); // <--- Inicializar el lock

    }
    public FileLock getLock() { return lock; }
    public int getTamanoEnBloques() { return tamanoEnBloques; }
    public void setTamanoEnBloques(int tamanoEnBloques) { this.tamanoEnBloques = tamanoEnBloques; }

    public DiskBlock getBloqueInicial() { return bloqueInicial; }
    public void setBloqueInicial(DiskBlock bloqueInicial) { this.bloqueInicial = bloqueInicial; }
    
    @Override
    public String toString() {
    return this.nombre; 
    }
}