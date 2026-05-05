import java.util.ArrayList;
import java.util.List;

public class GameState {

    // Screen / location
    public ScreenState screen          = ScreenState.TITLE_SCREEN;
    public LocationId  currentLocation = LocationId.STREET;

    // Progress flags
    public boolean hasUltimateParkingPass;
    public boolean campusUnlocked;
    public boolean hasBatteries;
    public boolean batteriesObtained;   // true once batteries ever picked up from CVS
    public boolean hasJavaSwingManual;
    public boolean bookstoreResolved;   // true once batteries handed to bookstore student
    public boolean manualGivenToLab;    // true once manual handed to Building 19
    public boolean hasProteinDrink;
    public boolean hasKFCPatty;
    public boolean hasPopeyesPatty;
    public boolean hasSuperChickenPatty;
    public boolean gymQuestStarted;
    public boolean gymItemsGiven;
    public boolean treadmillDone;
    public boolean weightsDone;
    public boolean machineDone;
    public boolean canPunchWall;
    public boolean wallBroken;
    public boolean hasRAM1;
    public boolean hasRAM2;
    public boolean hasRAM3;
    public boolean hasRAM4;
    public boolean ramInserted;
    public boolean gameWon;

    // Radio station puzzle
    public int     radioRound    = 1;

    // Inventory
    private final List<String> inventory = new ArrayList<>();

    // Quest log
    private String questText = "Enter the parking lot.";

    public void reset() {
        screen          = ScreenState.TITLE_SCREEN;
        currentLocation = LocationId.STREET;
        inventory.clear();

        hasUltimateParkingPass = false;
        campusUnlocked         = false;
        hasBatteries           = false;
        batteriesObtained      = false;
        hasJavaSwingManual     = false;
        bookstoreResolved      = false;
        manualGivenToLab       = false;
        hasProteinDrink        = false;
        hasKFCPatty            = false;
        hasPopeyesPatty        = false;
        hasSuperChickenPatty   = false;
        gymQuestStarted        = false;
        gymItemsGiven          = false;
        treadmillDone          = false;
        weightsDone            = false;
        machineDone            = false;
        canPunchWall           = false;
        wallBroken             = false;
        hasRAM1                = false;
        hasRAM2                = false;
        hasRAM3                = false;
        hasRAM4                = false;
        ramInserted            = false;
        gameWon                = false;
        radioRound             = 1;
        questText              = "Enter the parking lot.";
    }

    public void addItem(String item) {
        if (!inventory.contains(item)) inventory.add(item);
    }

    public void removeItem(String item) { inventory.remove(item); }
    public List<String> getInventory()  { return new ArrayList<>(inventory); }

    public String getQuestText()           { return questText; }
    public void   setQuestText(String txt) { questText = txt;  }

    public int getRamCount() {
        int c = 0;
        if (hasRAM1) c++;
        if (hasRAM2) c++;
        if (hasRAM3) c++;
        if (hasRAM4) c++;
        return c;
    }
}