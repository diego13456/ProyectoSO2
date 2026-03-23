package unimet.proyectoso2.procesos;

import unimet.proyectoso2.estructuras.LinkedList;

public class PlanificadorDisco {
    private LinkedList<PCB> colaProcesos;
    private LinkedList<PCB> historialProcesos;
    private int cabezalActual;
    private int direccion;
    private final int tamanoDisco;

    public PlanificadorDisco(int cabezalInicial, int direccionInicial, int tamanoDisco) {
        this.colaProcesos = new LinkedList<>();
        this.historialProcesos = new LinkedList<>(); 
        this.cabezalActual = cabezalInicial;
        this.direccion = direccionInicial;
        this.tamanoDisco = tamanoDisco;
    }

    public LinkedList<PCB> getColaProcesos() {
        return colaProcesos;
    }

    public LinkedList<PCB> getHistorialProcesos() {
        return historialProcesos;
    }

    public synchronized void agregarProceso(PCB proceso) {
        proceso.setEstado(EstadoProceso.NUEVO);
        historialProcesos.add(proceso);
        
        new Thread(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {}
            
            synchronized(this) {
                if (proceso.getEstado() != EstadoProceso.BLOQUEADO) {
                    proceso.setEstado(EstadoProceso.LISTO);
                    colaProcesos.add(proceso);
                }
            }
        }).start();
    }
    
    public synchronized void vaciarColaPorCrash() {
        colaProcesos = new LinkedList<>();
        for (int i = 0; i < historialProcesos.size(); i++) {
            PCB p = historialProcesos.get(i);
            if (p.getEstado() == EstadoProceso.LISTO || p.getEstado() == EstadoProceso.NUEVO || p.getEstado() == EstadoProceso.EJECUTANDO) {
                p.setEstado(EstadoProceso.BLOQUEADO);
            }
        }
    }
    public synchronized LinkedList<PCB> ejecutarFIFO() {
        LinkedList<PCB> ordenEjecucion = new LinkedList<>();
        while (!colaProcesos.isEmpty()) {
            PCB actual = colaProcesos.get(0);
            colaProcesos.remove(actual);
            actual.setEstado(EstadoProceso.EJECUTANDO);
            
            cabezalActual = actual.getBloqueObjetivo(); 
            actual.setEstado(EstadoProceso.TERMINADO);
            ordenEjecucion.add(actual);
        }
        return ordenEjecucion;
    }

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
        
        if (pcbMasCercano != null) {
            colaProcesos.remove(pcbMasCercano);
        }
        return pcbMasCercano;
    }

    private PCB extraerSCAN() {
        PCB seleccionado = null;
        int minDistancia = Integer.MAX_VALUE;

        for (int i = 0; i < colaProcesos.size(); i++) {
            PCB pcb = colaProcesos.get(i);
            int diff = pcb.getBloqueObjetivo() - cabezalActual;

            if ((direccion == 1 && diff >= 0) || (direccion == -1 && diff <= 0)) {
                int distAbs = Math.abs(diff);
                if (distAbs < minDistancia) {
                    minDistancia = distAbs;
                    seleccionado = pcb;
                }
            }
        }

        if (seleccionado == null && !colaProcesos.isEmpty()) {
            direccion *= -1;
            return extraerSCAN();
        }

        if (seleccionado != null) {
            colaProcesos.remove(seleccionado);
        }
        return seleccionado;
    }

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

        if (seleccionado == null && !colaProcesos.isEmpty()) {
            cabezalActual = 0;
            return extraerCSCAN();
        }

        if (seleccionado != null) {
            colaProcesos.remove(seleccionado);
        }
        return seleccionado;
    }

    public synchronized int getCabezalActual() {
        return cabezalActual;
    }

    public synchronized void setCabezalActual(int nuevaPosicion) {
        this.cabezalActual = nuevaPosicion;
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
            default:
                PCB pcb = colaProcesos.get(0);
                colaProcesos.remove(pcb);
                return pcb;
        }
    }
}