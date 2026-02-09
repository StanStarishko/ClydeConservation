package com.conservation.persistence;

import com.conservation.exception.PersistenceException;
import com.conservation.exception.ValidationException;
import com.conservation.model.Animal;
import com.conservation.model.Animal.Category;
import com.conservation.model.Animal.Sex;
import com.conservation.model.Keeper;
import com.conservation.model.Keeper.Position;
import com.conservation.model.HeadKeeper;
import com.conservation.model.AssistantKeeper;
import com.conservation.model.Cage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for XMLPersistence - generic XML serialisation and deserialisation.
 * Tests save/load operations, XSD validation, error handling, and rollback behaviour.
 * 
 * <p>Uses JUnit 5 TempDir for isolated file system testing to avoid
 * polluting the actual data directory.
 * 
 * @author Clyde Conservation Development Team
 * @version 1.0
 */
@DisplayName("XMLPersistence Tests")
class XMLPersistenceTest {

    // ============================================================
    // Test Constants
    // ============================================================
    
    private static final LocalDate SAMPLE_BIRTH_DATE = LocalDate.of(2020, 5, 15);
    private static final LocalDate SAMPLE_ACQUISITION_DATE = LocalDate.of(2023, 11, 20);
    
    private static final String ANIMALS_XML = "animals.xml";
    private static final String KEEPERS_XML = "keepers.xml";
    private static final String CAGES_XML = "cages.xml";
    
    // ============================================================
    // Test Directory (JUnit 5 TempDir)
    // ============================================================
    
    @TempDir
    Path tempDir;
    
    private Path dataDir;
    private Path schemaDir;
    
    // ============================================================
    // Setup and Teardown
    // ============================================================
    
    @BeforeEach
    void setupDirectories() throws IOException {
        // Create data and schema directories in temp folder
        dataDir = tempDir.resolve("data");
        schemaDir = tempDir.resolve("data/schemas");
        Files.createDirectories(schemaDir);
        
        // Create sample XSD schemas for validation tests
        createSampleSchemas();
    }
    
    /**
     * Creates minimal XSD schema files for testing validation.
     * These are simplified versions of the actual schemas.
     */
    private void createSampleSchemas() throws IOException {
        // Animal XSD schema
        String animalXsd = """
            <?xml version="1.0" encoding="UTF-8"?>
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
                <xs:element name="animals">
                    <xs:complexType>
                        <xs:sequence>
                            <xs:element name="animal" minOccurs="0" maxOccurs="unbounded">
                                <xs:complexType>
                                    <xs:sequence>
                                        <xs:element name="animalId" type="xs:positiveInteger"/>
                                        <xs:element name="name" type="xs:string"/>
                                        <xs:element name="type" type="xs:string"/>
                                        <xs:element name="category">
                                            <xs:simpleType>
                                                <xs:restriction base="xs:string">
                                                    <xs:enumeration value="PREDATOR"/>
                                                    <xs:enumeration value="PREY"/>
                                                </xs:restriction>
                                            </xs:simpleType>
                                        </xs:element>
                                        <xs:element name="dateOfBirth" type="xs:date"/>
                                        <xs:element name="dateOfAcquisition" type="xs:date"/>
                                        <xs:element name="sex">
                                            <xs:simpleType>
                                                <xs:restriction base="xs:string">
                                                    <xs:enumeration value="MALE"/>
                                                    <xs:enumeration value="FEMALE"/>
                                                </xs:restriction>
                                            </xs:simpleType>
                                        </xs:element>
                                    </xs:sequence>
                                </xs:complexType>
                            </xs:element>
                        </xs:sequence>
                    </xs:complexType>
                </xs:element>
            </xs:schema>
            """;
        Files.writeString(schemaDir.resolve("animal.xsd"), animalXsd);
        
        // Keeper XSD schema (simplified)
        String keeperXsd = """
            <?xml version="1.0" encoding="UTF-8"?>
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
                <xs:element name="keepers">
                    <xs:complexType>
                        <xs:sequence>
                            <xs:element name="keeper" minOccurs="0" maxOccurs="unbounded">
                                <xs:complexType>
                                    <xs:sequence>
                                        <xs:element name="keeperId" type="xs:positiveInteger"/>
                                        <xs:element name="firstName" type="xs:string"/>
                                        <xs:element name="surname" type="xs:string"/>
                                        <xs:element name="address" type="xs:string"/>
                                        <xs:element name="contactNumber" type="xs:string"/>
                                        <xs:element name="position" type="xs:string"/>
                                        <xs:element name="allocatedCages" minOccurs="0">
                                            <xs:complexType>
                                                <xs:sequence>
                                                    <xs:element name="cageId" type="xs:positiveInteger" minOccurs="0" maxOccurs="4"/>
                                                </xs:sequence>
                                            </xs:complexType>
                                        </xs:element>
                                    </xs:sequence>
                                </xs:complexType>
                            </xs:element>
                        </xs:sequence>
                    </xs:complexType>
                </xs:element>
            </xs:schema>
            """;
        Files.writeString(schemaDir.resolve("keeper.xsd"), keeperXsd);
        
        // Cage XSD schema (simplified)
        String cageXsd = """
            <?xml version="1.0" encoding="UTF-8"?>
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
                <xs:element name="cages">
                    <xs:complexType>
                        <xs:sequence>
                            <xs:element name="cage" minOccurs="0" maxOccurs="unbounded">
                                <xs:complexType>
                                    <xs:sequence>
                                        <xs:element name="cageId" type="xs:positiveInteger"/>
                                        <xs:element name="cageNumber" type="xs:string"/>
                                        <xs:element name="description" type="xs:string"/>
                                        <xs:element name="animalCapacity" type="xs:positiveInteger"/>
                                        <xs:element name="currentAnimals" minOccurs="0">
                                            <xs:complexType>
                                                <xs:sequence>
                                                    <xs:element name="animalId" type="xs:positiveInteger" minOccurs="0" maxOccurs="unbounded"/>
                                                </xs:sequence>
                                            </xs:complexType>
                                        </xs:element>
                                        <xs:element name="assignedKeeper" type="xs:positiveInteger" minOccurs="0"/>
                                    </xs:sequence>
                                </xs:complexType>
                            </xs:element>
                        </xs:sequence>
                    </xs:complexType>
                </xs:element>
            </xs:schema>
            """;
        Files.writeString(schemaDir.resolve("cage.xsd"), cageXsd);
    }

    // ============================================================
    // ANIMAL XML PERSISTENCE TESTS
    // ============================================================
    
    @Nested
    @DisplayName("Animal XML Persistence Tests")
    class AnimalXmlPersistenceTests {
        
        @Test
        @DisplayName("Save animals to XML creates valid file")
        void saveToXml_Animals_ShouldCreateValidFile() throws PersistenceException, IOException, ValidationException {
            // Arrange
            List<Animal> animals = createSampleAnimals();
            Path xmlPath = dataDir.resolve(ANIMALS_XML);
            
            // Act
            XMLPersistence.saveToXML(animals, xmlPath.toString(), "animals");
            
            // Assert
            assertTrue(Files.exists(xmlPath), "XML file should be created");
            String content = Files.readString(xmlPath);
            assertTrue(content.contains("<?xml"), "File should have XML declaration");
            assertTrue(content.contains("<animals>"), "File should have root element");
            assertTrue(content.contains("<animal>"), "File should contain animal elements");
        }
        
        @Test
        @DisplayName("Save and load animals preserves all data")
        void saveAndLoad_Animals_ShouldPreserveData() throws PersistenceException, ValidationException {
            // Arrange
            List<Animal> originalAnimals = createSampleAnimals();
            Path xmlPath = dataDir.resolve(ANIMALS_XML);
            
            // Act
            XMLPersistence.saveToXML(originalAnimals, xmlPath.toString(), "animals");
            Collection<Animal> loadedAnimals = XMLPersistence.loadFromXML(
                xmlPath.toString(), Animal.class
            );
            
            // Assert
            assertEquals(originalAnimals.size(), loadedAnimals.size(), 
                "Loaded animals count should match original");
            
            // Verify first animal data
            Animal originalFirst = originalAnimals.get(0);
            Animal loadedFirst = loadedAnimals.stream()
                .filter(animal -> animal.getAnimalId() == originalFirst.getAnimalId())
                .findFirst()
                .orElse(null);
            
            assertNotNull(loadedFirst, "Should find matching animal by ID");
            assertEquals(originalFirst.getName(), loadedFirst.getName(), "Name should match");
            assertEquals(originalFirst.getType(), loadedFirst.getType(), "Type should match");
            assertEquals(originalFirst.getCategory(), loadedFirst.getCategory(), "Category should match");
            assertEquals(originalFirst.getSex(), loadedFirst.getSex(), "Sex should match");
        }
        
        @Test
        @DisplayName("Save empty collection creates valid empty XML")
        void saveToXml_EmptyCollection_ShouldCreateValidEmptyXml() throws PersistenceException, IOException {
            // Arrange
            List<Animal> emptyList = new ArrayList<>();
            Path xmlPath = dataDir.resolve(ANIMALS_XML);
            
            // Act
            XMLPersistence.saveToXML(emptyList, xmlPath.toString(), "animals");
            
            // Assert
            assertTrue(Files.exists(xmlPath), "XML file should be created");
            String content = Files.readString(xmlPath);
            assertTrue(content.contains("<animals>"), "Should have root element");
            assertTrue(content.contains("</animals>"), "Should have closing root element");
        }
        
        @Test
        @DisplayName("Load from non-existent file throws PersistenceException")
        void loadFromXml_NonExistentFile_ShouldThrowException() {
            // Arrange
            String nonExistentPath = dataDir.resolve("nonexistent.xml").toString();
            
            // Act & Assert
            assertThrows(PersistenceException.class, () -> XMLPersistence.loadFromXML(nonExistentPath, Animal.class), "Should throw PersistenceException for non-existent file");
        }
        
        @Test
        @DisplayName("Load from empty file returns empty collection")
        void loadFromXml_EmptyFile_ShouldReturnEmptyCollection() throws PersistenceException, IOException {
            // Arrange
            Path xmlPath = dataDir.resolve(ANIMALS_XML);
            String emptyXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <animals>
                </animals>
                """;
            Files.writeString(xmlPath, emptyXml);
            
            // Act
            Collection<Animal> loadedAnimals = XMLPersistence.loadFromXML(
                xmlPath.toString(), Animal.class
            );
            
            // Assert
            assertNotNull(loadedAnimals, "Should return collection, not null");
            assertTrue(loadedAnimals.isEmpty(), "Collection should be empty");
        }
    }

    // ============================================================
    // KEEPER XML PERSISTENCE TESTS
    // ============================================================
    
    @Nested
    @DisplayName("Keeper XML Persistence Tests")
    class KeeperXmlPersistenceTests {
        
        @Test
        @DisplayName("Save keepers preserves polymorphic types")
        void saveAndLoad_Keepers_ShouldPreservePolymorphicTypes() throws PersistenceException, ValidationException {
            // Arrange
            List<Keeper> keepers = createSampleKeepers();
            Path xmlPath = dataDir.resolve(KEEPERS_XML);
            
            // Act
            XMLPersistence.saveToXML(keepers, xmlPath.toString(), "keepers");
            Collection<Keeper> loadedKeepers = XMLPersistence.loadFromXML(
                xmlPath.toString(), Keeper.class
            );
            
            // Assert
            assertEquals(keepers.size(), loadedKeepers.size(), 
                "Loaded keepers count should match original");
            
            // Verify position types are preserved
            long headKeeperCount = loadedKeepers.stream()
                .filter(keeper -> keeper.getPosition() == Position.HEAD_KEEPER)
                .count();
            long assistantCount = loadedKeepers.stream()
                .filter(keeper -> keeper.getPosition() == Position.ASSISTANT_KEEPER)
                .count();
            
            assertTrue(headKeeperCount > 0, "Should have head keepers");
            assertTrue(assistantCount > 0, "Should have assistant keepers");
        }
        
        @Test
        @DisplayName("Save keepers with allocated cages preserves cage IDs")
        void saveAndLoad_KeepersWithCages_ShouldPreserveCageIds() throws PersistenceException, ValidationException {
            // Arrange
            HeadKeeper keeper = new HeadKeeper("John", "Smith", "123 Main St", "07123456789");
            setKeeperId(keeper, 1);
            keeper.allocateCage(1);
            keeper.allocateCage(2);
            keeper.allocateCage(3);
            
            List<Keeper> keepers = List.of(keeper);
            Path xmlPath = dataDir.resolve(KEEPERS_XML);
            
            // Act
            XMLPersistence.saveToXML(keepers, xmlPath.toString(), "keepers");
            Collection<Keeper> loadedKeepers = XMLPersistence.loadFromXML(
                xmlPath.toString(), Keeper.class
            );
            
            // Assert
            Keeper loadedKeeper = loadedKeepers.iterator().next();
            List<Integer> allocatedCages = loadedKeeper.getAllocatedCageIds();
            
            assertEquals(3, allocatedCages.size(), "Should have 3 allocated cages");
            assertTrue(allocatedCages.contains(1), "Should contain cage 1");
            assertTrue(allocatedCages.contains(2), "Should contain cage 2");
            assertTrue(allocatedCages.contains(3), "Should contain cage 3");
        }
    }

    // ============================================================
    // CAGE XML PERSISTENCE TESTS
    // ============================================================
    
    @Nested
    @DisplayName("Cage XML Persistence Tests")
    class CageXmlPersistenceTests {
        
        @Test
        @DisplayName("Save cages preserves animal IDs and keeper assignment")
        void saveAndLoad_Cages_ShouldPreserveRelationships() throws PersistenceException, ValidationException {
            // Arrange
            Cage cage = new Cage("Large-01", "Large predator cage", 10);
            setCageId(cage, 1);
            cage.addAnimal(1);
            cage.addAnimal(2);
            cage.setAssignedKeeperId(5);
            
            List<Cage> cages = List.of(cage);
            Path xmlPath = dataDir.resolve(CAGES_XML);
            
            // Act
            XMLPersistence.saveToXML(cages, xmlPath.toString(), "cages");
            Collection<Cage> loadedCages = XMLPersistence.loadFromXML(
                xmlPath.toString(), Cage.class
            );
            
            // Assert
            Cage loadedCage = loadedCages.iterator().next();
            
            assertEquals("Large-01", loadedCage.getCageNumber(), "Cage number should match");
            assertEquals(10, loadedCage.getAnimalCapacity(), "Capacity should match");
            assertEquals(2, loadedCage.getCurrentAnimalIds().size(), "Should have 2 animals");
            assertEquals(Integer.valueOf(5), loadedCage.getAssignedKeeperId(),
                "Assigned keeper should match");
        }
        
        @Test
        @DisplayName("Save multiple cages of different sizes")
        void saveAndLoad_MultipleCages_ShouldPreserveAll() throws PersistenceException, ValidationException {
            // Arrange
            List<Cage> cages = createSampleCages();
            Path xmlPath = dataDir.resolve(CAGES_XML);
            
            // Act
            XMLPersistence.saveToXML(cages, xmlPath.toString(), "cages");
            Collection<Cage> loadedCages = XMLPersistence.loadFromXML(
                xmlPath.toString(), Cage.class
            );
            
            // Assert
            assertEquals(cages.size(), loadedCages.size(), "All cages should be loaded");
            
            // Verify different capacities exist
            boolean hasLarge = loadedCages.stream().anyMatch(cage -> cage.getAnimalCapacity() == 10);
            boolean hasMedium = loadedCages.stream().anyMatch(cage -> cage.getAnimalCapacity() == 5);
            boolean hasSmall = loadedCages.stream().anyMatch(cage -> cage.getAnimalCapacity() == 1);
            
            assertTrue(hasLarge, "Should have large cage");
            assertTrue(hasMedium, "Should have medium cage");
            assertTrue(hasSmall, "Should have small cage");
        }
    }

    // ============================================================
    // XSD VALIDATION TESTS
    // ============================================================
    
    @Nested
    @DisplayName("XSD Validation Tests")
    class XsdValidationTests {
        
        @Test
        @DisplayName("Validate valid XML against XSD returns true")
        void validateXml_ValidXml_ShouldReturnTrue() throws PersistenceException, ValidationException {
            // Arrange
            List<Animal> animals = createSampleAnimals();
            Path xmlPath = dataDir.resolve(ANIMALS_XML);
            Path xsdPath = schemaDir.resolve("animal.xsd");
            
            XMLPersistence.saveToXML(animals, xmlPath.toString(), "animals");
            
            // Act
            boolean isValid = XMLPersistence.validateXML(xmlPath.toString(), xsdPath.toString());
            
            // Assert
            assertTrue(isValid, "Valid XML should pass XSD validation");
        }
        
        @Test
        @DisplayName("Validate invalid XML against XSD throws or returns false")
        void validateXml_InvalidXml_ShouldFailValidation() throws IOException {
            // Arrange - Create XML with invalid structure (missing required element)
            Path xmlPath = dataDir.resolve("invalid_animals.xml");
            String invalidXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <animals>
                    <animal>
                        <animalId>1</animalId>
                        <name>Leo</name>
                        <!-- Missing required elements: type, category, dates, sex -->
                    </animal>
                </animals>
                """;
            Files.writeString(xmlPath, invalidXml);
            Path xsdPath = schemaDir.resolve("animal.xsd");
            
            // Act & Assert
            // Either throws PersistenceException or returns false
            try {
                boolean isValid = XMLPersistence.validateXML(xmlPath.toString(), xsdPath.toString());
                assertFalse(isValid, "Invalid XML should fail validation");
            } catch (PersistenceException exception) {
                // This is also acceptable behaviour
                assertTrue(exception.getMessage().toLowerCase().contains("valid") ||
                          exception.getMessage().toLowerCase().contains("schema") ||
                          exception.getMessage().toLowerCase().contains("xml"),
                    "Exception should mention validation failure");
            }
        }
        
        @Test
        @DisplayName("Validate with non-existent XSD throws PersistenceException")
        void validateXml_NonExistentXsd_ShouldThrowException() throws PersistenceException, ValidationException {
            // Arrange
            List<Animal> animals = createSampleAnimals();
            Path xmlPath = dataDir.resolve(ANIMALS_XML);
            XMLPersistence.saveToXML(animals, xmlPath.toString(), "animals");
            
            String nonExistentXsd = schemaDir.resolve("nonexistent.xsd").toString();
            
            // Act & Assert
            assertThrows(PersistenceException.class, () -> XMLPersistence.validateXML(xmlPath.toString(), nonExistentXsd), "Should throw exception for non-existent XSD");
        }
        
        @Test
        @DisplayName("Validate XML with invalid enum value fails")
        void validateXml_InvalidEnumValue_ShouldFailValidation() throws IOException {
            // Arrange - Create XML with invalid category value
            Path xmlPath = dataDir.resolve("invalid_enum.xml");
            String invalidXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <animals>
                    <animal>
                        <animalId>1</animalId>
                        <name>Leo</name>
                        <type>Tiger</type>
                        <category>INVALID_CATEGORY</category>
                        <dateOfBirth>2020-05-15</dateOfBirth>
                        <dateOfAcquisition>2023-11-20</dateOfAcquisition>
                        <sex>MALE</sex>
                    </animal>
                </animals>
                """;
            Files.writeString(xmlPath, invalidXml);
            Path xsdPath = schemaDir.resolve("animal.xsd");
            
            // Act & Assert
            try {
                boolean isValid = XMLPersistence.validateXML(xmlPath.toString(), xsdPath.toString());
                assertFalse(isValid, "Invalid enum value should fail validation");
            } catch (PersistenceException exception) {
                // Also acceptable
                assertNotNull(exception.getMessage());
            }
        }
    }

    // ============================================================
    // ERROR HANDLING AND ROLLBACK TESTS
    // ============================================================
    
    @Nested
    @DisplayName("Error Handling and Rollback Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Load corrupt XML throws PersistenceException")
        void loadFromXml_CorruptXml_ShouldThrowException() throws IOException {
            // Arrange - Create corrupt XML
            Path xmlPath = dataDir.resolve("corrupt.xml");
            String corruptXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <animals>
                    <animal>
                        <animalId>1</animalId>
                        <name>Leo
                        <!-- Missing closing tags -->
                """;
            Files.writeString(xmlPath, corruptXml);
            
            // Act & Assert
            PersistenceException exception = assertThrows(PersistenceException.class, () -> XMLPersistence.loadFromXML(xmlPath.toString(), Animal.class), "Should throw PersistenceException for corrupt XML");
            
            assertNotNull(exception.getMessage(), "Exception should have message");
        }
        
        @Test
        @DisplayName("Load XML with wrong root element throws exception")
        void loadFromXml_WrongRootElement_ShouldThrowException() throws IOException {
            // Arrange - Create XML with wrong root element
            Path xmlPath = dataDir.resolve("wrong_root.xml");
            String wrongRootXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <wrongRoot>
                    <animal>
                        <animalId>1</animalId>
                    </animal>
                </wrongRoot>
                """;
            Files.writeString(xmlPath, wrongRootXml);
            
            // Act & Assert
            assertThrows(PersistenceException.class, () -> XMLPersistence.loadFromXML(xmlPath.toString(), Animal.class), "Should throw exception for wrong root element");
        }
        
        @Test
        @DisplayName("Save to read-only directory throws PersistenceException")
        void saveToXml_ReadOnlyDirectory_ShouldThrowException() throws IOException, ValidationException {
            // Arrange
            List<Animal> animals = createSampleAnimals();
            
            // Create a read-only directory (platform-dependent)
            Path readOnlyDir = tempDir.resolve("readonly");
            Files.createDirectory(readOnlyDir);
            File readOnlyFile = readOnlyDir.toFile();
            
            // Try to make directory read-only (may not work on all platforms)
            boolean madeReadOnly = readOnlyFile.setWritable(false);
            
            if (madeReadOnly) {
                Path xmlPath = readOnlyDir.resolve(ANIMALS_XML);
                
                // Act & Assert
                try {
                    assertThrows(PersistenceException.class, () -> XMLPersistence.saveToXML(animals, xmlPath.toString(), "animals"), "Should throw exception when writing to read-only directory");
                } finally {
                    // Cleanup: restore write permissions
                    readOnlyFile.setWritable(true);
                }
            }
            // If we couldn't make it read-only, skip this test
        }
        
        @Test
        @DisplayName("PersistenceException contains file path")
        void persistenceException_ShouldContainFilePath() {
            // Arrange
            String nonExistentPath = dataDir.resolve("nonexistent.xml").toString();
            
            // Act & Assert
            PersistenceException exception = assertThrows(PersistenceException.class, () -> XMLPersistence.loadFromXML(nonExistentPath, Animal.class));
            
            // The exception should reference the file path
            assertTrue(exception.getFilePath() != null || 
                      exception.getMessage().contains("nonexistent"),
                "Exception should reference the problematic file");
        }
    }

    // ============================================================
    // DATE FORMAT TESTS
    // ============================================================
    
    @Nested
    @DisplayName("Date Format Handling Tests")
    class DateFormatTests {
        
        @Test
        @DisplayName("Dates are saved in ISO format (YYYY-MM-DD)")
        void saveToXml_Dates_ShouldBeInIsoFormat() throws PersistenceException, IOException, ValidationException {
            // Arrange
            Animal animal = createSampleAnimal("Leo", "Tiger", Category.PREDATOR);
            setAnimalId(animal, 1);
            List<Animal> animals = List.of(animal);
            Path xmlPath = dataDir.resolve(ANIMALS_XML);
            
            // Act
            XMLPersistence.saveToXML(animals, xmlPath.toString(), "animals");
            String content = Files.readString(xmlPath);
            
            // Assert
            assertTrue(content.contains("2020-05-15"), 
                "Birth date should be in ISO format");
            assertTrue(content.contains("2023-11-20"), 
                "Acquisition date should be in ISO format");
        }
        
        @Test
        @DisplayName("Dates are loaded correctly from ISO format")
        void loadFromXml_IsoDates_ShouldBeLoadedCorrectly() throws PersistenceException, ValidationException {
            // Arrange
            Animal original = createSampleAnimal("Leo", "Tiger", Category.PREDATOR);
            setAnimalId(original, 1);
            List<Animal> animals = List.of(original);
            Path xmlPath = dataDir.resolve(ANIMALS_XML);
            
            XMLPersistence.saveToXML(animals, xmlPath.toString(), "animals");
            
            // Act
            Collection<Animal> loaded = XMLPersistence.loadFromXML(
                xmlPath.toString(), Animal.class
            );
            Animal loadedAnimal = loaded.iterator().next();
            
            // Assert
            assertEquals(SAMPLE_BIRTH_DATE, loadedAnimal.getDateOfBirth(), 
                "Birth date should be preserved");
            assertEquals(SAMPLE_ACQUISITION_DATE, loadedAnimal.getDateOfAcquisition(), 
                "Acquisition date should be preserved");
        }
    }

    // ============================================================
    // SPECIAL CHARACTERS AND ENCODING TESTS
    // ============================================================
    
    @Nested
    @DisplayName("Special Characters and Encoding Tests")
    class EncodingTests {
        
        @Test
        @DisplayName("XML declaration specifies UTF-8 encoding")
        void saveToXml_ShouldSpecifyUtf8Encoding() throws PersistenceException, IOException, ValidationException {
            // Arrange
            List<Animal> animals = createSampleAnimals();
            Path xmlPath = dataDir.resolve(ANIMALS_XML);
            
            // Act
            XMLPersistence.saveToXML(animals, xmlPath.toString(), "animals");
            String content = Files.readString(xmlPath);
            
            // Assert
            assertTrue(content.contains("UTF-8") || content.contains("utf-8"), 
                "XML should declare UTF-8 encoding");
        }
        
        @Test
        @DisplayName("Special characters in data are escaped properly")
        void saveAndLoad_SpecialCharacters_ShouldBePreserved() throws PersistenceException, ValidationException {
            // Arrange
            Cage cage = new Cage("Cage-01", "Large cage with <special> & \"characters\"", 10);
            setCageId(cage, 1);
            List<Cage> cages = List.of(cage);
            Path xmlPath = dataDir.resolve(CAGES_XML);
            
            // Act
            XMLPersistence.saveToXML(cages, xmlPath.toString(), "cages");
            Collection<Cage> loaded = XMLPersistence.loadFromXML(
                xmlPath.toString(), Cage.class
            );
            
            // Assert
            Cage loadedCage = loaded.iterator().next();
            assertEquals(cage.getDescription(), loadedCage.getDescription(), 
                "Special characters should be preserved");
        }
    }

    // ============================================================
    // Helper Methods
    // ============================================================
    
    /**
     * Creates a list of sample animals for testing.
     */
    private List<Animal> createSampleAnimals() throws ValidationException {
        Animal tiger = createSampleAnimal("Leo", "Tiger", Category.PREDATOR);
        setAnimalId(tiger, 1);
        
        Animal zebra = createSampleAnimal("Marty", "Zebra", Category.PREY);
        setAnimalId(zebra, 2);
        
        Animal rabbit = createSampleAnimal("Bugs", "Rabbit", Category.PREY);
        setAnimalId(rabbit, 3);
        
        return List.of(tiger, zebra, rabbit);
    }
    
    /**
     * Creates a sample animal for testing.
     */
    private Animal createSampleAnimal(String name, String type, Category category) throws ValidationException {
        return new Animal(
            name,
            type,
            category,
            SAMPLE_BIRTH_DATE,
            SAMPLE_ACQUISITION_DATE,
            Sex.MALE
        );
    }
    
    /**
     * Creates a list of sample keepers for testing.
     */
    private List<Keeper> createSampleKeepers() throws ValidationException {
        HeadKeeper headKeeper = new HeadKeeper(
            "John", "Smith", "123 Main St", "07123456789"
        );
        setKeeperId(headKeeper, 1);
        
        AssistantKeeper assistantKeeper = new AssistantKeeper(
            "Jane", "Doe", "456 Oak Ave", "07987654321"
        );
        setKeeperId(assistantKeeper, 2);
        
        return List.of(headKeeper, assistantKeeper);
    }
    
    /**
     * Creates a list of sample cages for testing.
     */
    private List<Cage> createSampleCages() throws ValidationException {
        Cage largeCage = new Cage("Large-01", "Large predator cage", 10);
        setCageId(largeCage, 1);
        
        Cage mediumCage = new Cage("Medium-01", "Medium prey cage", 5);
        setCageId(mediumCage, 2);
        
        Cage smallCage = new Cage("Small-01", "Small individual cage", 1);
        setCageId(smallCage, 3);
        
        return List.of(largeCage, mediumCage, smallCage);
    }
    
    /**
     * Sets the animal ID using reflection (for test purposes).
     */
    private void setAnimalId(Animal animal, int animalId) {
        try {
            java.lang.reflect.Field idField = Animal.class.getDeclaredField("animalId");
            idField.setAccessible(true);
            idField.setInt(animal, animalId);
        } catch (Exception exception) {
            fail("Unable to set animal ID: " + exception.getMessage());
        }
    }
    
    /**
     * Sets the keeper ID using reflection (for test purposes).
     */
    private void setKeeperId(Keeper keeper, int keeperId) {
        try {
            java.lang.reflect.Field idField = Keeper.class.getDeclaredField("keeperId");
            idField.setAccessible(true);
            idField.setInt(keeper, keeperId);
        } catch (Exception exception) {
            fail("Unable to set keeper ID: " + exception.getMessage());
        }
    }
    
    /**
     * Sets the cage ID using reflection (for test purposes).
     */
    private void setCageId(Cage cage, int cageId) {
        try {
            java.lang.reflect.Field idField = Cage.class.getDeclaredField("cageId");
            idField.setAccessible(true);
            idField.setInt(cage, cageId);
        } catch (Exception exception) {
            fail("Unable to set cage ID: " + exception.getMessage());
        }
    }
}
