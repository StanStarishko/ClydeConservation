package com.conservation.interfaces;

import java.util.Collection;

/**
 * Generic interface for registry classes that manage collections of entities.
 * 
 * Provides standard CRUD operations (Create, Read, Update, Delete) for entity collections.
 * Implementations store entities in memory using HashMap for efficient O(1) lookups by ID.
 * 
 * @param <T> the type of entity stored in this registry
 * 
 * Implemented by: Animals, Keepers, Cages registry classes
 */
public interface IRegistry<T> {
    
    /**
     * Adds a new entity to the registry.
     * 
     * The entity must have a unique ID. If an entity with the same ID already exists,
     * the behaviour is implementation-dependent (may throw exception or replace existing).
     * 
     * @param entity the entity to add
     * @throws IllegalArgumentException if entity is null or invalid
     */
    void add(T entity);
    
    /**
     * Finds an entity by its unique ID.
     * 
     * @param entityId the unique identifier of the entity (as integer)
     * @return the entity with the given ID, or null if not found
     */
    T findById(int entityId);
    
    /**
     * Returns all entities currently in the registry.
     * 
     * The returned collection is a copy and modifications to it will not affect
     * the registry's internal storage.
     * 
     * @return collection of all entities (may be empty, never null)
     */
    Collection<T> getAll();
    
    /**
     * Removes an entity from the registry by its ID.
     * 
     * If no entity with the given ID exists, this method does nothing
     * (no exception is thrown).
     * 
     * @param entityId the unique identifier of the entity to remove
     * @return true if entity was removed, false if entity was not found
     */
    boolean remove(int entityId);
    
    /**
     * Returns the total number of entities in the registry.
     * 
     * @return count of entities (0 or positive integer)
     */
    int count();
    
    /**
     * Removes all entities from the registry.
     * 
     * Used primarily for testing or resetting the system state.
     */
    void clear();
}
