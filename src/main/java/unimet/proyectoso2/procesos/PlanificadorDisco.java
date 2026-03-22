
package unimet.proyectoso2.procesos;

import unimet.proyectoso2.estructuras.LinkedList;
import unimet.proyectoso2.procesos.PCB;

/**
 * Simula el controlador del disco encargado de ordenar las peticiones de I/O.
 */
public class PlanificadorDisco {
    private LinkedList<PCB> colaProcesos;
    private int cabezalActual;
    private int direccion; // 1 para ASCENDENTE, -1 para DESCENDENTE
    private final int tamanoDisco;

    public PlanificadorDisco(int cabezalInicial, int direccionInicial, int tamanoDisco) {
        this.colaProcesos = new LinkedList<>();
        this.cabezalActual = cabezalInicial;
        this.direccion = direccionInicial;
        this.tamanoDisco = tamanoDisco;
    }
    
    public LinkedList<PCB> getColaProcesos() {
    return colaProcesos;
    }
    
    public synchronized void agregarProceso(PCB proceso) {
        proceso.setEstado(EstadoProceso.LISTO);
        colaProcesos.add(proceso);
    }

    /**
     * 1. FIFO (First-In, First-Out)
     */
    public synchronized LinkedList<PCB> ejecutarFIFO() {
        LinkedList<PCB> ordenEjecucion = new LinkedList<>();
        while (!colaProcesos.isEmpty()) {
            PCB actual = colaProcesos.get(0);
            colaProcesos.remove(actual); // Lo sacamos de la cola de espera
            actual.setEstado(EstadoProceso.EJECUTANDO);
            
            cabezalActual = actual.getBloqueObjetivo(); // Movemos el cabezal
            actual.setEstado(EstadoProceso.TERMINADO);
            ordenEjecucion.add(actual);
        }
        return ordenEjecucion;
    }

    /**
     * 2. SSTF (Shortest Seek Time First)
     */
   private PCB extraerSSTF() {
    PCB pcbMasCercano = null;
    int distanciaMinima = Integer.MAX_VALUE;

    for (int i = 0; i < colaProcesos.size(); i++) {
        PCB actual = colaProcesos.get(i);
        int distancia = Math.abs(actual.getBloqueObjetivo() - cabezalActual);
        if (distancia < distanciaMinima) {
            distanciaMinima = distancia;
            pcbMasCercano = actual;
        }
    }
    colaProcesos.remove(pcbMasCercano);
    cabezalActual = pcbMasCercano.getBloqueObjetivo();
    return pcbMasCercano;
}

    /**
     * 3. SCAN (Algoritmo del Ascensor)
     */
    private PCB extraerSCAN() {
    PCB seleccionado = null;
    int minDistancia = Integer.MAX_VALUE;

    for (int i = 0; i < colaProcesos.size(); i++) {
        PCB pcb = colaProcesos.get(i);
        int diff = pcb.getBloqueObjetivo() - cabezalActual;

        // ¿Está en la dirección actual? (1 = arriba/derecha, -1 = abajo/izquierda)
        if ((direccion == 1 && diff >= 0) || (direccion == -1 && diff <= 0)) {
            int distAbs = Math.abs(diff);
            if (distAbs < minDistancia) {
                minDistancia = distAbs;
                seleccionado = pcb;
            }
        }
    }

    if (seleccionado == null) {
        // No hay más peticiones en esta dirección, "rebotamos"
        direccion *= -1; 
        return extraerSCAN(); // Buscamos ahora en la nueva dirección
    }

    colaProcesos.remove(seleccionado);
    cabezalActual = seleccionado.getBloqueObjetivo();
    return seleccionado;
}

    /**
     * 4. C-SCAN (Circular SCAN)
     */
    private PCB extraerCSCAN() {
    PCB seleccionado = null;
    int minDistancia = Integer.MAX_VALUE;

    for (int i = 0; i < colaProcesos.size(); i++) {
        PCB pcb = colaProcesos.get(i);
        int diff = pcb.getBloqueObjetivo() - cabezalActual;

        if (diff >= 0 && diff < minDistancia) {
            minDistancia = diff;
            seleccionado = pcb;
        }
    }

    if (seleccionado == null) {
        // Si no hay nadie adelante, saltamos al inicio del disco (bloque 0)
        cabezalActual = 0;
        return extraerCSCAN(); // Volvemos a buscar desde el inicio
    }

    colaProcesos.remove(seleccionado);
    cabezalActual = seleccionado.getBloqueObjetivo();
    return seleccionado;
}

// No olvides este getter para que la GUI sepa dónde pintar el cabezal (Rojo)
    public synchronized int getCabezalActual() {
    return cabezalActual;
    }
    
     public synchronized PCB obtenerSiguiente(String politica) {
    if (colaProcesos.isEmpty()) return null;

    switch (politica) {
        case "SSTF":
            return extraerSSTF();
        case "SCAN":
            return extraerSCAN();
        case "C-SCAN":
            return extraerCSCAN();
        default: // FIFO
            PCB pcb = colaProcesos.get(0);
            colaProcesos.remove(pcb);
            cabezalActual = pcb.getBloqueObjetivo();
            return pcb;
    }
  }

    public synchronized void setCabezalActual(int nuevaPosicion) {
    this.cabezalActual = nuevaPosicion;
    }
}
    
  