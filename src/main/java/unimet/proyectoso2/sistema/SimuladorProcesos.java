package unimet.proyectoso2.sistema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import unimet.proyectoso2.procesos.*;
import unimet.proyectoso2.disco.VirtualDisk;
import unimet.proyectoso2.disco.DiskBlock;
import unimet.proyectoso2.concurrencia.JournalManager;

public class SimuladorProcesos implements Runnable {
    private PlanificadorDisco planificador;
    private VirtualDisk disco;
    private JournalManager journal;
    private boolean encendido = true;
    private String politicaActual = "FIFO"; 
    private boolean pausado = true;
    
    private final List<Thread> hilosActivos = Collections.synchronizedList(new ArrayList<>());
    
    public void pausar() { this.pausado = true; }
    public void reanudar() { this.pausado = false; }
    public boolean isPausado() { return pausado; }

    public SimuladorProcesos(PlanificadorDisco planificador, VirtualDisk disco, JournalManager journal) {
        this.planificador = planificador;
        this.disco = disco;
        this.journal = journal;
    }
    
    public void setPoliticaActual(String politica) {
        this.politicaActual = politica;
    }
     
    @Override
    public void run() {
        while (encendido) {
            try {
                if (pausado) {
                    Thread.sleep(500);
                    continue;
                }
                PCB procesoActual = planificador.obtenerSiguiente(politicaActual);

                if (procesoActual != null) {
                    Thread t = new Thread(() -> {
                        try {
                            ejecutarTarea(procesoActual);
                        } catch (InterruptedException e) {
                            System.out.println("[CRASH] Proceso " + procesoActual.getIdProceso() + " interrumpido violentamente.");
                            procesoActual.setEstado(EstadoProceso.BLOQUEADO);
                        } finally {
                            hilosActivos.remove(Thread.currentThread());
                        }
                    });
                    hilosActivos.add(t);
                    t.start();
                    
                    Thread.sleep(100); 
                } else {
                    Thread.sleep(500); 
                }
            } catch (Exception e) {
                break;
            }
        }
    }

    private void ejecutarTarea(PCB proceso) throws InterruptedException {
        Archivo archivo = proceso.getArchivoObjetivo();
        if (archivo == null) return;

        synchronized (planificador) {
            proceso.setEstado(EstadoProceso.EJECUTANDO); 
            int destino = proceso.getBloqueObjetivo();
            int actual = planificador.getCabezalActual();
            
            while (actual != destino) {
                if (actual < destino) actual++;
                else actual--;
                planificador.setCabezalActual(actual); 
                Thread.sleep(30); 
            }
        } 

        boolean lockAdquirido = false;
        try {
            if (proceso.getOperacion() == OperacionCRUD.LEER) {
                if (archivo.getLock().requiresWaitLectura()) proceso.setEstado(EstadoProceso.BLOQUEADO);
                archivo.getLock().lockLectura(); 
            } else {
                if (archivo.getLock().requiresWaitEscritura()) proceso.setEstado(EstadoProceso.BLOQUEADO);
                archivo.getLock().lockEscritura(); 
            }
            lockAdquirido = true;
            
            proceso.setEstado(EstadoProceso.EJECUTANDO);

            switch (proceso.getOperacion()) {
                case CREAR:
                    DiskBlock bloqueDeInicio = disco.allocateBlocks(archivo.getTamanoEnBloques());
                    
                    if (bloqueDeInicio == null) {
                        System.out.println("[SIMULADOR] Abortando creación de " + archivo.getNombre() + " por falta de espacio.");
                        archivo.setTamanoEnBloques(-1);
                        break;
                    }
                    
                    archivo.setBloqueInicial(bloqueDeInicio);
                    journal.registrarPendiente("CREATE", archivo.getNombre(), bloqueDeInicio);
                    Thread.sleep(1500); 
                    journal.confirmar(archivo.getNombre());
                    break;
                case ELIMINAR:
                    journal.registrarPendiente("DELETE", archivo.getNombre(), archivo.getBloqueInicial());
                    if (archivo.getBloqueInicial() != null) {
                        disco.freeBlocks(archivo.getBloqueInicial());
                    }
                    Thread.sleep(1500);
                    archivo.setBloqueInicial(null);
                    archivo.setTamanoEnBloques(-1); 
                    journal.confirmar(archivo.getNombre());
                    break;
                case LEER:
                    Thread.sleep(6000); 
                    break;
                case ACTUALIZAR:
                    Thread.sleep(2000);
                    break;
            }

        } finally {
            if (lockAdquirido) { 
                if (proceso.getOperacion() == OperacionCRUD.LEER) {
                    archivo.getLock().unlockLectura();
                } else {
                    archivo.getLock().unlockEscritura();
                }
            }
            if(proceso.getEstado() == EstadoProceso.EJECUTANDO) {
                proceso.setEstado(EstadoProceso.TERMINADO);
            }
        }}

    public void detener() { 
        this.encendido = false; 
        synchronized(hilosActivos) {
            for(Thread t : hilosActivos) {
                t.interrupt(); 
            }
        }
    }
}