package com.conservation.model;

/**
 * Head Keeper - senior staff member with full management responsibilities.
 * 
 * Head keepers have authority to:
 * - Allocate animals to cages
 * - Allocate keepers to cages
 * - Make decisions about animal care and welfare
 * - Supervise assistant keepers
 * 
 * Extends the abstract Keeper class with management-specific behaviour.
 */
public class HeadKeeper extends Keeper {
    
    /**
     * Default constructor for creating empty HeadKeeper instance.
     * Required for XML deserialisation.
     */
    public HeadKeeper() {
        super();
        this.position = Position.HEAD_KEEPER;
    }
    
    /**
     * Full constructor for creating a HeadKeeper with all attributes.
     * 
     * Automatically sets position to HEAD_KEEPER.
     * 
     * @param firstName keeper's first name
     * @param surname keeper's surname
     * @param address keeper's residential address
     * @param contactNumber keeper's phone number
     */
    public HeadKeeper(String firstName, String surname,
                      String address, String contactNumber) {
        super(firstName, surname, address, contactNumber, Position.HEAD_KEEPER);
    }
    
    /**
     * Gets the responsibilities specific to a Head Keeper.
     * 
     * @return description of head keeper responsibilities
     */
    @Override
    public String getResponsibilities() {
        return "Full management responsibilities including animal allocation, " +
               "keeper supervision, and welfare decisions";
    }
    
    /**
     * Head keepers have full management permissions.
     * 
     * @return true - head keepers always have management permissions
     */
    @Override
    public boolean hasManagementPermissions() {
        return true;
    }
    
    /**
     * Approves an allocation decision.
     * 
     * Head keepers can approve animal and keeper allocations to cages.
     * This method could be extended with approval workflow logic.
     * 
     * @param allocationDescription description of what is being allocated
     * @return true if approved (currently always approves)
     */
    public boolean approveAllocation(String allocationDescription) {
        // In a real system, this might involve:
        // - Checking keeper's qualifications
        // - Reviewing animal welfare considerations
        // - Validating cage suitability
        System.out.println("Head Keeper " + getFullName() + 
                          " approved allocation: " + allocationDescription);
        return true;
    }
    
    /**
     * Supervises an assistant keeper's work.
     * 
     * Could be extended with performance tracking, training needs, etc.
     * 
     * @param assistantKeeper the assistant keeper being supervised
     * @return supervision notes or status
     */
    public String superviseAssistant(AssistantKeeper assistantKeeper) {
        return String.format("Head Keeper %s supervising Assistant Keeper %s",
                           getFullName(), assistantKeeper.getFullName());
    }
    
    @Override
    public String toString() {
        return String.format("HeadKeeper{id=%d, name='%s', cages=%d/%d}",
                keeperId, getFullName(), getAllocatedCageCount(), 4);
    }
}
