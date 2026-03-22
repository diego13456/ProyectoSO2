/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package unimet.proyectoso2.sistema;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;

public class PersistenceManager {
    private static final String FILE_PATH = "filesystem_data.json";
    // Usamos setPrettyPrinting para que el JSON sea legible
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void guardar(Directorio root) {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(root, writer);
            System.out.println("[JSON] Datos guardados en " + FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Directorio cargar() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return null;

        try (FileReader reader = new FileReader(FILE_PATH)) {
            return gson.fromJson(reader, Directorio.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}