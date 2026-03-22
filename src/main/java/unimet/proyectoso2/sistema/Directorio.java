/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package unimet.proyectoso2.sistema;

import unimet.proyectoso2.estructuras.LinkedList; // Nuestra lista sin java.util

/**
 * Representa una carpeta en el sistema de archivos. 
 * Contiene listas seguras para concurrencia de sus elementos internos.
 */
public class Directorio extends ElementoFS {
    private final LinkedList<Archivo> archivos;
    private final LinkedList<Directorio> subdirectorios;

    public Directorio(String nombre, String propietario) {
        super(nombre, propietario);
        this.archivos = new LinkedList<>();
        this.subdirectorios = new LinkedList<>();
    }

    public synchronized void agregarArchivo(Archivo archivo) {
        archivos.add(archivo);
    }

    public synchronized Archivo buscarArchivo(String nombreBuscado) {
        for (int i = 0; i < archivos.size(); i++) {
            Archivo actual = archivos.get(i);
            if (actual.getNombre().equals(nombreBuscado)) {
                return actual;
            }
        }
        return null;
    }

    public synchronized void agregarSubdirectorio(Directorio dir) {
        subdirectorios.add(dir);
    }

    public synchronized Directorio buscarSubdirectorio(String nombreBuscado) {
        for (int i = 0; i < subdirectorios.size(); i++) {
            Directorio actual = subdirectorios.get(i);
            if (actual.getNombre().equals(nombreBuscado)) {
                return actual;
            }
        }
        return null;
    }
    public LinkedList<Archivo> getArchivos() {
    return archivos;
    }

    public LinkedList<Directorio> getSubdirectorios() {
    return subdirectorios;
    }
}