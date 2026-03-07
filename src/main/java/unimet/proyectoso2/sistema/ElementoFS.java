/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package unimet.proyectoso2.sistema;
import java.time.LocalDateTime;

/**
 * Clase abstracta base para implementar polimorfismo en el File System.
 * Representa cualquier elemento que pueda existir en el disco (Archivo o Directorio).
 */
public abstract class ElementoFS {
    protected String nombre;
    protected String propietario;
    protected LocalDateTime fechaCreacion;

    public ElementoFS(String nombre, String propietario) {
        this.nombre = nombre;
        this.propietario = propietario;
        this.fechaCreacion = LocalDateTime.now(); // java.time no es una colección, es válido.
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getPropietario() { return propietario; }
    public void setPropietario(String propietario) { this.propietario = propietario; }
    
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}