/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package unimet.proyectoso2.sistema;

import unimet.proyectoso2.disco.VirtualDisk;
import unimet.proyectoso2.procesos.PlanificadorDisco;
import unimet.proyectoso2.concurrencia.JournalManager;
import unimet.proyectoso2.sistema.Directorio;
import unimet.proyectoso2.sistema.Archivo;
import unimet.proyectoso2.procesos.PCB;
import unimet.proyectoso2.disco.DiskBlock;
import unimet.proyectoso2.procesos.OperacionCRUD;

public class FileSystemManager {
    private Directorio root;
    private VirtualDisk disco;
    private PlanificadorDisco planificador;
    private JournalManager journal;
    private boolean modoAdministrador = true;

    public FileSystemManager() {
        this.disco = new VirtualDisk(256); 
        this.planificador = new PlanificadorDisco(50, 1, 256);
        this.journal = new JournalManager();
        
        Directorio cargado = PersistenceManager.cargar();
        if (cargado != null) {
            this.root = cargado;
            reconstruirDisco(this.root); 
        } else {
            this.root = new Directorio("/", "Admin");
        }
    }

    private void reconstruirDisco(Directorio dir) {
        dir.getArchivos().repairList(); 
        dir.getSubdirectorios().repairList();
        
        for (int i = 0; i < dir.getArchivos().size(); i++) {
            Archivo a = dir.getArchivos().get(i);
            if (a.getBloqueInicial() != null) {
                DiskBlock currentLoaded = a.getBloqueInicial();
                DiskBlock actualFirstBlock = null;
                DiskBlock currentActual = null;
                
                while (currentLoaded != null) {
                    int id = currentLoaded.getId();
                    if (id >= 0 && id < disco.getTotalBlocks()) {
                        disco.getBlocks()[id].setFree(false);
                        disco.decrementarBloquesLibres();
                        
                        DiskBlock realBlock = disco.getBlocks()[id];
                        if (actualFirstBlock == null) {
                            actualFirstBlock = realBlock;
                            currentActual = realBlock;
                        } else {
                            currentActual.setNextBlock(realBlock);
                            currentActual = realBlock;
                        }
                    }
                    currentLoaded = currentLoaded.getNextBlock();
                }
                a.setBloqueInicial(actualFirstBlock);
            }
        }
        for (int i = 0; i < dir.getSubdirectorios().size(); i++) {
            reconstruirDisco(dir.getSubdirectorios().get(i));
        }
    }
    
     public void limpiarArchivosFallidos(Directorio dir) {
        for (int i = 0; i < dir.getArchivos().size(); i++) {
            Archivo a = dir.getArchivos().get(i);
            if (a.getTamanoEnBloques() > 0 && a.getBloqueInicial() == null) {
                dir.getArchivos().remove(a);
                i--;
            }
        }
        for (int i = 0; i < dir.getSubdirectorios().size(); i++) {
            limpiarArchivosFallidos(dir.getSubdirectorios().get(i));
        }
    }

    public Directorio getRoot() { return root; }
    public VirtualDisk getDisco() { return disco; }
    public PlanificadorDisco getPlanificador() { return planificador; }
    public JournalManager getJournal() { return journal; }
    public boolean isModoAdmin() { return modoAdministrador; }
    public void setModoAdmin(boolean modo) { this.modoAdministrador = modo; }

    public void guardarEstado() {
        PersistenceManager.guardar(this.root);
    }
    
    public void eliminarDirectorioEnCascada(Directorio dirAEliminar, Directorio directorioPadre) {
    
    for (int i = 0; i < dirAEliminar.getArchivos().size(); i++) {
        Archivo arch = dirAEliminar.getArchivos().get(i);
        
        int bloqueObjetivo = (arch.getBloqueInicial() != null) ? arch.getBloqueInicial().getId() : 0;
        
        int pcbId = (int)(Math.random() * 1000); 
        
        PCB procesoEliminar = new PCB(pcbId, OperacionCRUD.ELIMINAR, arch, bloqueObjetivo);
        planificador.agregarProceso(procesoEliminar);
    }
    
    for (int i = 0; i < dirAEliminar.getSubdirectorios().size(); i++) {
        Directorio subDir = dirAEliminar.getSubdirectorios().get(i);
        eliminarDirectorioEnCascada(subDir, dirAEliminar);
    }
    
    if (directorioPadre != null) {
        directorioPadre.getSubdirectorios().remove(dirAEliminar);
    }
}
    
    
    public void aplicarPrueba(TestConfig config) {
        planificador.setCabezalActual(config.initial_head);

        if (config.system_files != null) {
            for (String key : config.system_files.keySet()) {
                int pos = Integer.parseInt(key);
                TestConfig.SysFile sf = config.system_files.get(key);
                
                Archivo arc = new Archivo(sf.name, "Admin", sf.blocks, null);
                DiskBlock bloque = disco.allocateBlocksAt(pos, sf.blocks);
                arc.setBloqueInicial(bloque);
                root.agregarArchivo(arc);
            }
        }

        if (config.requests != null) {
            for (TestConfig.Request req : config.requests) {
                Archivo target = buscarArchivoPorBloque(req.pos, root);
                OperacionCRUD op = mapearOperacion(req.op);
                
                if (target == null) target = new Archivo("Archivo_Temp_" + req.pos, "Admin", 1, disco.getBlocks()[req.pos]);
                
                unimet.proyectoso2.procesos.PCB pcb = new unimet.proyectoso2.procesos.PCB((int)(Math.random()*1000), op, target, req.pos);
                planificador.agregarProceso(pcb);
            }
        }
    }

    private Archivo buscarArchivoPorBloque(int pos, Directorio dir) {
        for (int i = 0; i < dir.getArchivos().size(); i++) {
            Archivo a = dir.getArchivos().get(i);
            if (a.getBloqueInicial() != null && a.getBloqueInicial().getId() == pos) return a;
        }
        for (int i = 0; i < dir.getSubdirectorios().size(); i++) {
            Archivo a = buscarArchivoPorBloque(pos, dir.getSubdirectorios().get(i));
            if (a != null) return a;
        }
        return null;
    }

    private unimet.proyectoso2.procesos.OperacionCRUD mapearOperacion(String opJson) {
        switch(opJson.toUpperCase()) {
            case "READ": return unimet.proyectoso2.procesos.OperacionCRUD.LEER;
            case "UPDATE": return unimet.proyectoso2.procesos.OperacionCRUD.ACTUALIZAR;
            case "DELETE": return unimet.proyectoso2.procesos.OperacionCRUD.ELIMINAR;
            case "CREATE": return unimet.proyectoso2.procesos.OperacionCRUD.CREAR;
            default: return unimet.proyectoso2.procesos.OperacionCRUD.LEER;
        }
    }
}