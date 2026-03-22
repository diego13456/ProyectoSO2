/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package unimet.proyectoso2.disco;
/**
 * Gestor del disco virtual. Maneja la asignación y liberación de bloques.
 * Soporta operaciones concurrentes de múltiples hilos (simulando I/O concurrente).
 */
public class VirtualDisk {
    private final DiskBlock[] blocks;
    private final int totalBlocks;
    private int freeBlocksCount;

    /**
     * @param totalBlocks Tamaño total del disco en bloques.
     */
    public VirtualDisk(int totalBlocks) {
        this.totalBlocks = totalBlocks;
        this.freeBlocksCount = totalBlocks;
        this.blocks = new DiskBlock[totalBlocks];
        
        // Formateo del disco: Inicialización del hardware virtual
        for (int i = 0; i < totalBlocks; i++) {
            this.blocks[i] = new DiskBlock(i);
        }
    }

    /**
     * Algoritmo de Asignación Encadenada.
     * Busca los primeros N bloques libres en el arreglo físico y los enlaza.
     * 
     * @param numberOfBlocks Cantidad de bloques que requiere el archivo.
     * @return El bloque inicial (head) de la cadena, o null si no hay espacio.
     */
    public synchronized DiskBlock allocateBlocks(int numberOfBlocks) {
        if (numberOfBlocks <= 0) return null;
        if (numberOfBlocks > freeBlocksCount) {
            System.err.println("Error de I/O: Espacio insuficiente en el disco virtual.");
            return null; 
        }

        DiskBlock firstBlock = null;
        DiskBlock currentBlock = null;
        int blocksAssigned = 0;

        // Búsqueda lineal de sectores libres (similar a un FAT scan)
        for (int i = 0; i < totalBlocks && blocksAssigned < numberOfBlocks; i++) {
            if (blocks[i].isFree()) {
                blocks[i].setFree(false);
                
                if (firstBlock == null) {
                    firstBlock = blocks[i];
                    currentBlock = blocks[i];
                } else {
                    currentBlock.setNextBlock(blocks[i]);
                    currentBlock = blocks[i];
                }
                
                blocksAssigned++;
                freeBlocksCount--;
            }
        }
        return firstBlock;
    }

    /**
     * Liberación de un archivo (Borrado físico a nivel de FAT).
     * Sigue la cadena de bloques y los marca como libres.
     * 
     * @param head El primer bloque del archivo a liberar.
     */
    public synchronized void freeBlocks(DiskBlock head) {
    DiskBlock current = head;
    while (current != null) {
        current.setFree(true); // <--- DEBE SER TRUE
        DiskBlock nextToProcess = current.getNextBlock();
        current.setNextBlock(null); // Limpiar el enlace
        
        current = nextToProcess;
        freeBlocksCount++;
    }
}

    public synchronized int getFreeBlocksCount() {
        return freeBlocksCount;
    }

    public int getTotalBlocks() {
        return totalBlocks;
    }
    
    public DiskBlock[] getBlocks() {
    return blocks;
    }
}