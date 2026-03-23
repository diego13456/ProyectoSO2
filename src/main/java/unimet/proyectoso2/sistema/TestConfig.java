package unimet.proyectoso2.sistema;

import java.util.List;
import java.util.Map;

public class TestConfig {
    public String test_id;
    public int initial_head;
    public List<Request> requests;
    public Map<String, SysFile> system_files;

    public static class Request {
        public int pos;
        public String op;
    }

    public static class SysFile {
        public String name;
        public int blocks;
    }
}