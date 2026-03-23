package unimet.proyectoso2.sistema;

import com.google.gson.*;
import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;

public class PersistenceManager {
    private static final String FILE_PATH = "filesystem_data.json";
    
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                @Override
                public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(src.toString());
                }
            })
            .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                @Override
                public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    return LocalDateTime.parse(json.getAsString());
                }
            })
            .setPrettyPrinting()
            .create();

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
    
    public static TestConfig cargarJsonPrueba(String rutaArchivo) {
        try (FileReader reader = new FileReader(rutaArchivo)) {
            return gson.fromJson(reader, TestConfig.class);
        } catch (Exception e) {
            System.err.println("Error al cargar el JSON de prueba: " + e.getMessage());
            return null;
        }
    }
}