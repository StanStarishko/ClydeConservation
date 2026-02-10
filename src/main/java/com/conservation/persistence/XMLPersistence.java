package com.conservation.persistence;

import com.conservation.exception.PersistenceException;
import com.conservation.exception.ValidationException;
import com.conservation.model.*;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Generic XML persistence handler for conservation system entities.
 *
 * Provides methods to save and load collections of entities using clean XML format.
 * Automatically determines entity type from filename.
 */
public class XMLPersistence {

    private static final String DATA_DIRECTORY = "data/";
    private static final String SCHEMA_DIRECTORY = "data/schemas/";

    private XMLPersistence() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Saves a collection of entities to an XML file.
     *
     * @param entities collection of entities to save
     * @param filename filename or filepath
     * @param rootElement root XML element name
     * @throws PersistenceException if save operation fails
     */
    public static void saveToXML(Collection<?> entities, String filename, String rootElement)
            throws PersistenceException {

        // Normalise filepath - remove duplicate "data/" prefix if present
        String filepath = filename;
        if (filename.startsWith(DATA_DIRECTORY)) {
            filepath = filename;
        } else if (!filename.startsWith("/") && !filename.contains(":")) {
            // Relative path - add DATA_DIRECTORY prefix
            filepath = DATA_DIRECTORY + filename;
        }

        try {
            // Create parent directory for the target file
            File targetFile = new File(filepath);
            File parentDir = targetFile.getParentFile();

            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    throw new PersistenceException(
                            "Failed to create parent directory",
                            parentDir.getAbsolutePath()
                    );
                }
            }

            // Write to temporary file first
            String tempFilepath = filepath + ".tmp";
            File tempFile = new File(tempFilepath);

            // Ensure parent directory exists for temp file too
            File tempParentDir = tempFile.getParentFile();
            if (tempParentDir != null && !tempParentDir.exists()) {
                if (!tempParentDir.mkdirs()) {
                    throw new PersistenceException(
                            "Failed to create temporary directory",
                            tempParentDir.getAbsolutePath()
                    );
                }
            }

            // Write XML content
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFilepath))) {
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                writer.write("<" + rootElement + ">\n");

                for (Object entity : entities) {
                    String entityXml = convertEntityToXML(entity);
                    writer.write(entityXml);
                }

                writer.write("</" + rootElement + ">\n");
            }

            // Atomic rename: delete old file and rename temp file
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
     * Automatically determines entity type from filename.
     *
     * @param filename filename or filepath
     * @return collection of loaded entities
     * @throws PersistenceException if load operation fails
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> loadFromXML(String filename, Class<T> clazz)
            throws PersistenceException {

        String entityType = determineEntityType(filename);
        String expectedType = clazz.getSimpleName();
        if (!entityType.equals(expectedType)) {
            System.err.println("Warning: filename suggests " + entityType +
                    " but requested type is " + expectedType);
        }

        String filepath = filename.startsWith(DATA_DIRECTORY) ? filename : DATA_DIRECTORY + filename;

        File file = new File(filepath);
        if (!file.exists()) {
            throw new PersistenceException(
                    "File not found",
                    filepath
            );
        }

        Collection<T> entities = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            String line;
            StringBuilder entityBuffer = new StringBuilder();
            boolean inEntity = false;

            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();

                if (trimmed.startsWith("<animal>") || trimmed.startsWith("<keeper>") || trimmed.startsWith("<cage>")) {
                    inEntity = true;
                    entityBuffer.setLength(0);
                    entityBuffer.append(trimmed).append("\n");
                } else if (trimmed.startsWith("</animal>") || trimmed.startsWith("</keeper>") || trimmed.startsWith("</cage>")) {
                    entityBuffer.append(trimmed).append("\n");
                    inEntity = false;

                    T entity = (T) parseEntity(entityBuffer.toString(), entityType);
                    if (entity != null) {
                        entities.add(entity);
                    }
                } else if (inEntity) {
                    entityBuffer.append(trimmed).append("\n");
                }
            }

            System.out.println("Successfully loaded " + entities.size() +
                    " entities from " + filepath);
            return entities;

        } catch (FileNotFoundException fileNotFoundException) {
            throw new PersistenceException(
                    "File not found",
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
     * Converts entity object to XML string representation.
     */
    private static String convertEntityToXML(Object entity) {
        if (entity instanceof Animal) {
            return animalToXML((Animal) entity);
        } else if (entity instanceof Keeper) {
            return keeperToXML((Keeper) entity);
        } else if (entity instanceof Cage) {
            return cageToXML((Cage) entity);
        }
        return "";
    }

    /**
     * Converts Animal to XML format.
     */
    private static String animalToXML(Animal animal) {
        StringBuilder xml = new StringBuilder();
        xml.append("  <animal>\n");
        xml.append("    <animalId>").append(animal.getAnimalId()).append("</animalId>\n");
        xml.append("    <name>").append(escapeXml(animal.getName())).append("</name>\n");
        xml.append("    <type>").append(escapeXml(animal.getType())).append("</type>\n");
        xml.append("    <category>").append(animal.getCategory()).append("</category>\n");
        xml.append("    <dateOfBirth>").append(animal.getDateOfBirth()).append("</dateOfBirth>\n");
        xml.append("    <dateOfAcquisition>").append(animal.getDateOfAcquisition()).append("</dateOfAcquisition>\n");
        xml.append("    <sex>").append(animal.getSex()).append("</sex>\n");
        xml.append("  </animal>\n");
        return xml.toString();
    }

    /**
     * Converts Keeper to XML format.
     */
    private static String keeperToXML(Keeper keeper) {
        StringBuilder xml = new StringBuilder();
        xml.append("  <keeper>\n");
        xml.append("    <keeperId>").append(keeper.getKeeperId()).append("</keeperId>\n");
        xml.append("    <firstName>").append(escapeXml(keeper.getFirstName())).append("</firstName>\n");
        xml.append("    <surname>").append(escapeXml(keeper.getSurname())).append("</surname>\n");
        xml.append("    <address>").append(escapeXml(keeper.getAddress())).append("</address>\n");
        xml.append("    <contactNumber>").append(escapeXml(keeper.getContactNumber())).append("</contactNumber>\n");
        xml.append("    <position>").append(keeper.getPosition()).append("</position>\n");

        List<Integer> allocatedCages = keeper.getAllocatedCageIds();
        if (!allocatedCages.isEmpty()) {
            xml.append("    <allocatedCages>\n");
            for (Integer cageId : allocatedCages) {
                xml.append("      <cageId>").append(cageId).append("</cageId>\n");
            }
            xml.append("    </allocatedCages>\n");
        }

        xml.append("  </keeper>\n");
        return xml.toString();
    }

    /**
     * Converts Cage to XML format.
     */
    private static String cageToXML(Cage cage) {
        StringBuilder xml = new StringBuilder();
        xml.append("  <cage>\n");
        xml.append("    <cageId>").append(cage.getCageId()).append("</cageId>\n");
        xml.append("    <cageNumber>").append(escapeXml(cage.getCageNumber())).append("</cageNumber>\n");
        xml.append("    <description>").append(escapeXml(cage.getDescription())).append("</description>\n");
        xml.append("    <animalCapacity>").append(cage.getAnimalCapacity()).append("</animalCapacity>\n");

        List<Integer> currentAnimals = cage.getCurrentAnimalIds();
        if (!currentAnimals.isEmpty()) {
            xml.append("    <currentAnimals>\n");
            for (Integer animalId : currentAnimals) {
                xml.append("      <animalId>").append(animalId).append("</animalId>\n");
            }
            xml.append("    </currentAnimals>\n");
        }

        Integer assignedKeeper = cage.getAssignedKeeperId();
        if (assignedKeeper != null) {
            xml.append("    <assignedKeeper>").append(assignedKeeper).append("</assignedKeeper>\n");
        }

        xml.append("  </cage>\n");
        return xml.toString();
    }

    /**
     * Determines entity type from filename.
     */
    private static String determineEntityType(String filename) {
        String name = new File(filename).getName().toLowerCase();
        if (name.contains("animal")) {
            return "Animal";
        } else if (name.contains("keeper")) {
            return "Keeper";
        } else if (name.contains("cage")) {
            return "Cage";
        }
        return "Unknown";
    }

    /**
     * Parses XML string to entity object.
     */
    private static Object parseEntity(String xmlData, String entityType) {
        try {
            return switch (entityType) {
                case "Animal" -> parseAnimal(xmlData);
                case "Keeper" -> parseKeeper(xmlData);
                case "Cage" -> parseCage(xmlData);
                default -> null;
            };
        } catch (Exception exception) {
            System.err.println("Failed to parse entity: " + exception.getMessage());
            return null;
        }
    }

    /**
     * Parses Animal from XML.
     */
    private static Animal parseAnimal(String xmlData) throws ValidationException {
        int animalId = parseInt(extractValue(xmlData, "animalId"));
        String name = extractValue(xmlData, "name");
        String type = extractValue(xmlData, "type");
        Animal.Category category = Animal.Category.valueOf(extractValue(xmlData, "category"));
        LocalDate dateOfBirth = LocalDate.parse(extractValue(xmlData, "dateOfBirth"));
        LocalDate dateOfAcquisition = LocalDate.parse(extractValue(xmlData, "dateOfAcquisition"));
        Animal.Sex sex = Animal.Sex.valueOf(extractValue(xmlData, "sex"));

        Animal animal = new Animal(name, type, category, dateOfBirth, dateOfAcquisition, sex);
        animal.setAnimalId(animalId);
        return animal;
    }

    /**
     * Parses Keeper from XML.
     */
    private static Keeper parseKeeper(String xmlData) throws ValidationException {
        int keeperId = parseInt(extractValue(xmlData, "keeperId"));
        String firstName = extractValue(xmlData, "firstName");
        String surname = extractValue(xmlData, "surname");
        String address = extractValue(xmlData, "address");
        String contactNumber = extractValue(xmlData, "contactNumber");
        Keeper.Position position = Keeper.Position.valueOf(extractValue(xmlData, "position"));

        Keeper keeper;
        if (position == Keeper.Position.HEAD_KEEPER) {
            keeper = new HeadKeeper(firstName, surname, address, contactNumber);
        } else {
            keeper = new AssistantKeeper(firstName, surname, address, contactNumber);
        }
        keeper.setKeeperId(keeperId);

        List<Integer> allocatedCages = extractIntegerList(xmlData, "cageId");
        for (Integer cageId : allocatedCages) {
            keeper.allocateCage(cageId);
        }

        return keeper;
    }

    /**
     * Parses Cage from XML.
     */
    private static Cage parseCage(String xmlData) throws ValidationException {
        int cageId = parseInt(extractValue(xmlData, "cageId"));
        String cageNumber = extractValue(xmlData, "cageNumber");
        String description = extractValue(xmlData, "description");
        int animalCapacity = parseInt(extractValue(xmlData, "animalCapacity"));

        Cage cage = new Cage(cageNumber, description, animalCapacity);
        cage.setCageId(cageId);

        List<Integer> currentAnimals = extractIntegerList(xmlData, "animalId");
        for (Integer animalId : currentAnimals) {
            cage.addAnimal(animalId);
        }

        String assignedKeeperStr = extractValue(xmlData, "assignedKeeper");
        if (!assignedKeeperStr.isEmpty()) {
            cage.setAssignedKeeperId(parseInt(assignedKeeperStr));
        }

        return cage;
    }

    /**
     * Extracts value between XML tags.
     */
    private static String extractValue(String xmlData, String tagName) {
        String openTag = "<" + tagName + ">";
        String closeTag = "</" + tagName + ">";

        int startIdx = xmlData.indexOf(openTag);
        if (startIdx == -1) {
            return "";
        }

        startIdx += openTag.length();
        int endIdx = xmlData.indexOf(closeTag, startIdx);
        if (endIdx == -1) {
            return "";
        }

        return xmlData.substring(startIdx, endIdx).trim();
    }

    /**
     * Extracts list of integers from XML.
     */
    private static List<Integer> extractIntegerList(String xmlData, String tagName) {
        List<Integer> values = new ArrayList<>();
        String openTag = "<" + tagName + ">";
        String closeTag = "</" + tagName + ">";

        int searchFrom = 0;
        while (true) {
            int startIdx = xmlData.indexOf(openTag, searchFrom);
            if (startIdx == -1) {
                break;
            }

            startIdx += openTag.length();
            int endIdx = xmlData.indexOf(closeTag, startIdx);
            if (endIdx == -1) {
                break;
            }

            String value = xmlData.substring(startIdx, endIdx).trim();
            try {
                values.add(Integer.parseInt(value));
            } catch (NumberFormatException numberFormatException) {
                System.err.println("Invalid integer value: " + value);
            }

            searchFrom = endIdx + closeTag.length();
        }

        return values;
    }

    /**
     * Parses string to integer safely.
     */
    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException numberFormatException) {
            return 0;
        }
    }

    /**
     * Escapes special XML characters.
     */
    private static String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    /**
     * Validates an XML file against an XSD schema.
     *
     * @param xmlFilePath full path to the XML file
     * @param xsdFilePath full path to the XSD schema file
     * @return true if XML is valid against the schema
     * @throws PersistenceException if files cannot be read or schema is invalid
     */
    public static boolean validateXML(String xmlFilePath, String xsdFilePath)
            throws PersistenceException {

        File xmlFile = new File(xmlFilePath);
        File xsdFile = new File(xsdFilePath);

        if (!xmlFile.exists()) {
            throw new PersistenceException(
                    "XML file not found for validation: " + xmlFilePath,
                    xmlFilePath
            );
        }

        if (!xsdFile.exists()) {
            throw new PersistenceException(
                    "XSD schema file not found: " + xsdFilePath,
                    xsdFilePath
            );
        }

        try {
            javax.xml.validation.SchemaFactory schemaFactory =
                    javax.xml.validation.SchemaFactory.newInstance(
                            javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);

            javax.xml.validation.Schema schema =
                    schemaFactory.newSchema(xsdFile);

            javax.xml.validation.Validator validator = schema.newValidator();
            validator.validate(new javax.xml.transform.stream.StreamSource(xmlFile));

            return true;

        } catch (org.xml.sax.SAXException saxException) {
            return false;
        } catch (IOException ioException) {
            throw new PersistenceException(
                    "Failed to validate XML against schema: " + ioException.getMessage(),
                    xmlFilePath,
                    ioException
            );
        }
    }

    public static boolean fileExists(String filename) {
        File file = new File(DATA_DIRECTORY + filename);
        return file.exists();
    }

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

    public static boolean backupFile(String filename) {
        String sourcePath = DATA_DIRECTORY + filename;
        String backupPath = DATA_DIRECTORY + filename + ".backup";

        try {
            File source = new File(sourcePath);
            File backup = new File(backupPath);

            if (!source.exists()) {
                return false;
            }

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

    public static String getDataDirectory() {
        return DATA_DIRECTORY;
    }

    public static String getSchemaDirectory() {
        return SCHEMA_DIRECTORY;
    }
}