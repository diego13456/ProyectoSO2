/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package unimet.proyectoso2.disco;
public class DiskBlock {
    private final int id;
    private boolean isFree;
    private DiskBlock nextBlock;

    public DiskBlock(int id) {
        this.id = id;
        this.isFree = true;
        this.nextBlock = null;
    }

    public int getId() { return id; }
    
    public boolean isFree() { return isFree; }
    
    public void setFree(boolean free) { isFree = free; }
    
    public DiskBlock getNextBlock() { return nextBlock; }
    
    public void setNextBlock(DiskBlock nextBlock) { this.nextBlock = nextBlock; }
    
    @Override
    public String toString() {
        return "Block[" + id + "]";
    }
}
