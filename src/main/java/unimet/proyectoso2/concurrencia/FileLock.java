/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package unimet.proyectoso2.concurrencia;

/**
 * Implementa un lock de Lectores/Escritores personalizado.
 * Sin usar java.util.concurrent, cumpliendo las reglas del proyecto.
 */
public class FileLock {
    private int lectoresActivos = 0;
    private boolean escribiendo = false;
    private int escritoresEsperando = 0;

    // Lock Compartido para Lectura
    public synchronized void lockLectura() throws InterruptedException {
        // Si alguien está escribiendo o hay escritores esperando, los lectores esperan
        // (Esto evita que los escritores se queden sin turno para siempre)
        while (escribiendo || escritoresEsperando > 0) {
            wait();
        }
        lectoresActivos++;
    }

    public synchronized void unlockLectura() {
        lectoresActivos--;
        // Si ya no hay lectores, avisamos a los escritores que pueden intentar entrar
        if (lectoresActivos == 0) {
            notifyAll();
        }
    }

    // Lock Exclusivo para Escritura
    public synchronized void lockEscritura() throws InterruptedException {
        escritoresEsperando++;
        // Un escritor solo entra si no hay nadie leyendo ni nadie escribiendo
        while (escribiendo || lectoresActivos > 0) {
            wait();
        }
        escritoresEsperando--;
        escribiendo = true;
    }

    public synchronized void unlockEscritura() {
        escribiendo = false;
        // Avisamos a todos (lectores y otros escritores) que el recurso está libre
        notifyAll();
    }
}