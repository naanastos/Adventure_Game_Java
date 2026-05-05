public class Location {

    private String name;       // display name shown in the top-left panel
    private String imageKey;   // key passed to AssetManager for the background image
    private String npcKey;     // key for the NPC portrait (null if no NPC)

    // Create a location with no NPC
    public Location(String name, String imageKey) {
        this(name, imageKey, null);
    }

    // Create a location with an NPC portrait
    public Location(String name, String imageKey, String npcKey) {
        this.name     = name;
        this.imageKey = imageKey;
        this.npcKey   = npcKey;
    }

    // Getters
    public String getName()     { return name; }
    public String getImageKey() { return imageKey; }
    public String getNpcKey()   { return npcKey; }

    // Setters
    public void setName(String name)       { this.name = name; }
    public void setImageKey(String key)    { this.imageKey = key; }
    public void setNpcKey(String npcKey)   { this.npcKey = npcKey; }

    @Override
    public String toString() {
        return "Location[name=" + name + ", image=" + imageKey + ", npc=" + npcKey + "]";
    }
}
