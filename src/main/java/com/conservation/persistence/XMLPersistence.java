package com.conservation.persistence;

import com.conservation.exception.PersistenceException;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Generic XML persistence handler for conservation system entities.
 * 
 * Provides methods to:
 * - Save collections of entities to XML files
 * - Load collections of entities from XML files
 * - Validate XML against XSD schemas (future enhancement)
 * 
 * Note: This is a simplified XML handler.
 * In production, consider using JAXB, Jackson XML, or similar libraries.
 */
public class XMLPersistence {
    
    private static final String DATA_DIRECTORY = "data/";
    private static final String SCHEMA_DIRECTORY = "data/schemas/";
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private XMLPersistence() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Saves a collection of entities to an XML file.
     * 
     * Creates parent directory if it doesn't exist.
     * Overwrites existing file.
     * 
     * @param entities collection of entities to save
     * @param filename filename (e.g., "animals.xml")
     * @param rootElement root XML element name (e.g., "animals")
     * @throws PersistenceException if save operation fails
     */
    public static void saveToXML(Collection<?> entities, String filename, String rootElement) 
            throws PersistenceException {
        
        String filepath = DATA_DIRECTORY + filename;
        
        try {
            // Create data directory if it doesn't exist
            File dataDir = new File(DATA_DIRECTORY);
            if (!dataDir.exists()) {
                if (!dataDir.mkdirs()) {
                    throw new PersistenceException(
                        "Failed to create data directory",
                        DATA_DIRECTORY
                    );
                }
            }
            
            // Write to temporary file first (for rollback on error)
            String tempFilepath = filepath + ".tmp";
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFilepath))) {
                // Write XML header
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                writer.write("<" + rootElement + ">\n");
                
                // Write each entity (assumes entities have meaningful toString or custom XML method)
                for (Object entity : entities) {
                    String entityXML = convertEntityToXML(entity);
                    writer.write(entityXML);
                    writer.write("\n");
                }
                
                // Write closing tag
                writer.write("</" + rootElement + ">\n");
            }
            
            // If write successful, rename temp file to actual file
            File tempFile = new File(tempFilepath);
            File actualFile = new File(filepath);
            
            if (actualFile.exists()) {
                if (!actualFile.delete()) {
                    throw new PersistenceException(
                        "Failed to delete old file before save",
                        filepath
                    );
                }
            }
            
            if (!tempFile.renameTo(actualFile)) {
                throw new PersistenceException(
                    "Failed to rename temporary file",
                    tempFilepath
                );
            }
            
            System.out.println("Successfully saved " + entities.size() + 
                             " entities to " + filepath);
            
        } catch (IOException ioException) {
            throw new PersistenceException(
                "Failed to write XML file",
                filepath,
                ioException
            );
        }
    }
    
    /**
     * Loads entities from an XML file.
     * 
     * Note: This is a placeholder for XML deserialization.
     * In a real implementation, would use JAXB or similar.
     * 
     * @param filename filename (e.g., "animals.xml")
     * @return collection of entities loaded
     * @throws PersistenceException if load operation fails
     */
    public static Collection<String> loadFromXML(String filename) throws PersistenceException {
        String filepath = DATA_DIRECTORY + filename;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            Collection<String> entities = new ArrayList<>();
            String line;
            
            while ((line = reader.readLine()) != null) {
                // Skip XML declaration and root elements
                if (line.trim().startsWith("<?xml") || 
                    line.trim().startsWith("<animals>") ||
                    line.trim().startsWith("</animals>") ||
                    line.trim().startsWith("<keepers>") ||
                    line.trim().startsWith("</keepers>") ||
                    line.trim().startsWith("<cages>") ||
                    line.trim().startsWith("</cages>")) {
                    continue;
                }
                
                if (!line.trim().isEmpty()) {
                    entities.add(line.trim());
                }
            }
            
            System.out.println("Successfully loaded " + entities.size() + 
                             " entities from " + filepath);
            
            return entities;
            
        } catch (FileNotFoundException fileNotFoundException) {
            throw new PersistenceException(
                "XML file not found",
                filepath,
                fileNotFoundException
            );

        } catch (IOException ioException) {
            throw new PersistenceException(
                "Failed to read XML file",
                filepath,
                ioException
            );
        }
    }
    
    /**
     * Validates an XML file against its XSD schema.
     * 
     * Note: This is a placeholder for XSD validation.
     * In a real implementation, would use javax.xml.validation.
     * 
     * @param filename XML filename to validate
     * @param schemaFilename XSD schema filename
     * @return true if validation passes, false otherwise
     */
    public static boolean validateWithXSD(String filename, String schemaFilename) {
        // Placeholder for XSD validation
        // In production:
        // 1. Load XSD schema from SCHEMA_DIRECTORY + schemaFilename
        // 2. Parse XML from DATA_DIRECTORY + filename
        // 3. Validate XML against schema
        // 4. Return validation result
        
        System.out.println("XSD validation not yet implemented");
        return true;
    }
    
    /**
     * Checks if a data file exists.
     * 
     * @param filename filename to check
     * @return true if file exists, false otherwise
     */
    public static boolean fileExists(String filename) {
        File file = new File(DATA_DIRECTORY + filename);
        return file.exists();
    }
    
    /**
     * Deletes a data file.
     * 
     * Use with caution!
     * 
     * @param filename filename to delete
     * @return true if file was deleted, false otherwise
     */
    public static boolean deleteFile(String filename) {
        File file = new File(DATA_DIRECTORY + filename);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                System.out.println("Deleted file: " + filename);
            }
            return deleted;
        }
        return false;
    }
    
    /**
     * Creates a backup of a data file.
     * 
     * @param filename filename to backup
     * @return true if backup created successfully, false otherwise
     */
    public static boolean backupFile(String filename) {
        String sourcePath = DATA_DIRECTORY + filename;
        String backupPath = DATA_DIRECTORY + filename + ".backup";
        
        try {
            File source = new File(sourcePath);
            File backup = new File(backupPath);
            
            if (!source.exists()) {
                return false;
            }
            
            // Copy file
            try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(source));
                 BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(backup))) {
                
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            
            System.out.println("Backup created: " + backupPath);
            return true;
            
        } catch (IOException ioException) {
            System.err.println("Failed to create backup: " + ioException.getMessage());
            return false;
        }
    }
    
    /**
     * Converts an entity object to XML string representation.
     * 
     * This is a simplified version.
     * In production, entities should implement IPersistable.toXML()
     * or use JAXB annotations.
     * 
     * @param entity the entity to convert
     * @return XML string representation
     */
    private static String convertEntityToXML(Object entity) {
        // Placeholder implementation
        // In production, would:
        // 1. Check if entity implements IPersistable
        // 2. Call entity.toXML()
        // 3. Or use reflection/JAXB to generate XML
        
        return "    <!-- Entity: " + entity.getClass().getSimpleName() + " -->\n" +
               "    <!-- Full XML serialization to be implemented -->";
    }
    
    /**
     * Gets the data directory path.
     * 
     * @return data directory path
     */
    public static String getDataDirectory() {
        return DATA_DIRECTORY;
    }
    
    /**
     * Gets the schema directory path.
     * 
     * @return schema directory path
     */
    public static String getSchemaDirectory() {
        return SCHEMA_DIRECTORY;
    }
}
