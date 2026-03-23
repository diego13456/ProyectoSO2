/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package unimet.proyectoso2.sistema;
import unimet.proyectoso2.disco.DiskBlock;
import unimet.proyectoso2.concurrencia.FileLock;


public class Archivo extends ElementoFS {
    private int tamanoEnBloques;
    private DiskBlock bloqueInicial;
     private transient FileLock lock; 


    public Archivo(String nombre, String propietario, int tamanoEnBloques, DiskBlock bloqueInicial) {
        super(nombre, propietario);
        this.tamanoEnBloques = tamanoEnBloques;
        this.bloqueInicial = bloqueInicial;
        this.lock = new FileLock(); 

    }
    public FileLock getLock() { 
    if (this.lock == null) { this.lock = new FileLock(); }
    return lock; 
}
    public int getTamanoEnBloques() { return tamanoEnBloques; }
    public void setTamanoEnBloques(int tamanoEnBloques) { this.tamanoEnBloques = tamanoEnBloques; }

    public DiskBlock getBloqueInicial() { return bloqueInicial; }
    public void setBloqueInicial(DiskBlock bloqueInicial) { this.bloqueInicial = bloqueInicial; }
    
    @Override
    public String toString() {
    return this.nombre + " (bloques: " + this.tamanoEnBloques + ")";

    }
}