package com.conservation.config;

/**
 * POJO class representing application configuration settings.
 * 
 * Settings are loaded from and saved to config/settings.json file.
 * Contains business rules, constraints, and system flags.
 * 
 * Used by SettingsManager for JSON serialisation/deserialisation.
 */
public class Settings {
    
    private boolean firstRun;
    private KeeperConstraints keeperConstraints;
    private AnimalRules animalRules;
    
    /**
     * Default constructor creates settings with default values.
     */
    public Settings() {
        this.firstRun = true;
        this.keeperConstraints = new KeeperConstraints();
        this.animalRules = new AnimalRules();
    }
    
    // Getters and Setters
    
    public boolean isFirstRun() {
        return firstRun;
    }
    
    public void setFirstRun(boolean firstRun) {
        this.firstRun = firstRun;
    }
    
    public KeeperConstraints getKeeperConstraints() {
        return keeperConstraints;
    }
    
    public void setKeeperConstraints(KeeperConstraints keeperConstraints) {
        this.keeperConstraints = keeperConstraints;
    }
    
    public AnimalRules getAnimalRules() {
        return animalRules;
    }
    
    public void setAnimalRules(AnimalRules animalRules) {
        this.animalRules = animalRules;
    }
    
    /**
     * Nested class for keeper-related constraints.
     * 
     * Defines minimum and maximum number of cages per keeper.
     */
    public static class KeeperConstraints {
        private int minCages;
        private int maxCages;
        
        public KeeperConstraints() {
            this.minCages = 1;
            this.maxCages = 4;
        }
        
        public int getMinCages() {
            return minCages;
        }
        
        public void setMinCages(int minCages) {
            this.minCages = minCages;
        }
        
        public int getMaxCages() {
            return maxCages;
        }
        
        public void setMaxCages(int maxCages) {
            this.maxCages = maxCages;
        }
    }
    
    /**
     * Nested class for animal allocation rules.
     * 
     * Defines whether predator and prey animals can share cages.
     */
    public static class AnimalRules {
        private boolean predatorShareable;
        private boolean preyShareable;
        
        public AnimalRules() {
            this.predatorShareable = false;  // Predators must be alone
            this.preyShareable = true;       // Prey can share cages
        }
        
        public boolean isPredatorShareable() {
            return predatorShareable;
        }
        
        public void setPredatorShareable(boolean predatorShareable) {
            this.predatorShareable = predatorShareable;
        }
        
        public boolean isPreyShareable() {
            return preyShareable;
        }
        
        public void setPreyShareable(boolean preyShareable) {
            this.preyShareable = preyShareable;
        }
    }
    
    @Override
    public String toString() {
        return String.format("Settings{firstRun=%s, keeperConstraints=%s, animalRules=%s}",
                firstRun, keeperConstraints, animalRules);
    }
}
