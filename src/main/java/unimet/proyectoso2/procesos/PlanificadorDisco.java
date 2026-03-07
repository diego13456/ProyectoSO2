/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package unimet.proyectoso2.procesos;

import unimet.proyectoso2.estructuras.LinkedList;

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
    public synchronized LinkedList<PCB> ejecutarSSTF() {
        LinkedList<PCB> ordenEjecucion = new LinkedList<>();
        
        while (!colaProcesos.isEmpty()) {
            PCB pcbMasCercano = null;
            int distanciaMinima = Integer.MAX_VALUE;

            // Búsqueda lineal manual (O(N))
            for (int i = 0; i < colaProcesos.size(); i++) {
                PCB actual = colaProcesos.get(i);
                int distancia = Math.abs(actual.getBloqueObjetivo() - cabezalActual);
                
                if (distancia < distanciaMinima) {
                    distanciaMinima = distancia;
                    pcbMasCercano = actual;
                }
            }

            // Procesar el más cercano
            colaProcesos.remove(pcbMasCercano);
            cabezalActual = pcbMasCercano.getBloqueObjetivo();
            pcbMasCercano.setEstado(EstadoProceso.TERMINADO);
            ordenEjecucion.add(pcbMasCercano);
        }
        return ordenEjecucion;
    }

    /**
     * 3. SCAN (Algoritmo del Ascensor)
     */
    public synchronized LinkedList<PCB> ejecutarSCAN() {
        LinkedList<PCB> ordenEjecucion = new LinkedList<>();
        
        while (!colaProcesos.isEmpty()) {
            PCB pcbSiguiente = null;
            int distanciaMinima = Integer.MAX_VALUE;

            // Buscar en la dirección actual
            for (int i = 0; i < colaProcesos.size(); i++) {
                PCB actual = colaProcesos.get(i);
                int diff = actual.getBloqueObjetivo() - cabezalActual;
                
                // Si va hacia arriba (1), ignoramos los negativos. Si va abajo (-1), ignoramos los positivos
                if ((direccion == 1 && diff >= 0) || (direccion == -1 && diff <= 0)) {
                    int distAbs = Math.abs(diff);
                    if (distAbs < distanciaMinima) {
                        distanciaMinima = distAbs;
                        pcbSiguiente = actual;
                    }
                }
            }

            if (pcbSiguiente != null) {
                colaProcesos.remove(pcbSiguiente);
                cabezalActual = pcbSiguiente.getBloqueObjetivo();
                pcbSiguiente.setEstado(EstadoProceso.TERMINADO);
                ordenEjecucion.add(pcbSiguiente);
            } else {
                // No hay más peticiones en esta dirección. Llegamos al extremo y rebotamos.
                if (direccion == 1) {
                    cabezalActual = tamanoDisco - 1;
                } else {
                    cabezalActual = 0;
                }
                direccion *= -1; // Invertir dirección
            }
        }
        return ordenEjecucion;
    }

    /**
     * 4. C-SCAN (Circular SCAN)
     */
    public synchronized LinkedList<PCB> ejecutarCSCAN() {
        LinkedList<PCB> ordenEjecucion = new LinkedList<>();
        
        // C-SCAN suele trabajar en una sola dirección, asumiremos siempre ASCENDENTE (1)
        direccion = 1; 

        while (!colaProcesos.isEmpty()) {
            PCB pcbSiguiente = null;
            int distanciaMinima = Integer.MAX_VALUE;

            // Buscar peticiones hacia adelante
            for (int i = 0; i < colaProcesos.size(); i++) {
                PCB actual = colaProcesos.get(i);
                int diff = actual.getBloqueObjetivo() - cabezalActual;
                
                if (diff >= 0 && diff < distanciaMinima) {
                    distanciaMinima = diff;
                    pcbSiguiente = actual;
                }
            }

            if (pcbSiguiente != null) {
                colaProcesos.remove(pcbSiguiente);
                cabezalActual = pcbSiguiente.getBloqueObjetivo();
                pcbSiguiente.setEstado(EstadoProceso.TERMINADO);
                ordenEjecucion.add(pcbSiguiente);
            } else {
                // Llegamos al final del disco. Salto abrupto al inicio sin atender peticiones.
                cabezalActual = 0;
            }
        }
        return ordenEjecucion;
    }
}