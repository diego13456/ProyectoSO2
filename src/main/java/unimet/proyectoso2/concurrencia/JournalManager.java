/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package unimet.proyectoso2.concurrencia;

import unimet.proyectoso2.estructuras.LinkedList;
import unimet.proyectoso2.disco.DiskBlock;
import unimet.proyectoso2.disco.VirtualDisk;

public class JournalManager {

    // Clase interna para representar cada entrada del log
    public static class LogEntry {
        public String operacion;   // "CREATE" o "DELETE"
        public String nombreArchivo;
        public DiskBlock bloqueInicial; // Para saber qué liberar en un UNDO
        public String estado;      // "PENDIENTE" o "CONFIRMADA"

        public LogEntry(String op, String nombre, DiskBlock bloque) {
            this.operacion = op;
            this.nombreArchivo = nombre;
            this.bloqueInicial = bloque;
            this.estado = "PENDIENTE";
        }
    }

    private LinkedList<LogEntry> transacciones;

    public JournalManager() {
        this.transacciones = new LinkedList<>();
    }

    /**
     * Paso A: Registrar antes de ejecutar la operación crítica.
     */
    public synchronized void registrarPendiente(String op, String nombre, DiskBlock bloque) {
        LogEntry entrada = new LogEntry(op, nombre, bloque);
        transacciones.add(entrada);
        System.out.println("[JOURNAL] Registrada operación PENDIENTE: " + op + " para " + nombre);
    }

    /**
     * Paso B: Marcar como CONFIRMADA (Commit) tras el éxito.
     */
    public synchronized void confirmar(String nombre) {
        for (int i = 0; i < transacciones.size(); i++) {
            LogEntry entry = transacciones.get(i);
            if (entry.nombreArchivo.equals(nombre) && entry.estado.equals("PENDIENTE")) {
                entry.estado = "CONFIRMADA";
                System.out.println("[JOURNAL] Operación CONFIRMADA: " + nombre);
                break;
            }
        }
    }

    /**
     * REQUERIMIENTO 8: Recuperación al reiniciar.
     * Busca transacciones PENDIENTES y aplica UNDO.
     */
    public synchronized void ejecutarRecuperacion(VirtualDisk disco) {
        System.out.println("[JOURNAL] Iniciando verificación de consistencia...");
        
        for (int i = 0; i < transacciones.size(); i++) {
            LogEntry entry = transacciones.get(i);
            
            if (entry.estado.equals("PENDIENTE")) {
                System.out.println("[JOURNAL] Detectada operación inconclusa: " + entry.nombreArchivo);
                
                // Aplicar UNDO: Si se estaba creando, liberamos los bloques para evitar basura
                if (entry.operacion.equals("CREATE") && entry.bloqueInicial != null) {
                    disco.freeBlocks(entry.bloqueInicial);
                    System.out.println("[JOURNAL] UNDO aplicado: Bloques liberados para " + entry.nombreArchivo);
                }
                
                // Removemos la entrada fallida
                transacciones.remove(entry);
            }
        }
        System.out.println("[JOURNAL] Recuperación finalizada.");
    }

    public LinkedList<LogEntry> getTransacciones() {
        return transacciones;
    }
}