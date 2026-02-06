package com.conservation.interfaces;

/**
 * Interface for objects that can be persisted to and from XML format.
 * 
 * Classes implementing this interface must provide methods to:
 * - Convert object state to XML string representation
 * - Reconstruct object state from XML string
 * 
 * Used by: Animal, Keeper, Cage entities for XML persistence
 */
public interface IPersistable {
    
    /**
     * Converts the current object state to XML string format.
     * 
     * @return XML string representation of the object
     */
    String toXML();
    
    /**
     * Reconstructs object state from XML string data.
     * 
     * @param xmlData XML string containing object data
     * @throws IllegalArgumentException if XML data is invalid or malformed
     */
    void fromXML(String xmlData);
}
