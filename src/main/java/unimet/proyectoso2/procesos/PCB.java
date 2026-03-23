/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package unimet.proyectoso2.procesos;

import unimet.proyectoso2.sistema.Archivo;

public class PCB {
    private final int idProceso;
    private EstadoProceso estado;
    private OperacionCRUD operacion;
    private Archivo archivoObjetivo;
    private int bloqueObjetivo; 

    public PCB(int idProceso, OperacionCRUD operacion, Archivo archivoObjetivo, int bloqueObjetivo) {
        this.idProceso = idProceso;
        this.estado = EstadoProceso.NUEVO;
        this.operacion = operacion;
        this.archivoObjetivo = archivoObjetivo;
        this.bloqueObjetivo = bloqueObjetivo;
    }

    public int getIdProceso() { return idProceso; }
    public int getBloqueObjetivo() { return bloqueObjetivo; }
    public EstadoProceso getEstado() { return estado; }
    public void setEstado(EstadoProceso estado) { this.estado = estado; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PCB pcb = (PCB) obj;
        return idProceso == pcb.idProceso;
    }

    @Override
    public String toString() {
        return "PCB[ID:" + idProceso + " -> Track:" + bloqueObjetivo + "]";
    }
    
    public OperacionCRUD getOperacion() {
    return operacion;
    }

    public Archivo getArchivoObjetivo() {
    return archivoObjetivo;
    }
}