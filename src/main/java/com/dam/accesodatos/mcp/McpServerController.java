package com.dam.accesodatos.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.dam.accesodatos.ra1.FileUserService;
import com.dam.accesodatos.model.User;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador REST que expone las herramientas MCP via HTTP.
 *
 * Proporciona endpoints para que los LLMs puedan:
 * - Listar herramientas disponibles
 * - Ejecutar herramientas específicas
 * - Obtener información sobre el servidor MCP
 */
@RestController
@RequestMapping("/mcp")
@CrossOrigin(origins = "*")
public class McpServerController {

    private static final Logger logger = LoggerFactory.getLogger(McpServerController.class);

    @Autowired
    private McpToolRegistry toolRegistry;

    @Autowired
    private FileUserService fileUserService;

    /**
     * Endpoint para listar todas las herramientas MCP disponibles
     */
    @GetMapping("/tools")
    public ResponseEntity<Map<String, Object>> getTools() {
        logger.debug("Solicitadas herramientas MCP disponibles");

        List<McpToolRegistry.McpToolInfo> tools = toolRegistry.getRegisteredTools();

        List<Map<String, String>> toolsList = tools.stream()
                .map(tool -> {
                    Map<String, String> toolMap = new HashMap<>();
                    toolMap.put("name", tool.getName());
                    toolMap.put("description", tool.getDescription());
                    return toolMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("tools", toolsList);
        response.put("count", toolsList.size());
        response.put("server", "MCP Server - RA1 Ficheros DAM");
        response.put("version", "1.0.0");

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para obtener información del servidor MCP
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getServerInfo() {
        logger.debug("Solicitada información del servidor MCP");

        Map<String, Object> info = new HashMap<>();
        info.put("name", "MCP Server - RA1 Gestión de Ficheros DAM");
        info.put("version", "1.0.0");
        info.put("description", "Servidor MCP para gestión de ficheros con herramientas CSV, JSON y XML");
        info.put("protocol", "Model Context Protocol (MCP)");
        info.put("tools_count", toolRegistry.getRegisteredTools().size());
        info.put("supported_formats", List.of("CSV", "JSON", "XML"));

        return ResponseEntity.ok(info);
    }

    /**
     * Endpoint de health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> getHealth() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "MCP Server");

        return ResponseEntity.ok(health);
    }

    /**
     * Endpoint para obtener documentación de uso
     */
    @GetMapping("/docs")
    public ResponseEntity<Map<String, Object>> getDocs() {
        Map<String, Object> docs = new HashMap<>();
        docs.put("usage", "Este servidor MCP proporciona herramientas para gestión de archivos de usuarios");

        Map<String, String> endpoints = new HashMap<>();
        // Endpoints de información
        endpoints.put("GET /mcp/tools", "Lista todas las herramientas disponibles");
        endpoints.put("GET /mcp/info", "Información del servidor");
        endpoints.put("GET /mcp/health", "Estado del servidor");
        endpoints.put("GET /mcp/docs", "Esta documentación");

        // CSV endpoints
        endpoints.put("POST /mcp/csv/read", "Lee usuarios desde archivo CSV");
        endpoints.put("POST /mcp/csv/write", "Escribe usuarios a archivo CSV");

        // JSON endpoints
        endpoints.put("POST /mcp/json/read", "Lee usuarios desde archivo JSON");
        endpoints.put("POST /mcp/json/write", "Escribe usuarios a archivo JSON");

        // XML endpoints
        endpoints.put("POST /mcp/xml/read_dom", "Lee usuarios desde XML (DOM parser)");
        endpoints.put("POST /mcp/xml/read_sax", "Lee usuarios desde XML (SAX parser)");
        endpoints.put("POST /mcp/xml/write", "Escribe usuarios a archivo XML");

        // File management endpoints
        endpoints.put("POST /mcp/files/list", "Lista archivos de usuario en directorio");
        endpoints.put("POST /mcp/files/validate_structure", "Valida estructura de directorios");

        // Analysis endpoints
        endpoints.put("POST /mcp/analysis/search_text", "Busca texto en archivo");
        endpoints.put("POST /mcp/analysis/compare_io", "Compara rendimiento BufferedReader vs FileReader");
        endpoints.put("POST /mcp/analysis/compare_nio_io", "Compara java.nio.file.Files vs java.io");

        // Random access endpoints
        endpoints.put("POST /mcp/random/read", "Lee desde posición específica (RandomAccessFile)");
        endpoints.put("POST /mcp/random/write", "Escribe en posición específica (RandomAccessFile)");

        // Processing endpoints
        endpoints.put("POST /mcp/process/convert_encoding", "Convierte codificación de archivo");
        endpoints.put("POST /mcp/process/create_temp", "Crea archivo temporal");
        endpoints.put("POST /mcp/process/format_text", "Formatea archivo de texto");

        // Test endpoint
        endpoints.put("POST /mcp/test/get_file_info", "Obtiene información de archivo (implementado)");

        docs.put("endpoints", endpoints);
        docs.put("total_endpoints", endpoints.size());
        docs.put("note", "Las herramientas están implementadas como esqueletos educativos para que los estudiantes las completen");

        return ResponseEntity.ok(docs);
    }

    /**
     * Endpoint de prueba para ejecutar getFileInfo (implementado por estudiante)
     */
    @PostMapping("/test/get_file_info")
    public ResponseEntity<Map<String, Object>> testGetFileInfo(@RequestBody Map<String, String> request) {
        logger.debug("Ejecutando prueba de getFileInfo");

        String filePath = request.get("filePath");
        if (filePath == null || filePath.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'filePath' es requerido");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            String result = fileUserService.getFileInfo(filePath);

            Map<String, Object> response = new HashMap<>();
            response.put("tool", "get_file_info");
            response.put("input", filePath);
            response.put("result", result);
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error ejecutando getFileInfo para archivo: " + filePath, e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error ejecutando getFileInfo: " + e.getMessage());
            error.put("tool", "get_file_info");
            error.put("input", filePath);
            error.put("status", "error");

            return ResponseEntity.status(500).body(error);
        }
    }

    // ========== CSV ENDPOINTS ==========

    /**
     * Lee usuarios desde archivo CSV
     */
    @PostMapping("/csv/read")
    public ResponseEntity<Map<String, Object>> readCSV(@RequestBody Map<String, String> request) {
        logger.debug("Leyendo usuarios desde CSV");

        String filePath = request.get("filePath");
        if (filePath == null || filePath.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'filePath' es requerido");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            List<com.dam.accesodatos.model.User> users = fileUserService.readUsersFromCSV(filePath);

            Map<String, Object> response = new HashMap<>();
            response.put("tool", "read_users_csv");
            response.put("input", filePath);
            response.put("result", users);
            response.put("count", users.size());
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error leyendo CSV: " + filePath, e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error leyendo CSV: " + e.getMessage());
            error.put("tool", "read_users_csv");
            error.put("input", filePath);
            error.put("status", "error");

            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Escribe usuarios a archivo CSV
     */
    @PostMapping("/csv/write")
    public ResponseEntity<Map<String, Object>> writeCSV(@RequestBody Map<String, Object> request) {
        logger.debug("Escribiendo usuarios a CSV");

        String filePath = (String) request.get("filePath");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> usersData = (List<Map<String, Object>>) request.get("users");

        if (filePath == null || filePath.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'filePath' es requerido");
            return ResponseEntity.badRequest().body(error);
        }

        if (usersData == null || usersData.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'users' es requerido y no puede estar vacío");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            // Convertir Map a User objects
            List<com.dam.accesodatos.model.User> users = new java.util.ArrayList<>();

            for (Map<String, Object> userData : usersData) {
                // Extraer datos del map
                Long id = userData.get("id") != null ? ((Number) userData.get("id")).longValue() : null;
                String name = (String) userData.get("name");
                String email = (String) userData.get("email");
                String department = (String) userData.get("department");
                String role = (String) userData.get("role");

                // Crear User con constructor apropiado
                User user = new User(id, name, email, department, role);

                // Setters solo para campos mutables opcionales
                if (userData.get("active") != null) {
                    user.setActive((Boolean) userData.get("active"));
                }

                if (userData.get("createdAt") != null) {
                    String createdAtStr = (String) userData.get("createdAt");
                    user.setCreatedAt(LocalDateTime.parse(createdAtStr));
                }

                if (userData.get("updatedAt") != null) {
                    String updatedAtStr = (String) userData.get("updatedAt");
                    user.setUpdatedAt(LocalDateTime.parse(updatedAtStr));
                }

                users.add(user);
            }

            boolean success = fileUserService.writeUsersToCSV(users, filePath);

            Map<String, Object> response = new HashMap<>();
            response.put("tool", "write_users_csv");
            response.put("input", Map.of("filePath", filePath, "users", usersData.size()));
            response.put("result", success);
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error escribiendo CSV: " + filePath, e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error escribiendo CSV: " + e.getMessage());
            error.put("tool", "write_users_csv");
            error.put("input", filePath);
            error.put("status", "error");

            return ResponseEntity.status(500).body(error);
        }
    }

    // ========== JSON ENDPOINTS ==========

    /**
     * Lee usuarios desde archivo JSON
     */
    @PostMapping("/json/read")
    public ResponseEntity<Map<String, Object>> readJSON(@RequestBody Map<String, String> request) {
        logger.debug("Leyendo usuarios desde JSON");

        String filePath = request.get("filePath");
        if (filePath == null || filePath.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'filePath' es requerido");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            List<com.dam.accesodatos.model.User> users = fileUserService.readUsersFromJSON(filePath);

            Map<String, Object> response = new HashMap<>();
            response.put("tool", "read_users_json");
            response.put("input", filePath);
            response.put("result", users);
            response.put("count", users.size());
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error leyendo JSON: " + filePath, e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error leyendo JSON: " + e.getMessage());
            error.put("tool", "read_users_json");
            error.put("input", filePath);
            error.put("status", "error");

            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Escribe usuarios a archivo JSON
     */
    @PostMapping("/json/write")
    public ResponseEntity<Map<String, Object>> writeJSON(@RequestBody Map<String, Object> request) {
        logger.debug("Escribiendo usuarios a JSON");

        String filePath = (String) request.get("filePath");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> usersData = (List<Map<String, Object>>) request.get("users");

        if (filePath == null || filePath.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'filePath' es requerido");
            return ResponseEntity.badRequest().body(error);
        }

        if (usersData == null || usersData.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'users' es requerido y no puede estar vacío");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            List<com.dam.accesodatos.model.User> users = new java.util.ArrayList<>();

            for (Map<String, Object> userData : usersData) {
                // Extraer datos del map
                Long id = userData.get("id") != null ? ((Number) userData.get("id")).longValue() : null;
                String name = (String) userData.get("name");
                String email = (String) userData.get("email");
                String department = (String) userData.get("department");
                String role = (String) userData.get("role");

                // Crear User con constructor apropiado
                User user = new User(id, name, email, department, role);

                // Setters solo para campos mutables opcionales
                if (userData.get("active") != null) {
                    user.setActive((Boolean) userData.get("active"));
                }

                if (userData.get("createdAt") != null) {
                    String createdAtStr = (String) userData.get("createdAt");
                    user.setCreatedAt(LocalDateTime.parse(createdAtStr));
                }

                if (userData.get("updatedAt") != null) {
                    String updatedAtStr = (String) userData.get("updatedAt");
                    user.setUpdatedAt(LocalDateTime.parse(updatedAtStr));
                }

                users.add(user);
            }

            boolean success = fileUserService.writeUsersToJSON(users, filePath);

            Map<String, Object> response = new HashMap<>();
            response.put("tool", "write_users_json");
            response.put("input", Map.of("filePath", filePath, "users", usersData.size()));
            response.put("result", success);
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error escribiendo JSON: " + filePath, e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error escribiendo JSON: " + e.getMessage());
            error.put("tool", "write_users_json");
            error.put("input", filePath);
            error.put("status", "error");

            return ResponseEntity.status(500).body(error);
        }
    }

    // ========== XML ENDPOINTS ==========

    /**
     * Lee usuarios desde archivo XML usando DOM parser
     */
    @PostMapping("/xml/read_dom")
    public ResponseEntity<Map<String, Object>> readXMLDOM(@RequestBody Map<String, String> request) {
        logger.debug("Leyendo usuarios desde XML (DOM)");

        String filePath = request.get("filePath");
        if (filePath == null || filePath.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'filePath' es requerido");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            List<com.dam.accesodatos.model.User> users = fileUserService.readUsersFromXML(filePath);

            Map<String, Object> response = new HashMap<>();
            response.put("tool", "read_users_xml_dom");
            response.put("input", filePath);
            response.put("result", users);
            response.put("count", users.size());
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error leyendo XML (DOM): " + filePath, e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error leyendo XML (DOM): " + e.getMessage());
            error.put("tool", "read_users_xml_dom");
            error.put("input", filePath);
            error.put("status", "error");

            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Lee usuarios desde archivo XML usando SAX parser
     */
    @PostMapping("/xml/read_sax")
    public ResponseEntity<Map<String, Object>> readXMLSAX(@RequestBody Map<String, String> request) {
        logger.debug("Leyendo usuarios desde XML (SAX)");

        String filePath = request.get("filePath");
        if (filePath == null || filePath.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'filePath' es requerido");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            List<com.dam.accesodatos.model.User> users = fileUserService.readUsersFromXMLSAX(filePath);

            Map<String, Object> response = new HashMap<>();
            response.put("tool", "read_users_xml_sax");
            response.put("input", filePath);
            response.put("result", users);
            response.put("count", users.size());
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error leyendo XML (SAX): " + filePath, e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error leyendo XML (SAX): " + e.getMessage());
            error.put("tool", "read_users_xml_sax");
            error.put("input", filePath);
            error.put("status", "error");

            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Escribe usuarios a archivo XML
     */
    @PostMapping("/xml/write")
    public ResponseEntity<Map<String, Object>> writeXML(@RequestBody Map<String, Object> request) {
        logger.debug("Escribiendo usuarios a XML");


        String filePath = (String) request.get("filePath");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> usersData = (List<Map<String, Object>>) request.get("users");


        if (filePath == null || filePath.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'filePath' es requerido");
            return ResponseEntity.badRequest().body(error);
        }


        if (usersData == null || usersData.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'users' es requerido y no puede estar vacío");
            return ResponseEntity.badRequest().body(error);
        }


        try {
            // Convertir List<Map<String, Object>> a List<User>
            List<com.dam.accesodatos.model.User> users = new java.util.ArrayList<>();

            for (Map<String, Object> userData : usersData) {
                // Extraer datos del map
                Long id = userData.get("id") != null ? ((Number) userData.get("id")).longValue() : null;
                String name = (String) userData.get("name");
                String email = (String) userData.get("email");
                String department = (String) userData.get("department");
                String role = (String) userData.get("role");

                // Crear User con constructor apropiado
                User user = new User(id, name, email, department, role);

                // Setters solo para campos mutables opcionales
                if (userData.get("active") != null) {
                    user.setActive((Boolean) userData.get("active"));
                }

                if (userData.get("createdAt") != null) {
                    String createdAtStr = (String) userData.get("createdAt");
                    user.setCreatedAt(LocalDateTime.parse(createdAtStr));
                }

                if (userData.get("updatedAt") != null) {
                    String updatedAtStr = (String) userData.get("updatedAt");
                    user.setUpdatedAt(LocalDateTime.parse(updatedAtStr));
                }

                users.add(user);
            }


            boolean success = fileUserService.writeUsersToXML(users, filePath);


            Map<String, Object> response = new HashMap<>();
            response.put("tool", "write_users_xml");
            response.put("input", Map.of("filePath", filePath, "users", usersData.size()));
            response.put("result", success);
            response.put("status", "success");


            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error escribiendo XML: " + filePath, e);


            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error escribiendo XML: " + e.getMessage());
            error.put("tool", "write_users_xml");
            error.put("input", filePath);
            error.put("status", "error");


            return ResponseEntity.status(500).body(error);
        }
    }

    // ========== FILE MANAGEMENT ENDPOINTS ==========

    /**
     * Lista archivos de usuario en directorio
     */
    @PostMapping("/files/list")
    public ResponseEntity<Map<String, Object>> listFiles(@RequestBody Map<String, String> request) {
        logger.debug("Listando archivos de usuario");

        String directoryPath = request.get("directoryPath");
        if (directoryPath == null || directoryPath.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'directoryPath' es requerido");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            List<String> files = fileUserService.listUserFiles(directoryPath);

            Map<String, Object> response = new HashMap<>();
            response.put("tool", "list_user_files");
            response.put("input", directoryPath);
            response.put("result", files);
            response.put("count", files.size());
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error listando archivos: " + directoryPath, e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error listando archivos: " + e.getMessage());
            error.put("tool", "list_user_files");
            error.put("input", directoryPath);
            error.put("status", "error");

            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Valida estructura de directorios
     */
    @PostMapping("/files/validate_structure")
    public ResponseEntity<Map<String, Object>> validateStructure(@RequestBody Map<String, String> request) {
        logger.debug("Validando estructura de directorios");

        String basePath = request.get("basePath");
        if (basePath == null || basePath.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'basePath' es requerido");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            boolean valid = fileUserService.validateDirectoryStructure(basePath);

            Map<String, Object> response = new HashMap<>();
            response.put("tool", "validate_directory_structure");
            response.put("input", basePath);
            response.put("result", valid);
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error validando estructura: " + basePath, e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error validando estructura: " + e.getMessage());
            error.put("tool", "validate_directory_structure");
            error.put("input", basePath);
            error.put("status", "error");

            return ResponseEntity.status(500).body(error);
        }
    }

    // ========== ANALYSIS ENDPOINTS ==========

    /**
     * Busca texto en archivo
     */
    @PostMapping("/analysis/search_text")
    public ResponseEntity<Map<String, Object>> searchText(@RequestBody Map<String, String> request) {
        logger.debug("Buscando texto en archivo");

        String filePath = request.get("filePath");
        String searchText = request.get("searchText");

        if (filePath == null || filePath.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'filePath' es requerido");
            return ResponseEntity.badRequest().body(error);
        }

        if (searchText == null || searchText.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'searchText' es requerido");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            String result = fileUserService.searchTextInFile(filePath, searchText);

            Map<String, Object> response = new HashMap<>();
            response.put("tool", "search_text_in_file");
            response.put("input", Map.of("filePath", filePath, "searchText", searchText));
            response.put("result", result);
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error buscando texto: " + filePath, e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error buscando texto: " + e.getMessage());
            error.put("tool", "search_text_in_file");
            error.put("input", Map.of("filePath", filePath, "searchText", searchText));
            error.put("status", "error");

            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Compara rendimiento de I/O
     */
    @PostMapping("/analysis/compare_io")
    public ResponseEntity<Map<String, Object>> compareIO(@RequestBody Map<String, String> request) {
        logger.debug("Comparando rendimiento I/O");

        String filePath = request.get("filePath");
        if (filePath == null || filePath.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'filePath' es requerido");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            String result = fileUserService.compareIOPerformance(filePath);

            Map<String, Object> response = new HashMap<>();
            response.put("tool", "compare_io_performance");
            response.put("input", filePath);
            response.put("result", result);
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error comparando I/O: " + filePath, e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error comparando I/O: " + e.getMessage());
            error.put("tool", "compare_io_performance");
            error.put("input", filePath);
            error.put("status", "error");

            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Compara NIO vs I/O tradicional
     */
    @PostMapping("/analysis/compare_nio_io")
    public ResponseEntity<Map<String, Object>> compareNIOvsIO(@RequestBody Map<String, String> request) {
        logger.debug("Comparando NIO vs I/O");

        String filePath = request.get("filePath");
        if (filePath == null || filePath.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'filePath' es requerido");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            String result = fileUserService.compareNIOvsIO(filePath);

            Map<String, Object> response = new HashMap<>();
            response.put("tool", "compare_nio_vs_io");
            response.put("input", filePath);
            response.put("result", result);
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error comparando NIO vs I/O: " + filePath, e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error comparando NIO vs I/O: " + e.getMessage());
            error.put("tool", "compare_nio_vs_io");
            error.put("input", filePath);
            error.put("status", "error");

            return ResponseEntity.status(500).body(error);
        }
    }

    // ========== RANDOM ACCESS ENDPOINTS ==========

    /**
     * Lee desde posición específica con RandomAccessFile
     */
    @PostMapping("/random/read")
    public ResponseEntity<Map<String, Object>> randomRead(@RequestBody Map<String, Object> request) {
        logger.debug("Lectura aleatoria en archivo");

        String filePath = (String) request.get("filePath");
        Long position = request.get("position") != null ?
                ((Number) request.get("position")).longValue() : null;
        Integer length = request.get("length") != null ?
                ((Number) request.get("length")).intValue() : null;

        if (filePath == null || filePath.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'filePath' es requerido");
            return ResponseEntity.badRequest().body(error);
        }

        if (position == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'position' es requerido");
            return ResponseEntity.badRequest().body(error);
        }

        if (length == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'length' es requerido");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            String result = fileUserService.randomAccessRead(filePath, position, length);

            Map<String, Object> response = new HashMap<>();
            response.put("tool", "random_access_read");
            response.put("input", Map.of("filePath", filePath, "position", position, "length", length));
            response.put("result", result);
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error en lectura aleatoria: " + filePath, e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error en lectura aleatoria: " + e.getMessage());
            error.put("tool", "random_access_read");
            error.put("input", Map.of("filePath", filePath, "position", position, "length", length));
            error.put("status", "error");

            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Escribe en posición específica con RandomAccessFile
     */
    @PostMapping("/random/write")
    public ResponseEntity<Map<String, Object>> randomWrite(@RequestBody Map<String, Object> request) {
        logger.debug("Escritura aleatoria en archivo");

        String filePath = (String) request.get("filePath");
        Long position = request.get("position") != null ?
                ((Number) request.get("position")).longValue() : null;
        String content = (String) request.get("content");

        if (filePath == null || filePath.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'filePath' es requerido");
            return ResponseEntity.badRequest().body(error);
        }

        if (position == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'position' es requerido");
            return ResponseEntity.badRequest().body(error);
        }

        if (content == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'content' es requerido");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            boolean result = fileUserService.randomAccessWrite(filePath, position, content);

            Map<String, Object> response = new HashMap<>();
            response.put("tool", "random_access_write");
            response.put("input", Map.of("filePath", filePath, "position", position, "content", content));
            response.put("result", result);
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error en escritura aleatoria: " + filePath, e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error en escritura aleatoria: " + e.getMessage());
            error.put("tool", "random_access_write");
            error.put("input", Map.of("filePath", filePath, "position", position, "content", content));
            error.put("status", "error");

            return ResponseEntity.status(500).body(error);
        }
    }

    // ========== PROCESSING ENDPOINTS ==========

    /**
     * Convierte codificación de archivo
     */
    @PostMapping("/process/convert_encoding")
    public ResponseEntity<Map<String, Object>> convertEncoding(@RequestBody Map<String, String> request) {
        logger.debug("Convirtiendo codificación de archivo");

        String sourceFile = request.get("sourceFile");
        String targetFile = request.get("targetFile");
        String sourceCharset = request.get("sourceCharset");
        String targetCharset = request.get("targetCharset");

        if (sourceFile == null || sourceFile.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'sourceFile' es requerido");
            return ResponseEntity.badRequest().body(error);
        }

        if (targetFile == null || targetFile.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'targetFile' es requerido");
            return ResponseEntity.badRequest().body(error);
        }

        if (sourceCharset == null || sourceCharset.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'sourceCharset' es requerido");
            return ResponseEntity.badRequest().body(error);
        }

        if (targetCharset == null || targetCharset.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'targetCharset' es requerido");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            boolean result = fileUserService.convertFileEncoding(sourceFile, targetFile,
                    sourceCharset, targetCharset);

            Map<String, Object> response = new HashMap<>();
            response.put("tool", "convert_file_encoding");
            response.put("input", Map.of(
                    "sourceFile", sourceFile,
                    "targetFile", targetFile,
                    "sourceCharset", sourceCharset,
                    "targetCharset", targetCharset
            ));
            response.put("result", result);
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error convirtiendo codificación: " + sourceFile, e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error convirtiendo codificación: " + e.getMessage());
            error.put("tool", "convert_file_encoding");
            error.put("input", Map.of("sourceFile", sourceFile, "targetFile", targetFile));
            error.put("status", "error");

            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Crea archivo temporal
     */
    @PostMapping("/process/create_temp")
    public ResponseEntity<Map<String, Object>> createTemp(@RequestBody Map<String, String> request) {
        logger.debug("Creando archivo temporal");

        String prefix = request.get("prefix");
        String content = request.get("content");

        if (prefix == null || prefix.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'prefix' es requerido");
            return ResponseEntity.badRequest().body(error);
        }

        if (content == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'content' es requerido");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            String result = fileUserService.createTempFile(prefix, content);

            Map<String, Object> response = new HashMap<>();
            response.put("tool", "create_temp_file");
            response.put("input", Map.of("prefix", prefix, "content", content));
            response.put("result", result);
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error creando archivo temporal: " + prefix, e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error creando archivo temporal: " + e.getMessage());
            error.put("tool", "create_temp_file");
            error.put("input", Map.of("prefix", prefix));
            error.put("status", "error");

            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Formatea archivo de texto
     */
    @PostMapping("/process/format_text")
    public ResponseEntity<Map<String, Object>> formatText(@RequestBody Map<String, String> request) {
        logger.debug("Formateando archivo de texto");

        String sourceFile = request.get("sourceFile");
        if (sourceFile == null || sourceFile.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'sourceFile' es requerido");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            String result = fileUserService.formatTextFile(sourceFile);

            Map<String, Object> response = new HashMap<>();
            response.put("tool", "format_text_file");
            response.put("input", sourceFile);
            response.put("result", result);
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error formateando texto: " + sourceFile, e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error formateando texto: " + e.getMessage());
            error.put("tool", "format_text_file");
            error.put("input", sourceFile);
            error.put("status", "error");

            return ResponseEntity.status(500).body(error);
        }
    }
}