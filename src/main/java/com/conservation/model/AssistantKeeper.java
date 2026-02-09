package com.conservation.model;

/**
 * Assistant Keeper - junior staff member with basic care responsibilities.
 * 
 * Assistant keepers are responsible for:
 * - Daily animal care (feeding, cleaning)
 * - Monitoring animal health
 * - Reporting issues to head keepers
 * - Following instructions from senior staff
 * 
 * Assistant keepers do NOT have authority to allocate animals or keepers to cages.
 * 
 * Extends the abstract Keeper class with assistant-specific behaviour.
 */
public class AssistantKeeper extends Keeper {
    
    private int supervisorId;  // ID of the Head Keeper supervising this assistant
    
    /**
     * Default constructor for creating empty AssistantKeeper instance.
     * Required for XML deserialisation.
     */
    public AssistantKeeper() {
        super();
        this.position = Position.ASSISTANT_KEEPER;
    }
    
    /**
     * Full constructor for creating an AssistantKeeper with all attributes.
     * 
     * Automatically sets position to ASSISTANT_KEEPER.
     * 
     * @param firstName keeper's first name
     * @param surname keeper's surname
     * @param address keeper's residential address
     * @param contactNumber keeper's phone number
     */
    public AssistantKeeper(String firstName, String surname,
                          String address, String contactNumber) {
        super(firstName, surname, address, contactNumber, Position.ASSISTANT_KEEPER);
    }
    
    /**
     * Gets the responsibilities specific to an Assistant Keeper.
     * 
     * @return description of assistant keeper responsibilities
     */
    @Override
    public String getResponsibilities() {
        return "Daily animal care, health monitoring, and reporting to head keepers. " +
               "No allocation or management responsibilities.";
    }

    /**
     * Gets the full professional title for an Assistant Keeper.
     *
     * @return title in format "Assistant Keeper FirstName Surname"
     */
    @Override
    public String getFullTitle() {
        return "Assistant Keeper " + getFullName();
    }

    /**
     * Assistant keepers do not have management permissions.
     * 
     * @return false - assistant keepers cannot make management decisions
     */
    @Override
    public boolean hasManagementPermissions() {
        return false;
    }

    /**
     * Gets the ID of the head keeper supervising this assistant.
     * 
     * @return supervisor's keeper ID, or 0 if not assigned
     */
    public int getSupervisorId() {
        return supervisorId;
    }
    
    /**
     * Sets the supervising head keeper for this assistant.
     * 
     * @param supervisorId the keeper ID of the supervising head keeper
     */
    public void setSupervisorId(int supervisorId) {
        this.supervisorId = supervisorId;
    }
    
    @Override
    public String toString() {
        return String.format("AssistantKeeper{id=%d, name='%s', cages=%d/%d, supervisor=%d}",
                keeperId, getFullName(), getAllocatedCageCount(), 4, supervisorId);
    }
}
