package com.dam.accesodatos.ra1;

import com.dam.accesodatos.model.User;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests TDD para RA1: Gestión de Ficheros
 * 
 * PROPÓSITO EDUCATIVO:
 * - Estos tests FALLARÁN hasta que los estudiantes implementen los métodos
 * - Cada test valida un criterio de evaluación específico
 * - Los estudiantes deben hacer que todos los tests pasen VERDES
 * - NO modificar estos tests, solo implementar FileUserServiceImpl
 * 
 * METODOLOGÍA TDD:
 * 1. Ejecutar tests → ROJO (fallan)
 * 2. Implementar código mínimo → VERDE (pasan)
 * 3. Refactorizar código → VERDE (mantener funcionalidad)
 */
@SpringBootTest
@TestPropertySource(properties = "spring.main.web-application-type=none")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FileUserServiceTest {

    private FileUserService fileUserService;
    private Path testDataDir;
    private List<User> testUsers;

    @BeforeEach
    void setUp() throws IOException {
        fileUserService = new FileUserServiceImpl();
        
        // Crear directorio temporal para tests
        testDataDir = Files.createTempDirectory("ra1_test_");
        
        // Crear usuarios de ejemplo para tests
        testUsers = Arrays.asList(
            createTestUser(1L, "Juan Pérez", "juan@example.com", "IT", "Developer"),
            createTestUser(2L, "María García", "maria@example.com", "HR", "Manager"),
            createTestUser(3L, "Carlos López", "carlos@example.com", "Finance", "Analyst")
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        // Limpiar archivos temporales
        Files.walk(testDataDir)
             .sorted((a, b) -> b.compareTo(a)) // Borrar archivos antes que directorios
             .forEach(path -> {
                 try {
                     Files.deleteIfExists(path);
                 } catch (IOException e) {
                     // Ignorar errores de limpieza
                 }
             });
    }

    // ================================
    // TESTS RA1-CE1.f: FORMATO CSV
    // ================================

    @Test
    @Order(1)
    @DisplayName("CE1.f: writeUsersToCSV debe crear archivo CSV válido")
    void testWriteUsersToCSV_CreatesValidFile() {
        // Given
        String csvPath = testDataDir.resolve("users.csv").toString();
        
        // When
        boolean result = fileUserService.writeUsersToCSV(testUsers, csvPath);
        
        // Then
        assertTrue(result, "writeUsersToCSV debe retornar true");
        assertTrue(Files.exists(Paths.get(csvPath)), "Archivo CSV debe existir");
        
        // Verificar contenido básico
        try {
            List<String> lines = Files.readAllLines(Paths.get(csvPath));
            assertTrue(lines.size() >= 4, "CSV debe tener cabecera + 3 usuarios");
            assertTrue(lines.get(0).contains("id,name,email"), "Primera línea debe ser cabecera");
            assertTrue(lines.get(1).contains("Juan Pérez"), "Debe contener datos del primer usuario");
        } catch (IOException e) {
            fail("No se pudo leer el archivo CSV generado: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("CE1.f: readUsersFromCSV debe leer archivo CSV correctamente")
    void testReadUsersFromCSV_ReadsValidFile() throws IOException {
        // Given - Crear archivo CSV de prueba
        String csvPath = testDataDir.resolve("test_users.csv").toString();
        String csvContent = """
            id,name,email,department,role,active,createdAt,updatedAt
            1,Juan Pérez,juan@example.com,IT,Developer,true,2024-01-01T10:00:00,2024-01-01T10:00:00
            2,María García,maria@example.com,HR,Manager,true,2024-01-02T11:00:00,2024-01-02T11:00:00
            """;
        Files.writeString(Paths.get(csvPath), csvContent);
        
        // When
        List<User> users = fileUserService.readUsersFromCSV(csvPath);
        
        // Then
        assertNotNull(users, "readUsersFromCSV no debe retornar null");
        assertEquals(2, users.size(), "Debe leer exactamente 2 usuarios");
        
        User firstUser = users.get(0);
        assertEquals(1L, firstUser.getId(), "ID del primer usuario debe ser 1");
        assertEquals("Juan Pérez", firstUser.getName(), "Nombre debe coincidir");
        assertEquals("juan@example.com", firstUser.getEmail(), "Email debe coincidir");
        assertEquals("IT", firstUser.getDepartment(), "Departamento debe coincidir");
        assertEquals("Developer", firstUser.getRole(), "Rol debe coincidir");
        assertTrue(firstUser.getActive(), "Usuario debe estar activo");
    }

    @Test
    @Order(3)
    @DisplayName("CE1.f: CSV roundtrip debe mantener datos íntegros")
    void testCSV_RoundTrip_PreservesData() {
        // Given
        String csvPath = testDataDir.resolve("roundtrip.csv").toString();
        
        // When - Escribir y luego leer
        fileUserService.writeUsersToCSV(testUsers, csvPath);
        List<User> readUsers = fileUserService.readUsersFromCSV(csvPath);
        
        // Then - Datos deben ser idénticos
        assertEquals(testUsers.size(), readUsers.size(), "Número de usuarios debe coincidir");
        for (int i = 0; i < testUsers.size(); i++) {
            User original = testUsers.get(i);
            User read = readUsers.get(i);
            assertEquals(original.getId(), read.getId(), "ID debe coincidir");
            assertEquals(original.getName(), read.getName(), "Nombre debe coincidir");
            assertEquals(original.getEmail(), read.getEmail(), "Email debe coincidir");
        }
    }

    // ================================
    // TESTS RA1-CE1.e: FORMATO JSON
    // ================================

    @Test
    @Order(4)
    @DisplayName("CE1.e: writeUsersToJSON debe crear archivo JSON válido")
    void testWriteUsersToJSON_CreatesValidFile() {
        // Given
        String jsonPath = testDataDir.resolve("users.json").toString();
        
        // When
        boolean result = fileUserService.writeUsersToJSON(testUsers, jsonPath);
        
        // Then
        assertTrue(result, "writeUsersToJSON debe retornar true");
        assertTrue(Files.exists(Paths.get(jsonPath)), "Archivo JSON debe existir");
        
        // Verificar que es JSON válido
        try {
            String content = Files.readString(Paths.get(jsonPath));
            assertTrue(content.trim().startsWith("["), "JSON debe empezar con array");
            assertTrue(content.trim().endsWith("]"), "JSON debe terminar con array");
            assertTrue(content.contains("Juan Pérez"), "Debe contener datos del usuario");
        } catch (IOException e) {
            fail("No se pudo leer el archivo JSON generado: " + e.getMessage());
        }
    }

    @Test
    @Order(5)
    @DisplayName("CE1.e: readUsersFromJSON debe leer archivo JSON correctamente")
    void testReadUsersFromJSON_ReadsValidFile() throws IOException {
        // Given - Primero escribir JSON
        String jsonPath = testDataDir.resolve("test_users.json").toString();
        fileUserService.writeUsersToJSON(testUsers, jsonPath);
        
        // When
        List<User> users = fileUserService.readUsersFromJSON(jsonPath);
        
        // Then
        assertNotNull(users, "readUsersFromJSON no debe retornar null");
        assertEquals(3, users.size(), "Debe leer exactamente 3 usuarios");
        assertEquals("Juan Pérez", users.get(0).getName(), "Primer usuario debe coincidir");
    }

    // ================================
    // TESTS RA1-CE1.d: FORMATO XML
    // ================================

    @Test
    @Order(6)
    @DisplayName("CE1.d: writeUsersToXML debe crear archivo XML válido")
    void testWriteUsersToXML_CreatesValidFile() {
        // Given
        String xmlPath = testDataDir.resolve("users.xml").toString();
        
        // When
        boolean result = fileUserService.writeUsersToXML(testUsers, xmlPath);
        
        // Then
        assertTrue(result, "writeUsersToXML debe retornar true");
        assertTrue(Files.exists(Paths.get(xmlPath)), "Archivo XML debe existir");
        
        // Verificar estructura XML básica
        try {
            String content = Files.readString(Paths.get(xmlPath));
            assertTrue(content.contains("<users>"), "XML debe tener elemento raíz users");
            assertTrue(content.contains("<user>"), "XML debe tener elementos user");
            assertTrue(content.contains("<name>Juan Pérez</name>"), "XML debe contener datos");
            assertTrue(content.contains("</users>"), "XML debe cerrar elemento raíz");
        } catch (IOException e) {
            fail("No se pudo leer el archivo XML generado: " + e.getMessage());
        }
    }

    @Test
    @Order(7)
    @DisplayName("CE1.d: readUsersFromXML debe leer archivo XML con DOM")
    void testReadUsersFromXML_ReadsWithDOM() throws IOException {
        // Given - Primero escribir XML
        String xmlPath = testDataDir.resolve("test_users.xml").toString();
        fileUserService.writeUsersToXML(testUsers, xmlPath);
        
        // When
        List<User> users = fileUserService.readUsersFromXML(xmlPath);
        
        // Then
        assertNotNull(users, "readUsersFromXML no debe retornar null");
        assertEquals(3, users.size(), "Debe leer exactamente 3 usuarios");
        assertEquals("Juan Pérez", users.get(0).getName(), "Primer usuario debe coincidir");
    }

    @Test
    @Order(8)
    @DisplayName("CE1.d: readUsersFromXMLSAX debe leer archivo XML con SAX")
    void testReadUsersFromXMLSAX_ReadsWithSAX() throws IOException {
        // Given - Crear XML manualmente para SAX
        String xmlPath = testDataDir.resolve("sax_users.xml").toString();
        String xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <users>
                <user>
                    <id>1</id>
                    <name>Juan Pérez</name>
                    <email>juan@example.com</email>
                    <department>IT</department>
                    <role>Developer</role>
                    <active>true</active>
                    <createdAt>2024-01-01T10:00:00</createdAt>
                    <updatedAt>2024-01-01T10:00:00</updatedAt>
                </user>
            </users>
            """;
        Files.writeString(Paths.get(xmlPath), xmlContent);
        
        // When
        List<User> users = fileUserService.readUsersFromXMLSAX(xmlPath);
        
        // Then
        assertNotNull(users, "readUsersFromXMLSAX no debe retornar null");
        assertEquals(1, users.size(), "Debe leer exactamente 1 usuario");
        assertEquals("Juan Pérez", users.get(0).getName(), "Usuario debe coincidir");
    }

    // ================================
    // TESTS RA1-CE1.c: GESTIÓN DIRECTORIOS
    // ================================

    @Test
    @Order(9)
    @DisplayName("CE1.c: listUserFiles debe encontrar archivos de usuario")
    void testListUserFiles_FindsUserFiles() throws IOException {
        // Given - Crear archivos de diferentes tipos
        Files.writeString(testDataDir.resolve("users.csv"), "test");
        Files.writeString(testDataDir.resolve("users.json"), "test");
        Files.writeString(testDataDir.resolve("users.xml"), "test");
        Files.writeString(testDataDir.resolve("other.txt"), "test"); // No debe aparecer
        
        // When
        List<String> files = fileUserService.listUserFiles(testDataDir.toString());
        
        // Then
        assertNotNull(files, "listUserFiles no debe retornar null");
        assertTrue(files.size() >= 3, "Debe encontrar al menos 3 archivos de usuario");
        assertTrue(files.stream().anyMatch(f -> f.contains("users.csv")), "Debe encontrar CSV");
        assertTrue(files.stream().anyMatch(f -> f.contains("users.json")), "Debe encontrar JSON");
        assertTrue(files.stream().anyMatch(f -> f.contains("users.xml")), "Debe encontrar XML");
        assertFalse(files.stream().anyMatch(f -> f.contains("other.txt")), "No debe encontrar TXT");
    }

    @Test
    @Order(10)
    @DisplayName("CE1.c: validateDirectoryStructure debe crear estructura necesaria")
    void testValidateDirectoryStructure_CreatesDirectories() {
        // Given
        String basePath = testDataDir.resolve("project").toString();
        
        // When
        boolean result = fileUserService.validateDirectoryStructure(basePath);
        
        // Then
        assertTrue(result, "validateDirectoryStructure debe retornar true");
        
        // Verificar que se crearon directorios esperados
        assertTrue(Files.exists(Paths.get(basePath)), "Directorio base debe existir");
        // Los estudiantes deben definir qué estructura crean
    }

    // ================================
    // TESTS DE ERROR Y EDGE CASES
    // ================================

    @Test
    @Order(11)
    @DisplayName("readUsersFromCSV debe manejar archivo inexistente")
    void testReadUsersFromCSV_HandlesFileNotFound() {
        // Given
        String nonExistentPath = testDataDir.resolve("nonexistent.csv").toString();
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            fileUserService.readUsersFromCSV(nonExistentPath);
        }, "Debe lanzar RuntimeException para archivo inexistente");
    }

    @Test
    @Order(12)
    @DisplayName("Escritura debe crear directorios padre automáticamente")
    void testWrite_CreatesParentDirectories() {
        // Given
        String deepPath = testDataDir.resolve("deep/nested/dirs/users.json").toString();
        
        // When
        boolean result = fileUserService.writeUsersToJSON(testUsers, deepPath);
        
        // Then
        assertTrue(result, "Debe crear directorios padre automáticamente");
        assertTrue(Files.exists(Paths.get(deepPath)), "Archivo debe existir en ruta profunda");
    }

    // ================================
    // MÉTODOS AUXILIARES
    // ================================

    private User createTestUser(Long id, String name, String email, String department, String role) {
        User user = new User(id, name, email, department, role);
        user.setId(id);
        user.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        user.setUpdatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        return user;
    }
}