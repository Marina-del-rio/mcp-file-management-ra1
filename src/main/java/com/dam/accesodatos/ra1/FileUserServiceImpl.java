package com.dam.accesodatos.ra1;

import com.dam.accesodatos.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * IMPLEMENTACIÓN PARA ESTUDIANTES - RA1: Gestión de Ficheros

 * Esta clase contiene esqueletos de métodos que los estudiantes deben completar
 * para demostrar su comprensión del RA1 y sus criterios de evaluación.

 * INSTRUCCIONES:
 * 1. Completar cada método marcado con TODO
 * 2. Usar solo las clases Java I/O indicadas en comentarios
 * 3. Implementar manejo robusto de excepciones
 * 4. Ejecutar tests para validar implementación
 * 5. Documentar decisiones técnicas en comentarios

 * PROHIBIDO:
 * - Usar librerías externas no permitidas
 * - Copiar código sin entender
 * - Omitir manejo de excepciones
 */
@Service
public class FileUserServiceImpl implements FileUserService {

    private final ObjectMapper objectMapper;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public FileUserServiceImpl() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules(); // Para LocalDateTime
    }

    // ========================================================================================
    // CE1.a: ANÁLISIS DE CLASES RELACIONADAS CON TRATAMIENTO DE FICHEROS
    // ========================================================================================

    @Override
    public String getFileInfo(String filePath) {
        /*
         * TODO CE1.a: Implementar información detallada de archivo (actividad 1 de la presentación vista en clase)
         * 
         * Pasos requeridos:
         * 1. Crear objeto File con la ruta proporcionada
         * 2. Verificar que existe con exists()
         * 3. Determinar si es archivo o directorio con isFile()/isDirectory()
         * 4. Obtener tamaño con length() (solo para archivos)
         * 5. Obtener permisos con canRead(), canWrite(), canExecute()
         * 6. Formatear fecha de modificación con SimpleDateFormat
         * 7. Construir string con formato: "Tipo: archivo/directorio, Tamaño: X bytes, Permisos: rwx, Fecha: ..."
         * 
         * Clases requeridas:
         * - File (exists, isFile, isDirectory, length, lastModified, canRead, canWrite, canExecute)
         * - SimpleDateFormat (para formatear fecha)
         * - Date (para convertir timestamp)
         */
        
        // Paso 1: Crear objeto File con la ruta proporcionada
        File file = new File(filePath);
        
        // Paso 2: Verificar que existe
        if (!file.exists()) {
            return "Error: El archivo o directorio no existe: " + filePath;
        }
        
        // Paso 3: Determinar si es archivo o directorio
        String tipo = file.isFile() ? "archivo" : "directorio";
        
        // Paso 4: Obtener tamaño (solo para archivos)
        String tamano;
        if (file.isFile()) {
            tamano = file.length() + " bytes";
        } else {
            tamano = "N/A (directorio)";
        }
        
        // Paso 5: Obtener permisos
        String permisos = "";
        permisos += file.canRead() ? "r" : "-";
        permisos += file.canWrite() ? "w" : "-";
        permisos += file.canExecute() ? "x" : "-";
        
        // Paso 6: Formatear fecha de modificación
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date fechaModificacion = new Date(file.lastModified());
        String fechaFormateada = formatter.format(fechaModificacion);
        
        // Paso 7: Construir string con formato especificado
        return String.format("Tipo: %s, Tamaño: %s, Permisos: %s, Fecha: %s", 
                           tipo, tamano, permisos, fechaFormateada);
    }

    @Override
    public String compareIOPerformance(String filePath) {
        /*
         * TODO CE1.a: Implementar comparación de rendimiento I/O con y sin buffering
         * 
         * Pasos requeridos:
         * 1. Validar que archivo existe
         * 2. Primera prueba: leer con FileReader (sin buffer)
         *    - Usar System.currentTimeMillis() antes y después
         *    - Leer carácter por carácter con read()
         * 3. Segunda prueba: leer con BufferedReader
         *    - Medir tiempo de la misma manera
         *    - Usar readLine() para leer líneas
         * 4. Calcular diferencia de tiempo
         * 5. Retornar comparación formateada
         * 
         * Formato sugerido:
         * "FileReader: 1250ms
         *  BufferedReader: 45ms
         *  Mejora: 96.4% más rápido con buffer"
         */
        
        // TODO: Implementar aquí
        //1. Validar que el archivo existe
        File file = new File(filePath);
        if  (!file.exists()) {
            return "El fichero no existe: " + filePath;
        }

        long fileReaderTime = 0;
        long bufferedReaderTime = 0;

        // 2. Primera prueba: leer con FileReader (sin buffer)
        try (FileReader reader = new FileReader(file)) {
            long startTime = System.currentTimeMillis();//inicio tiempo

            while (reader.read() != -1) {
                //lee cada linea hasta el final
            }
            long endTime = System.currentTimeMillis();//termina el tiempo cuando termina de leer cada linea
            fileReaderTime = endTime - startTime;//calculo de tiempo sin buffer
        } catch (IOException e) {
            return "Error durante la lectura con FileReader: " + e.getMessage();
        }

        // 3. Segunda prueba: leer con BufferedReader (con buffer)
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            long startTime = System.currentTimeMillis();//inicio

            while (bufferedReader.readLine() != null) {
                // Lee línea por línea hasta el final del archivo (null)
            }
            long endTime = System.currentTimeMillis();//fin
            bufferedReaderTime = endTime - startTime;//calculo
        } catch (IOException e) {
            return "Error durante la lectura con BufferedReader: " + e.getMessage();
        }
        // 4. Calcular la diferencia de tiempo
        long diferencia = fileReaderTime - bufferedReaderTime;

        // 5. Retornar comparación formateada con la diferencia
        return String.format(
                "FileReader: %dms\n" +
                        "BufferedReader: %dms\n" +
                        "Diferencia: %dms",
                fileReaderTime,
                bufferedReaderTime,
                diferencia
        );
    }

    @Override
    public String compareNIOvsIO(String filePath) {
        /*
         * TODO CE1.a: Implementar comparación entre enfoques NIO y IO tradicional
         * 
         * Pasos requeridos:
         * 1. Enfoque tradicional (java.io):
         *    - Usar File para verificar existencia
         *    - Usar BufferedReader para leer líneas
         *    - Contar líneas manualmente
         * 2. Enfoque NIO (java.nio.file):
         *    - Usar Path y Files.exists() para verificar
         *    - Usar Files.readAllLines() para leer todo de una vez
         *    - Obtener tamaño de lista directamente
         * 3. Comparar sintaxis y funcionalidades
         * 4. Medir tiempo de ejecución de ambos
         * 5. Retornar comparación formateada
         * 
         * Formato sugerido:
         * "IO Tradicional: 15 líneas de código, 45ms
         *  NIO: 3 líneas de código, 32ms
         *  NIO es más conciso y eficiente"
         */
        
        // TODO: Implementar aquí
        int lineCountIO = 0;
        int lineCountNIO = 0;
        long timeIO = 0;
        long timeNIO = 0;
        //1. Enfoque tradicional (java.io):
        try{
            long startTime = System.currentTimeMillis();//inicio IO
            //   - Usar File para verificar existencia
            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                //   - Usar BufferedReader para leer líneas
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    while (reader.readLine() != null) {
                        //   - Contar lineas manualmente
                        lineCountIO++;
                    }
                }
            } else {
                return "Error con IO: El archivo no existe en la ruta especificada.";
            }
            timeIO = System.currentTimeMillis() - startTime;//fin timer IO y calculo
        } catch (IOException e) {
            return "Error durante la lectura con IO tradicional: " + e.getMessage();
        }
        //2. Enfoque NIO (java.nio.file):
        try{
            long startTime = System.currentTimeMillis();//inicio NIO
            //   - Usar Path  y Files.exists() para verificar
            Path path = Paths.get(filePath);
            if (Files.exists(path) && Files.isRegularFile(path)) {
                //   - Usar Files.readAllLines() para leer todo de una vez
                List<String> allLines = Files.readAllLines(path);
                //   - Obtener tamaño de lista directamente
                lineCountNIO = allLines.size();
            } else {
                return "Error con NIO: El archivo no existe en la ruta especificada.";
            }
            timeNIO = System.currentTimeMillis() - startTime;//fin NIO y calculo
        } catch (IOException e) {
            return "Error durante la lectura con NIOvsIO: " + e.getMessage();
        }

        // 3. Comparar sintaxis y funcionalidades
        final int codeLinesIO = 5;
        final int codeLinesNIO = 4;
        // 4. Medir tiempo de ejecucion de ambos
        // 5. Retornar comparacion formateada
        return String.format(
                "IO Tradicional: %d líneas de código, %dms\n" +
                        "NIO: %d líneas de código, %dms\n" +
                        "NIO es más conciso y más eficiente.",
                codeLinesIO,
                timeIO,
                codeLinesNIO,
                timeNIO
        );
    }

    // ========================================================================================
    // CE1.b: UTILIZACIÓN DE FLUJOS PARA ACCESO A INFORMACIÓN EN FICHEROS
    // ========================================================================================

    @Override
    public String searchTextInFile(String filePath, String searchText) {
        /*
         * TODO CE1.b: Implementar búsqueda de texto en archivo (actividad 4 de la presentación vista en clase)
         * 
         * Pasos requeridos:
         * 1. Validar que archivo existe
         * 2. Usar BufferedReader para leer línea por línea
         * 3. Para cada línea: usar String.contains() o indexOf() para buscar
         * 4. Llevar contador de línea actual
         * 5. Acumular resultados: número de línea y contenido donde aparece
         * 6. Contar total de ocurrencias
         * 7. Retornar string formateado con resultados
         * 
         * Formato sugerido:
         * "Línea 5: contenido de la línea donde aparece texto
         *  Línea 12: otra línea con el texto
         *  Total: 2 ocurrencias encontradas"
         */
        
        // TODO: Implementar aquí
        // 1. Validar que archivo existe
        File file = new File(filePath);
        if (!file.exists()) {
            return "El fichero no existe: " + filePath;
        }
        int lineaActual=0;
        Map<Integer, String> busquedas = new HashMap<>();//LinkedHashMap a modo de diccionario de busquedas
        // 2. Usar BufferedReader para leer línea por línea
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String linea;
            while ((linea = reader.readLine())!= null) {
                // 4. Llevar contador de línea actual
                lineaActual++;
                //3. Para cada línea: usar String.contains() o indexOf() para buscar
                if(linea.contains(searchText)){
                    //5. Acumular resultados: número de línea y contenido donde aparece
                    busquedas.put(lineaActual, linea);
                }
            }
            // 6. Contar total de ocurrencias
            int cantOcurrencias = busquedas.size();
            // 7. Retornar string formateado con resultados


            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Integer, String> entry : busquedas.entrySet()) {
                sb.append("Línea ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            sb.append("Total: ").append(cantOcurrencias).append("ocurrencias encontradas");
            String resultado = sb.toString();
            return resultado;
        }catch (IOException e){
            return "Error durante la lectura con FileReader: " + e.getMessage();
        }

    }

    @Override
    public String randomAccessRead(String filePath, long position, int length) {
        /*
         * TODO CE1.b: Implementar lectura desde posición específica
         * 
         * Pasos requeridos:
         * 1. Validar que archivo existe
         * 2. Crear RandomAccessFile en modo "r" (solo lectura)
         * 3. Usar seek(position) para posicionar puntero
         * 4. Crear buffer de bytes del tamaño especificado
         * 5. Usar read(buffer) para leer datos
         * 6. Convertir bytes a String
         * 7. Manejar EOF si posición está fuera del archivo
         * 
         * Clases requeridas:
         * - RandomAccessFile (constructor con modo "r", seek, read)
         * - String constructor para convertir bytes
         */
        
        // TODO: Implementar aquí
        // 1. Validar que archivo existe
        File file = new File(filePath);
        if (!file.exists()) {
            return "El fichero no existe: " + filePath;
        }
        // 2. Crear RandomAccessFile en modo "r" (solo lectura)
        try(RandomAccessFile raf = new RandomAccessFile(file, "r")){

            //7. Manejar EOF si posición está fuera del archivo
            if (position > raf.length()) {
                return "Posición fuera del tamaño del archivo.";
            }
            // 3. Usar seek(position) para posicionar punter
            raf.seek(position);//mover puntero al byte especificado
            // 4. Crear buffer de bytes del tamaño especificado
            byte[] buffer = new byte[length];
            // 5. Usar read(buffer) para leer datos
            int bytesRead = raf.read(buffer);
            // 6. Convertir bytes a String
            if (bytesRead > 0) {
                return new String(buffer, 0, bytesRead);//converetir los bytes en una cadena usando la codificacion por defecto
            } else {
                return "Fin de archivo alcanzado o no se pudo leer.";
            }

        }catch (EOFException e) {
            return "Posición fuera del tamaño del archivo (EOF alcanzado).";
        }catch (IOException e) {
            return "Error al convertir codificación: " + e.getMessage();
        }
    }

    @Override
    public boolean randomAccessWrite(String filePath, long position, String content) {
        /*
         * TODO CE1.b: Implementar escritura en posición específica
         * 
         * Pasos requeridos:
         * 1. Crear directorios padre si no existen
         * 2. Crear RandomAccessFile en modo "rw" (lectura/escritura)
         * 3. Usar seek(position) para posicionar puntero
         * 4. Convertir content a bytes con getBytes()
         * 5. Usar write(bytes) para escribir datos
         * 6. Cerrar archivo con close()
         * 7. Retornar true si exitoso
         * 
         * Clases requeridas:
         * - RandomAccessFile (constructor con modo "rw", seek, write)
         * - String.getBytes() para obtener bytes
         */
        
        // TODO: Implementar aquí
        File file = new File(filePath);

        // 1. Crear directorios padre si no existen
        File dirPadre = file.getParentFile();
        if(dirPadre != null && !dirPadre.exists()) {
            dirPadre.mkdirs();
        }
        RandomAccessFile raf = null;
        //2. Crear RandomAccessFile en modo "rw" (lectura/escritura)
        try{
            raf = new RandomAccessFile(file, "rw");
            //3. Usar seek(position) para posicionar puntero
            raf.seek(position);
            //4. Convertir content a bytes con getBytes()
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            //5. Usar write(bytes) para escribir datos
            raf.write(bytes);

            // 7. Retornar true si exitoso
            return true;
        }catch (IOException e) {
            System.out.println("Error al convertir codificación: " + e.getMessage());
            return false;
        }finally {//* 6. Cerrar archivo con close()
            if(raf != null) {
                try {
                    raf.close();
                } catch (IOException ignored) {}
            }
        }
    }

    @Override
    public boolean convertFileEncoding(String sourceFile, String targetFile, 
                                     String sourceCharset, String targetCharset) {
        /*
         * TODO CE1.b: Implementar conversión entre codificaciones (UTF-8, ISO-8859-1)
         * 
         * Pasos requeridos:
         * 1. Validar que archivo origen existe
         * 2. Crear InputStreamReader con FileInputStream y charset origen
         * 3. Crear OutputStreamWriter con FileOutputStream y charset destino
         * 4. Envolver con BufferedReader/BufferedWriter para eficiencia
         * 5. Leer línea por línea y escribir con nueva codificación
         * 6. Usar try-with-resources para cerrar recursos
         * 7. Retornar true si conversión exitosa
         * 
         * Ejemplo: convertir de ISO-8859-1 a UTF-8
         * InputStreamReader isr = new InputStreamReader(new FileInputStream(source), "ISO-8859-1");
         * OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(target), "UTF-8");
         */
        
        // TODO: Implementar aquí
        //1. Validar que archivo origen existe
        File source = new File(sourceFile);
        if (!source.exists()) {
            System.out.println("El archivo origen no existe: " + sourceFile);
            return false;
        }
        //6. Usar try-with-resources para cerrar recursos
        try( //2. Crear InputStreamReader con FileInputStream y charset origen
             InputStreamReader isr = new InputStreamReader(new FileInputStream(source), sourceCharset);
             //3. Crear OutputStreamWriter con FileOutputStream y charset destino
             OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(targetFile), targetCharset);
             //4. Envolver con BufferedReader/BufferedWriter para eficiencia
             BufferedReader br = new BufferedReader(isr);
             BufferedWriter bw = new BufferedWriter(osw)
        ){
            //5. Leer línea por línea y escribir con nueva codificación
            String linea;
            while((linea = br.readLine()) != null) {
                bw.write(linea);
                bw.newLine();
            }
            //.7. Retornar true si conversión exitosa
            return true;

        }catch(IOException e) {
            System.out.println("Error al convertir la codificación: " + e.getMessage());
            return false;
        }

    }

    // ========================================================================================
    // CE1.c: UTILIZACIÓN DE CLASES PARA GESTIÓN DE FICHEROS Y DIRECTORIOS
    // ========================================================================================

    @Override
    public List<String> listUserFiles(String directoryPath) {
        /*
         * TODO CE1.c: Implementar listado de archivos usando Java I/O
         * 
         * Pasos requeridos:
         * 1. Validar que directoryPath existe y es directorio
         * 2. Usar Files.list() o File.listFiles()
         * 3. Filtrar solo archivos (no directorios)
         * 4. Filtrar por extensiones: .csv, .json, .xml
         * 5. Retornar lista de nombres de archivo
         * 
         * Clases I/O requeridas:
         * - Files, Paths
         * - Stream API para filtrado
         * - FilenameFilter o predicados
         */
        
        // TODO: Implementar aquí
        //1. Validar que directoryPath existe y es directorio
        Path dirtPath = Paths.get(directoryPath);
        if(!Files.exists(dirtPath) || !Files.isDirectory(dirtPath)) {
            System.out.println("El archivo no existe: " + dirtPath);
            return List.of();//Devuelve lista vacia
        }
        // 2. Usar Files.list() o File.listFiles()
        try(Stream<Path> paths = Files.list(dirtPath)){//flujo de elementos tipo Path
            //5. Retornar lista de nombres de archivo
            return paths
                    //3. Filtrar solo archivos (no directorios)
                    .filter(Files::isRegularFile)
                    //4. Filtrar por extensiones: .csv, .json, .xml
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase();
                        return name.endsWith(".csv") || name.endsWith(".json") || name.endsWith(".xml");
                    })
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());

        }catch(IOException e) {
            System.out.println("Error al listar archivos: " + e.getMessage());
            return List.of(); // Devuelve lista vacía en caso de error
        }
    }

    @Override
    public boolean validateDirectoryStructure(String basePath) {
        /*
         * TODO CE1.c: Implementar validación de estructura de directorios
         * 
         * Pasos requeridos:
         * 1. Definir estructura esperada (ej: data/, exports/, temp/)
         * 2. Para cada directorio: verificar si existe
         * 3. Crear directorios faltantes con Files.createDirectories()
         * 4. Verificar permisos de lectura/escritura
         * 5. Retornar true si todo está correcto
         * 
         * Clases I/O requeridas:
         * - Files, Paths
         * - Files.exists(), Files.isDirectory()
         * - Files.createDirectories()
         * - Files.isReadable(), Files.isWritable()
         */
        
        // TODO: Implementar aquí
        // 1. Definir estructura esperada (ej: data/, exports/, temp/)
        String[] subdirs = {"data", "exports", "temp"};

        try{
            //2. Para cada directorio: verificar si existe
            for(String subdir: subdirs){
                Path dirPath = Paths.get(basePath);
                if(!Files.exists(dirPath)){
                    Files.createDirectories(dirPath);//3. Crear directorios faltantes con Files.createDirectories()
                }
                //4. Verificar permisos de lectura/escritura
                if(!Files.isDirectory(dirPath) || !Files.isReadable(dirPath) || !Files.isWritable(dirPath)){
                    System.out.println("El archivo no existe: " + dirPath);
                    return false;
                }
            }
            //5. Retornar true si todo está correcto
            return true;
        }catch (IOException e){
            System.out.println("Error al validar estructura de directorios: " + e.getMessage());
            return false;
        }

        // Estructura sugerida:
        // basePath/
        //   ├── data/       (archivos de datos)
        //   ├── exports/    (archivos exportados)
        //   └── temp/       (archivos temporales)
    }

    @Override
    public String createTempFile(String prefix, String content) {
        /*
         * TODO CE1.c: Implementar creación de archivo temporal
         * 
         * Pasos requeridos:
         * 1. Usar File.createTempFile(prefix, ".tmp") para crear archivo temporal
         * 2. Obtener ruta absoluta con getAbsolutePath()
         * 3. Escribir contenido usando FileWriter
         * 4. Cerrar recursos correctamente
         * 5. Retornar ruta del archivo temporal creado
         * 
         * NOTA: Los archivos temporales se crean en directorio del sistema
         * (ej: C:\\Users\\usuario\\AppData\\Local\\Temp en Windows)
         * 
         * Clases requeridas:
         * - File.createTempFile() (método estático)
         * - FileWriter para escribir contenido
         */
        
        // TODO: Implementar aquí
        try{
            //1. Usar File.createTempFile(prefix, ".tmp") para crear archivo temporal
            File tempFile = File.createTempFile(prefix, ".temp");

            //2.Obtener ruta absoluta con getAbsolutePath()
            String absolutePath = tempFile.getAbsolutePath();
            //3. Escribir contenido usando FileWriter
            try(FileWriter writer = new FileWriter(tempFile)) {//4. Cerrar recursos correctamente
                writer.write(content);
            }
            //5. Retornar ruta del archivo temporal creado
            return absolutePath;

        } catch (IOException e) {
            return "Error al crear el archivo: " + e.getMessage();
        }
    }

    @Override
    public String formatTextFile(String sourceFile) {
        /*
         * TODO CE1.c: Implementar formateador basado en ejemplo ArreglarFichero de la presentación vista en clase
         * 
         * Pasos requeridos (basado en ArreglarFichero de la presentación):
         * 1. Validar que archivo origen existe
         * 2. Crear archivo temporal para resultado
         * 3. Leer archivo línea por línea con BufferedReader
         * 4. Para cada línea, procesar carácter por carácter:
         *    - Eliminar espacios al principio de línea (princLinea flag)
         *    - Sustituir múltiples espacios consecutivos por uno solo (espacios flag)
         *    - Convertir primera letra de línea a mayúscula (primerLetra flag)
         *    - Mantener otros caracteres como están
         * 5. Escribir línea procesada a archivo temporal
         * 6. Retornar ruta del archivo temporal
         * 
         * Variables de control sugeridas:
         * - boolean princLinea = true (inicio de línea)
         * - boolean espacios = false (espacios consecutivos)
         * - boolean primerLetra = false (primera letra encontrada)
         */
        
        // TODO: Implementar aquí
        throw new UnsupportedOperationException("TODO: Implementar formatTextFile basado en ejemplo ArreglarFichero de la presentación vista en clase");
    }

    // ========================================================================================
    // CE1.d: ESCRITURA Y LECTURA DE INFORMACIÓN EN FORMATO XML
    // ========================================================================================

    @Override
    public List<User> readUsersFromXML(String filePath) {
        /*
         * TODO CE1.d: Implementar lectura de XML usando DOM parser
         * 
         * Pasos requeridos:
         * 1. Crear DocumentBuilderFactory y DocumentBuilder
         * 2. Usar DocumentBuilder.parse() para obtener Document
         * 3. Obtener todos los elementos "user" con getElementsByTagName()
         * 4. Para cada elemento user: extraer texto de cada campo
         * 5. Convertir texto a tipos apropiados (Long, Boolean, LocalDateTime)
         * 6. Crear objeto User con los datos extraídos
         * 
         * Clases XML requeridas:
         * - DocumentBuilderFactory, DocumentBuilder
         * - Document, Element, NodeList
         * - NO usar JAXB automático
         */
        
        List<User> users = new ArrayList<>();
        
        // TODO: Implementar aquí
        throw new UnsupportedOperationException("TODO: Implementar readUsersFromXML usando DOM parser");
        
        // ESTRUCTURA XML esperada:
        // <users>
        //   <user>
        //     <id>1</id>
        //     <name>Juan</name>
        //     ... otros campos
        //   </user>
        // </users>
    }

    @Override
    public boolean writeUsersToXML(List<User> users, String filePath) {
        /*
         * TODO CE1.d: Implementar escritura de XML usando DOM y Transformer
         * 
         * Pasos requeridos:
         * 1. Crear DocumentBuilderFactory y DocumentBuilder
         * 2. Crear nuevo Document con createElement()
         * 3. Crear elemento raíz "users"
         * 4. Para cada User: crear elemento "user" con subelementos
         * 5. Usar Transformer para escribir Document a archivo
         * 6. Configurar Transformer para pretty-print (setOutputProperty)
         * 
         * Clases XML requeridas:
         * - DocumentBuilderFactory, DocumentBuilder, Document
         * - Element (createElement, appendChild, setTextContent)
         * - TransformerFactory, Transformer
         * - DOMSource, StreamResult
         */
        
        // TODO: Implementar aquí
        throw new UnsupportedOperationException("TODO: Implementar writeUsersToXML usando DOM y Transformer");
    }

    @Override
    public List<User> readUsersFromXMLSAX(String filePath) {
        /*
         * TODO CE1.d: Implementar lectura de XML usando SAX parser (alternativa eficiente)
         * 
         * Pasos requeridos:
         * 1. Crear SAXParserFactory y SAXParser
         * 2. Implementar DefaultHandler personalizado:
         *    - startElement(): detectar inicio de elementos
         *    - characters(): capturar contenido de texto
         *    - endElement(): procesar fin de elementos
         * 3. Mantener estado durante parsing (usuario actual, campo actual)
         * 4. Usar SAXParser.parse() con el handler
         * 
         * Clases SAX requeridas:
         * - SAXParserFactory, SAXParser
         * - DefaultHandler (clase anónima o interna)
         * - Atributos de estado para tracking
         */
        
        List<User> users = new ArrayList<>();
        
        // TODO: Implementar aquí
        throw new UnsupportedOperationException("TODO: Implementar readUsersFromXMLSAX usando SAX parser");
        
        // PISTA: Crear clase que extienda DefaultHandler
        /*
        class UserSAXHandler extends DefaultHandler {
            // TODO: Implementar startElement, characters, endElement
        }
        */
    }

    // ========================================================================================
    // CE1.e: ESCRITURA Y LECTURA DE INFORMACIÓN EN FORMATO JSON
    // ========================================================================================

    @Override
    public List<User> readUsersFromJSON(String filePath) {
        /*
         * TODO CE1.e: Implementar lectura de JSON usando Jackson
         * 
         * Pasos requeridos:
         * 1. Validar que archivo existe
         * 2. Usar ObjectMapper.readValue() con TypeReference para List<User>
         * 3. Manejar excepciones de Jackson apropiadamente
         * 4. Retornar lista vacía si archivo está vacío
         * 
         * Clases requeridas:
         * - ObjectMapper (ya creado como campo)
         * - TypeReference<List<User>>
         * - File (para pasar a readValue)
         */
        
        // TODO: Implementar aquí
        throw new UnsupportedOperationException("TODO: Implementar readUsersFromJSON usando Jackson ObjectMapper");
        
        // PISTA: objectMapper.readValue(new File(filePath), new TypeReference<List<User>>() {});
    }

    @Override
    public boolean writeUsersToJSON(List<User> users, String filePath) {
        /*
         * TODO CE1.e: Implementar escritura de JSON usando Jackson
         * 
         * Pasos requeridos:
         * 1. Crear directorios padre si no existen
         * 2. Configurar ObjectMapper para pretty-print
         * 3. Usar ObjectMapper.writeValue() para escribir a archivo
         * 4. Manejar excepciones apropiadamente
         */
        
        // TODO: Implementar aquí
        throw new UnsupportedOperationException("TODO: Implementar writeUsersToJSON usando Jackson ObjectMapper");
    }

    // ========================================================================================
    // CE1.f: ESCRITURA Y LECTURA DE INFORMACIÓN EN OTROS FORMATOS ESTÁNDAR (CSV)
    // ========================================================================================

    @Override
    public List<User> readUsersFromCSV(String filePath) {
        /*
         * TODO CE1.f: Implementar lectura de CSV usando Java I/O vanilla
         * 
         * Pasos requeridos:
         * 1. Validar que el archivo existe usando Files.exists()
         * 2. Usar BufferedReader con FileReader para leer líneas
         * 3. Saltear la primera línea (cabeceras)
         * 4. Para cada línea: usar String.split(",") para separar campos
         * 5. Convertir cada línea a objeto User
         * 6. Manejar parsing de LocalDateTime desde String
         * 7. Usar try-with-resources para garantizar cierre de recursos
         * 8. Lanzar RuntimeException con mensaje descriptivo si hay errores
         * 
         * Clases Java I/O requeridas:
         * - Files, Paths (validación)
         * - FileReader, BufferedReader (lectura)
         * - String.split() (parsing)
         * - LocalDateTime.parse() (conversión fechas)
         */
        
        List<User> users = new ArrayList<>();
        
        // TODO: Implementar aquí
        throw new UnsupportedOperationException("TODO: Implementar readUsersFromCSV usando BufferedReader");
        
        // EJEMPLO de estructura esperada:
        // try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
        //     String line = reader.readLine(); // Saltear cabeceras
        //     while ((line = reader.readLine()) != null) {
        //         String[] fields = line.split(",");
        //         // TODO: Convertir fields a User
        //     }
        // } catch (IOException e) {
        //     throw new RuntimeException("Error leyendo CSV: " + e.getMessage(), e);
        // }
    }

    @Override
    public boolean writeUsersToCSV(List<User> users, String filePath) {
        /*
         * TODO CE1.f: Implementar escritura de CSV usando Java I/O vanilla
         * 
         * Pasos requeridos:
         * 1. Crear directorios padre si no existen usando Files.createDirectories()
         * 2. Usar PrintWriter con FileWriter para escribir
         * 3. Escribir línea de cabeceras CSV
         * 4. Para cada User: formatear campos separados por comas
         * 5. Manejar formato de LocalDateTime a String
         * 6. Usar try-with-resources
         * 7. Retornar true si exitoso, lanzar RuntimeException si error
         * 
         * Clases Java I/O requeridas:
         * - Files, Paths (crear directorios)
         * - FileWriter, PrintWriter (escritura)
         * - DateTimeFormatter (formateo fechas)
         */
        
        // TODO: Implementar aquí
        throw new UnsupportedOperationException("TODO: Implementar writeUsersToCSV usando PrintWriter");
    }


    // ========================================================================================
    // MÉTODOS AUXILIARES SUGERIDOS
    // ========================================================================================
    // Los estudiantes pueden crear estos métodos privados para ayudar en las implementaciones

    /**
     * TODO: Método auxiliar para convertir String CSV a User
     * @param csvLine Línea CSV con campos separados por comas
     * @return User creado desde la línea CSV
     */
    private User parseUserFromCSV(String csvLine) {
        // TODO: Implementar parsing de línea CSV a User
        throw new UnsupportedOperationException("TODO: Implementar parseUserFromCSV");
    }

    /**
     * TODO: Método auxiliar para convertir User a String CSV
     * @param user Usuario a convertir
     * @return Línea CSV con campos del usuario
     */
    private String userToCSV(User user) {
        // TODO: Implementar conversión de User a línea CSV
        throw new UnsupportedOperationException("TODO: Implementar userToCSV");
    }

    /**
     * TODO: Método auxiliar para crear directorios padre de un archivo
     * @param filePath Ruta del archivo
     */
    private void createParentDirectories(String filePath) {
        // TODO: Implementar creación de directorios padre
        // PISTA: Path parent = Paths.get(filePath).getParent();
        throw new UnsupportedOperationException("TODO: Implementar createParentDirectories");
    }
}