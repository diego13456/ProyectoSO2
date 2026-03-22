/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package unimet.proyectoso2.sistema;

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

    public SimuladorProcesos(PlanificadorDisco planificador, VirtualDisk disco, JournalManager journal) {
        this.planificador = planificador;
        this.disco = disco;
        this.journal = journal;
    }
    
     public void setPoliticaActual(String politica) {
        this.politicaActual = politica;
        System.out.println("[SIMULADOR] Política cambiada a: " + politica);
        }
     
    @Override
    public void run() {
        while (encendido) {
            try {
                // --- MODIFICADO: Ahora pasamos la política al planificador ---
                PCB procesoActual = planificador.obtenerSiguiente(politicaActual);

                if (procesoActual != null) {
                    ejecutarTarea(procesoActual);
                } else {
                    Thread.sleep(1000); 
                }
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void ejecutarTarea(PCB proceso) throws InterruptedException {
    Archivo archivo = proceso.getArchivoObjetivo();
    if (archivo == null) return;

    // 1. Cambiar estado a EJECUTANDO
    proceso.setEstado(EstadoProceso.EJECUTANDO);
    int destino = proceso.getBloqueObjetivo();

    // 2. SIMULAR MOVIMIENTO DEL CABEZAL (Desplazamiento en tiempo real - Requerimiento 4)
    int actual = planificador.getCabezalActual();
    System.out.println("[DISCO] Moviendo cabezal de " + actual + " a " + destino);
    
    while (actual != destino) {
        if (actual < destino) actual++;
        else actual--;
        
        planificador.setCabezalActual(actual); // Actualiza la posición interna
        Thread.sleep(30); // Velocidad del brazo del disco (ajusta si quieres más rápido)
    }

    // 3. JOURNALING: Registro inicial (Operaciones Críticas - Requerimiento 8)
    if (proceso.getOperacion() == OperacionCRUD.CREAR || proceso.getOperacion() == OperacionCRUD.ELIMINAR) {
        journal.registrarPendiente(proceso.getOperacion().toString(), archivo.getNombre(), archivo.getBloqueInicial());
    }

    // 4. GESTIÓN DE LOCKS Y OPERACIÓN (Seccion Crítica)
    try {
        // A. Adquirir el Lock correspondiente
        if (proceso.getOperacion() == OperacionCRUD.LEER) {
            System.out.println("[LOCK] Solicitando lectura para: " + archivo.getNombre());
            archivo.getLock().lockLectura();
        } else {
            System.out.println("[LOCK] Solicitando escritura para: " + archivo.getNombre());
            archivo.getLock().lockEscritura();
        }

        // B. Realizar la operación física en el disco
        switch (proceso.getOperacion()) {
            case CREAR:
                DiskBlock bloqueDeInicio = disco.allocateBlocks(archivo.getTamanoEnBloques());
                archivo.setBloqueInicial(bloqueDeInicio);
                System.out.println("[IO] Bloques asignados a " + archivo.getNombre());
                break;

            case ELIMINAR:
                if (archivo.getBloqueInicial() != null) {
                    disco.freeBlocks(archivo.getBloqueInicial());
                    archivo.setBloqueInicial(null);
                }
                System.out.println("[IO] Bloques liberados de " + archivo.getNombre());
                break;

            case LEER:
                // Simular tiempo de lectura
                Thread.sleep(1000); 
                System.out.println("[IO] Lectura completada: " + archivo.getNombre());
                break;

            case ACTUALIZAR:
                // Simular tiempo de escritura/renombrado
                Thread.sleep(1000);
                System.out.println("[IO] Actualización completada: " + archivo.getNombre());
                break;
        }

        // 5. JOURNALING: Confirmar operación (COMMIT)
        if (proceso.getOperacion() == OperacionCRUD.CREAR || proceso.getOperacion() == OperacionCRUD.ELIMINAR) {
            journal.confirmar(archivo.getNombre());
        }

    } finally {
        // 6. LIBERAR LOCKS (Siempre se ejecuta, incluso si hay error)
        if (proceso.getOperacion() == OperacionCRUD.LEER) {
            archivo.getLock().unlockLectura();
        } else {
            archivo.getLock().unlockEscritura();
        }
        
        // 7. Finalizar proceso
        proceso.setEstado(EstadoProceso.TERMINADO);
        System.out.println("[PROCESO] Finalizado ID: " + proceso.getIdProceso());
    }
}

    public void detener() { this.encendido = false; }
    
    private void moverCabezal(int destino) throws InterruptedException {
    int actual = planificador.getCabezalActual();
    while (actual != destino) {
        if (actual < destino) actual++;
        else actual--;
        
        planificador.setCabezalActual(actual); // Necesitas este setter
        Thread.sleep(50); // Velocidad del movimiento
    }
    }
}