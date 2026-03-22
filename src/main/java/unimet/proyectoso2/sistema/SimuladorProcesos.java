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

    public SimuladorProcesos(PlanificadorDisco planificador, VirtualDisk disco, JournalManager journal) {
        this.planificador = planificador;
        this.disco = disco;
        this.journal = journal;
    }

    @Override
    public void run() {
        while (encendido) {
            try {
                // Obtenemos el siguiente proceso de la cola del planificador
                PCB procesoActual = planificador.obtenerSiguiente();

                if (procesoActual != null) {
                    ejecutarTarea(procesoActual);
                } else {
                    Thread.sleep(1000); // Esperar si la cola está vacía
                }
            } catch (InterruptedException e) {
                System.out.println("Simulación interrumpida.");
                break;
            }
        }
    }

    private void ejecutarTarea(PCB proceso) throws InterruptedException {
        Archivo archivo = proceso.getArchivoObjetivo();
        if (archivo == null) return;

        proceso.setEstado(EstadoProceso.EJECUTANDO);
        
        // 1. Simular movimiento del cabezal
        Thread.sleep(1000); 

        // 2. Journaling (Solo para operaciones críticas)
        if (proceso.getOperacion() == OperacionCRUD.CREAR || proceso.getOperacion() == OperacionCRUD.ELIMINAR) {
            // Usamos el nombre del método exacto de tu JournalManager
            journal.registrarPendiente(proceso.getOperacion().toString(), archivo.getNombre(), archivo.getBloqueInicial());
        }

        try {
            // 3. Gestión de Locks
            if (proceso.getOperacion() == OperacionCRUD.LEER) {
                archivo.getLock().lockLectura();
            } else {
                archivo.getLock().lockEscritura();
            }

            // 4. Operación en Disco
            if (proceso.getOperacion() == OperacionCRUD.CREAR) {
            // 1. El disco reserva los bloques y nos da el PRIMERO
            DiskBlock bloqueDeInicio = disco.allocateBlocks(archivo.getTamanoEnBloques());
            // 2. ¡IMPORTANTE! Se lo asignamos al objeto archivo para que sepa dónde empieza
            archivo.setBloqueInicial(bloqueDeInicio); 
            System.out.println("Bloques asignados a " + archivo.getNombre() + " empezando en: " + (bloqueDeInicio != null ? bloqueDeInicio.getId() : "Error"));

            } else if (proceso.getOperacion() == OperacionCRUD.ELIMINAR) {
                disco.freeBlocks(archivo.getBloqueInicial());
            }

            Thread.sleep(1000); // Simular tiempo de I/O

            // 5. Confirmar en Journal
            journal.confirmar(archivo.getNombre());

        } finally {
            // 6. Liberar Locks siempre
            if (proceso.getOperacion() == OperacionCRUD.LEER) {
                archivo.getLock().unlockLectura();
            } else {
                archivo.getLock().unlockEscritura();
            }
            proceso.setEstado(EstadoProceso.TERMINADO);
        }
    }

    public void detener() { this.encendido = false; }
}