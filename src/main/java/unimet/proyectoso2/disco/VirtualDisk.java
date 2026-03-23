/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package unimet.proyectoso2.disco;

public class VirtualDisk {
    private final DiskBlock[] blocks;
    private final int totalBlocks;
    private int freeBlocksCount;

    public VirtualDisk(int totalBlocks) {
        this.totalBlocks = totalBlocks;
        this.freeBlocksCount = totalBlocks;
        this.blocks = new DiskBlock[totalBlocks];
        
        for (int i = 0; i < totalBlocks; i++) {
            this.blocks[i] = new DiskBlock(i);
        }
    }

    public synchronized DiskBlock allocateBlocks(int numberOfBlocks) {
        if (numberOfBlocks <= 0) return null;

        int startId = -1;
        int consecutiveFree = 0;

        for (int i = 0; i < totalBlocks; i++) {
            if (blocks[i].isFree()) {
                if (consecutiveFree == 0) startId = i;
                consecutiveFree++;
                if (consecutiveFree == numberOfBlocks) {
                    break; 
                }
            } else {
                consecutiveFree = 0; 
                }
        }
        if (consecutiveFree < numberOfBlocks) {
            System.err.println("Error de I/O: Espacio contiguo insuficiente (Fragmentación).");
            return null; 
        }

        DiskBlock firstBlock = blocks[startId];
        DiskBlock currentBlock = firstBlock;
        blocks[startId].setFree(false);
        freeBlocksCount--;

        for (int i = 1; i < numberOfBlocks; i++) {
            int nextId = startId + i;
            blocks[nextId].setFree(false);
            currentBlock.setNextBlock(blocks[nextId]);
            currentBlock = blocks[nextId];
            freeBlocksCount--;
        }
        
        currentBlock.setNextBlock(null);
        
        return firstBlock;
    }

    
    public synchronized void freeBlocks(DiskBlock head) {
        DiskBlock current = head;
        while (current != null) {
            current.setFree(true); 
            
            DiskBlock nextToProcess = current.getNextBlock();
            
            
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
    
    public synchronized void decrementarBloquesLibres() {
        if (freeBlocksCount > 0) {
            freeBlocksCount--;
        }
    }
    
    public synchronized DiskBlock allocateBlocksAt(int startId, int numberOfBlocks) {
        if (numberOfBlocks <= 0 || startId < 0 || startId + numberOfBlocks > totalBlocks) {
            return null; 
        }

        for (int i = 0; i < numberOfBlocks; i++) {
            if (!blocks[startId + i].isFree()) {
                System.err.println("Error: Colisión detectada en el bloque " + (startId + i));
                return null;
            }
        }

        DiskBlock firstBlock = blocks[startId];
        DiskBlock currentBlock = firstBlock;
        blocks[startId].setFree(false);
        freeBlocksCount--;

        for (int i = 1; i < numberOfBlocks; i++) {
            int nextId = startId + i;
            blocks[nextId].setFree(false);
            currentBlock.setNextBlock(blocks[nextId]);
            currentBlock = blocks[nextId];
            freeBlocksCount--;
        }
        
        currentBlock.setNextBlock(null);
        return firstBlock;
    }
    
    public synchronized int getLargestContiguousFreeSpace() {
        int max = 0;
        int current = 0;
        for (int i = 0; i < totalBlocks; i++) {
            if (blocks[i].isFree()) {
                current++;
                if (current > max) max = current;
            } else {
                current = 0;
            }
        }
        return max;
    }
}