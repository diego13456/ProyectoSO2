package unimet.proyectoso2.concurrencia;

import unimet.proyectoso2.estructuras.LinkedList;
import unimet.proyectoso2.disco.DiskBlock;
import unimet.proyectoso2.disco.VirtualDisk;

public class JournalManager {

    public static class LogEntry {
        public String operacion;
        public String nombreArchivo;
        public DiskBlock bloqueInicial;
        public String estado;

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

    public synchronized void registrarPendiente(String op, String nombre, DiskBlock bloque) {
        LogEntry entrada = new LogEntry(op, nombre, bloque);
        transacciones.add(entrada);
        System.out.println("[JOURNAL] Registrada operación PENDIENTE: " + op + " para " + nombre);
    }

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


   public synchronized void ejecutarRecuperacion(VirtualDisk disco) {
        System.out.println("[JOURNAL] Iniciando verificación de consistencia...");
        
        for (int i = 0; i < transacciones.size(); i++) {
            LogEntry entry = transacciones.get(i);
            
            if (entry.estado.equals("PENDIENTE")) {
                System.out.println("[JOURNAL] Detectada operación inconclusa: " + entry.nombreArchivo);
                
                if (entry.operacion.equals("CREATE") && entry.bloqueInicial != null) {
                    disco.freeBlocks(entry.bloqueInicial);
                    System.out.println("[JOURNAL] UNDO aplicado: Bloques liberados para " + entry.nombreArchivo);
                }
                
                if (entry.operacion.equals("DELETE") && entry.bloqueInicial != null) {
                    DiskBlock current = entry.bloqueInicial;
                    while (current != null && current.isFree()) {
                        current.setFree(false);
                        disco.decrementarBloquesLibres(); 
                        current = current.getNextBlock();
                    }
                    System.out.println("[JOURNAL] UNDO aplicado: Bloques recuperados para " + entry.nombreArchivo);
                }
                
                transacciones.remove(entry);
                i--;
            }
        }
        System.out.println("[JOURNAL] Recuperación finalizada.");
    }

    public LinkedList<LogEntry> getTransacciones() {
        return transacciones;
    }
}