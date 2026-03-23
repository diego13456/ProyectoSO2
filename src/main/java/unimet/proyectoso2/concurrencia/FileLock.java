/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package unimet.proyectoso2.concurrencia;

public class FileLock {
    private int lectoresActivos = 0;
    private boolean escribiendo = false;
    private int escritoresEsperando = 0;

    public synchronized void lockLectura() throws InterruptedException {
        while (escribiendo || escritoresEsperando > 0) {
            wait();
        }
        lectoresActivos++;
    }

    public synchronized void unlockLectura() {
        lectoresActivos--;
        if (lectoresActivos == 0) {
            notifyAll();
        }
    }

    public synchronized void lockEscritura() throws InterruptedException {
        escritoresEsperando++;
        try {
            while (escribiendo || lectoresActivos > 0) {
                wait();
            }
            escritoresEsperando--;
            escribiendo = true;
        } catch (InterruptedException e) {
            escritoresEsperando--; 
            throw e;
        }
    }

    public synchronized void unlockEscritura() {
        escribiendo = false;
        notifyAll();
    }
    
    public synchronized boolean tryLockLectura() {
        if (escribiendo || escritoresEsperando > 0) return false;
        lectoresActivos++;
        return true;
    }

    public synchronized boolean tryLockEscritura() {
        if (escribiendo || lectoresActivos > 0) return false;
        escribiendo = true;
        return true;
}
    
    public synchronized boolean requiresWaitLectura() {
        return escribiendo || escritoresEsperando > 0;
    }

    public synchronized boolean requiresWaitEscritura() {
        return escribiendo || lectoresActivos > 0;
    }
}