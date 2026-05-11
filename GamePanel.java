import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.*;

public class GamePanel extends JPanel implements MouseListener, MouseMotionListener {

    private static final long serialVersionUID = 1L;

    // Virtual resolution
    static final int VW = 1600, VH = 900;

    // Panel layout (virtual coords)
    static final int LOC_X=24,  LOC_Y=20,  LOC_W=940, LOC_H=110;
    static final int IMG_X=24,  IMG_Y=140, IMG_W=940, IMG_H=740;
    static final int DESC_X=985,DESC_Y=20, DESC_W=590, DESC_H=600;
    static final int CHO_X=985, CHO_Y=630, CHO_W=590, CHO_H=210;
    static final int INV_X=985, INV_Y=852, INV_W=285, INV_H=38;
    static final int QST_X=1285,QST_Y=852, QST_W=290, QST_H=38;

    // Fonts
    static final Font F_HUGE   = new Font("Monospaced", Font.BOLD, 54);
    static final Font F_TITLE  = new Font("Monospaced", Font.BOLD, 34);
    static final Font F_LOC    = new Font("Monospaced", Font.BOLD, 26);
    static final Font F_DESC   = new Font("Monospaced", Font.BOLD, 24);
    static final Font F_CHOICE = new Font("Monospaced", Font.BOLD, 19);
    static final Font F_SMALL  = new Font("Monospaced", Font.BOLD, 14);
    static final Font F_BTN    = new Font("Monospaced", Font.BOLD, 16);

    // Colours
    static final Color C_BG    = new Color(232, 232, 228);
    static final Color C_PANEL = new Color(12, 12, 12);
    static final Color C_WHITE = Color.WHITE;
    static final Color C_G1    = new Color(255,   0, 200, 130); // magenta glitch
    static final Color C_G2    = new Color(  0, 255, 128, 110); // green glitch

    // Location data
    // ArrayList of all Location objects, then loaded into a HashMap for fast lookup.
    // This is the primary data structure holding the Location class objects.
    private static final ArrayList<Location> LOCATION_LIST = new ArrayList<>();
    private static final HashMap<LocationId, Location> LOCATIONS = new HashMap<>();
    static {
        // Build the list of all game locations
        LOCATION_LIST.add(new Location("Street Facing Parking Lot",  "street_parking_lot"));
        LOCATION_LIST.add(new Location("Parking Lot",                "parking_lot"));
        LOCATION_LIST.add(new Location("Parking Lot Wizard's House", "wizard_house",             "npc_wizard"));
        LOCATION_LIST.add(new Location("Campus Entrance",            "campus_entrance_blocked"));
        LOCATION_LIST.add(new Location("Building 16",                "building16"));
        LOCATION_LIST.add(new Location("Building 19",                "building19",               "npc_computer_lab_person"));
        LOCATION_LIST.add(new Location("Building 2",                 "building2"));
        LOCATION_LIST.add(new Location("Dollar Tree",                "dollar_tree"));
        LOCATION_LIST.add(new Location("KFC",                        "kfc"));
        LOCATION_LIST.add(new Location("Popeyes",                    "popeyes"));
        LOCATION_LIST.add(new Location("CVS",                        "cvs",                      "npc_cvs_clerk"));
        LOCATION_LIST.add(new Location("Theater",                    "theater",                  "npc_theater_actor"));
        LOCATION_LIST.add(new Location("Radio Station",              "radio_station",             "npc_radio_dj"));
        LOCATION_LIST.add(new Location("Bookstore",                  "bookstore",                "npc_bookstore_student"));
        LOCATION_LIST.add(new Location("Fitness Center",             "fitness_center",            "npc_gym_man"));
        LOCATION_LIST.add(new Location("Brick Wall",                 "brick_wall"));
        LOCATION_LIST.add(new Location("Student Success Center",     "student_success_entrance"));
        LOCATION_LIST.add(new Location("Student Success Center",     "student_success_center"));

        // Load list into HashMap keyed by LocationId for O(1) lookup in enterLocation()
        LocationId[] ids = {
            LocationId.STREET, LocationId.PARKING_LOT, LocationId.WIZARD_HOUSE,
            LocationId.CAMPUS_ENTRANCE, LocationId.BUILDING_16, LocationId.BUILDING_19,
            LocationId.BUILDING_2, LocationId.DOLLAR_TREE, LocationId.KFC,
            LocationId.POPEYES, LocationId.CVS, LocationId.THEATER,
            LocationId.RADIO_STATION, LocationId.BOOKSTORE, LocationId.FITNESS_CENTER,
            LocationId.BRICK_WALL, LocationId.STUDENT_SUCCESS_ENTRANCE,
            LocationId.STUDENT_SUCCESS_CENTER
        };
        for (int i = 0; i < ids.length; i++) {
            LOCATIONS.put(ids[i], LOCATION_LIST.get(i));
        }
    }

    // Game state
    private final GameState gs = new GameState();

    // Current display
    private String       locationName   = "";
    private String       description    = "";
    private List<Choice> choices        = new ArrayList<>();
    private String       imageKey       = "street_parking_lot";
    private String       npcKey         = null;  // first NPC
    private String       npcKey2        = null;  // second NPC
    private List<MapNode> mapNodes      = new ArrayList<>();
    private ScreenState  previousScreen = ScreenState.LOCATION_VIEW;

    // Hover state
    private int     hoveredChoice = -1;
    private int     hoveredTitle  = -1;
    private int     hoveredInvItem = -1;
    private boolean hoverInv = false, hoverQst = false;
    private MapNode hoveredNode   = null;

    // Item descriptions shown in the inventory panel
    private static final java.util.Map<String, String> ITEM_DESCS = new java.util.HashMap<>();
    static {
        ITEM_DESCS.put("Ultimate Parking Pass",
            "A wizard's ultimate parking pass.\nIt says, \"You shall not pass...\nunless of course, you have this pass\"");
        ITEM_DESCS.put("Batteries",
            "It's a pack of batteries.");
        ITEM_DESCS.put("Java Swing Manual",
            "\"Swing methods are the built-in functions provided by the Swing API...\"\nYou felt a shift in reality.");
        ITEM_DESCS.put("KFC Chicken Patty",
            "It's a Chicken Patty from KFC.");
        ITEM_DESCS.put("Popeyes Chicken Patty",
            "It's a Chicken Patty from Popeyes.");
        ITEM_DESCS.put("Super Chicken Patty",
            "It's the combined mass of two chicken patties from Popeyes and KFC.");
        ITEM_DESCS.put("Ultimate Protein Drink",
            "It's a perfect protein drink.");
        ITEM_DESCS.put("RAM Stick 1 (64 GB)",
            "The first legendary RAM STCC (stick).");
        ITEM_DESCS.put("RAM Stick 2 (64 GB)",
            "The second legendary RAM STCC (stick).");
        ITEM_DESCS.put("RAM Stick 3 (64 GB)",
            "The third legendary RAM STCC (stick).");
        ITEM_DESCS.put("RAM Stick 4 (64 GB)",
            "The fourth legendary RAM STCC (stick).");
    }

    // Scaling state
    private double scale = 1.0;
    private int    offX = 0, offY = 0;

    // Inner class: map node
    private static class MapNode {
        final int cx, cy;
        final LocationId dest;
        final String label, type;

        MapNode(int cx, int cy, LocationId dest, String label, String type) {
            this.cx = cx; this.cy = cy;
            this.dest = dest; this.label = label; this.type = type;
        }

        Rectangle bounds() { return new Rectangle(cx - 34, cy - 34, 68, 68); }
    }

    public GamePanel() {
        setBackground(C_BG);
        addMouseListener(this);
        addMouseMotionListener(this);
        new Timer(32, e -> repaint()).start(); // ~30 fps
    }

    // PAINT
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();

        double sx = (double) getWidth()  / VW;
        double sy = (double) getHeight() / VH;
        scale = Math.min(sx, sy);
        offX  = (int) ((getWidth()  - VW * scale) / 2);
        offY  = (int) ((getHeight() - VH * scale) / 2);

        g.translate(offX, offY);
        g.scale(scale, scale);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,     RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(C_BG); g.fillRect(0, 0, VW, VH);

        switch (gs.screen) {
            case TITLE_SCREEN   -> drawTitle(g);
            case INSTRUCTIONS   -> drawInstructions(g);
            case INVENTORY_VIEW -> drawInventory(g);
            case QUEST_LOG_VIEW -> drawQuestLog(g);
            case WIN_SCREEN     -> drawWin(g);
            default             -> drawGameView(g); // MAP_VIEW + LOCATION_VIEW
        }
        g.dispose();
    }

    // SCREENS
    private void drawTitle(Graphics2D g) {
        g.setColor(C_PANEL); g.fillRect(0, 0, VW, VH);
        BufferedImage bg = AssetManager.get("title");
        if (bg != null) g.drawImage(bg, 0, 0, VW, VH, null);
        g.setFont(F_HUGE);
        String h1 = "CAMPUS QUEST";
        drawGlitch(g, h1, centreX(g, h1), 210);
        g.setFont(F_TITLE);
        String h2 = "The Search for More RAM";
        drawGlitch(g, h2, centreX(g, h2), 272);
        drawMenuButtons(g, new String[]{ "Start Game", "Instructions", "Quit" }, 420);
    }

    // Shared button renderer used by drawTitle and drawWin.
    private void drawMenuButtons(Graphics2D g, String[] opts, int startY) {
        g.setFont(F_CHOICE);
        FontMetrics fm = g.getFontMetrics();
        for (int i = 0; i < opts.length; i++) {
            int oy = startY + i * 72, ow = fm.stringWidth(opts[i]) + 44, ox = (VW - ow) / 2;
            if (hoveredTitle == i) { g.setColor(C_WHITE); g.fillRoundRect(ox, oy-30, ow, 50, 8, 8); g.setColor(C_PANEL); }
            else g.setColor(C_WHITE);
            g.drawString(opts[i], ox + 22, oy);
        }
    }

    private void drawInstructions(Graphics2D g) {
        g.setColor(C_PANEL); g.fillRect(0, 0, VW, VH);
        g.setFont(F_TITLE); g.setColor(C_WHITE);
        String hdr = "HOW TO PLAY";
        g.drawString(hdr, centreX(g, hdr), 130);
        String[] lines = {
            "This is an interactive adventure game using java,",
            "taking place at the STCC campus (and adjacent areas).",
            "Built on the text based adventure game assignment and",
            "based on point and click games from the 90s and 80s.",
            "Goal of this game is to find the Student Success center",
            "by talking to everyone and collecting 4 STCCs (sticks) of RAM.",
            "Tips: Talk to everyone. Collect all the items.",
        };
        g.setFont(F_DESC);
        int ly = 210;
        for (String line : lines) { g.drawString(line, 220, ly); ly += 42; }
        drawTitleButton(g, "Back to Title", 260, 740, 200, 44, hoveredTitle == 0);
    }

    private void drawGameView(Graphics2D g) {
        drawPanel(g, LOC_X, LOC_Y, LOC_W, LOC_H);
        drawPanel(g, IMG_X, IMG_Y, IMG_W, IMG_H);
        drawPanel(g, DESC_X, DESC_Y, DESC_W, DESC_H);
        drawPanel(g, CHO_X,  CHO_Y, CHO_W, CHO_H);

        BufferedImage bg = AssetManager.get(imageKey);
        if (bg != null) g.drawImage(bg, IMG_X, IMG_Y, IMG_W, IMG_H, null);

        if (gs.screen == ScreenState.MAP_VIEW)
            for (MapNode n : mapNodes) drawMapNode(g, n);

        drawNpc(g, npcKey,  npcKey2 != null ? IMG_X + 44 : IMG_X + (IMG_W - 280) / 2);
        drawNpc(g, npcKey2, IMG_X + IMG_W - 280 - 44);

        g.setFont(F_LOC); g.setColor(C_WHITE);
        FontMetrics fm = g.getFontMetrics();
        drawGlitch(g, locationName, LOC_X + 22, LOC_Y + (LOC_H + fm.getAscent() - fm.getDescent()) / 2);

        g.setFont(F_DESC); g.setColor(C_WHITE);
        drawWrapped(g, description, DESC_X + 30, DESC_Y + 60, DESC_W - 60, 52);

        drawChoices(g);
        drawTitleButton(g, "[ INVENTORY ]", INV_X, INV_Y, INV_W, INV_H, hoverInv);
        drawTitleButton(g, "[ QUEST LOG ]", QST_X, QST_Y, QST_W, QST_H, hoverQst);
    }

    private void drawInventory(Graphics2D g) {
        drawOverlayPanel(g);

        // Column boundaries inside the overlay panel (x=180, w=1240)
        int col1X = 260,  col1W = 393;  // left  - item list
        int col2X = 653,  col2W = 413;  // mid   - item image
        int col3X = 1066, col3W = 314;  // right - description

        g.setFont(F_HUGE); g.setColor(C_WHITE);
        g.drawString("INVENTORY", col1X, 200);

        List<String> inv = gs.getInventory();
        if (inv.isEmpty()) {
            g.setFont(F_DESC); g.setColor(C_WHITE);
            g.drawString("Your inventory is empty.", col1X, 280);
        } else {
            g.setFont(F_DESC);
            FontMetrics fm = g.getFontMetrics();
            for (int i = 0; i < inv.size(); i++) {
                Rectangle r = inventoryItemRect(i);
                boolean hov = (hoveredInvItem == i);
                if (hov) {
                    g.setColor(C_WHITE);
                    g.fillRoundRect(r.x, r.y, r.width, r.height, 6, 6);
                    g.setColor(C_PANEL);
                } else {
                    g.setColor(C_WHITE);
                }
                int ty = r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2;
                g.drawString("-  " + inv.get(i), col1X + 8, ty);
            }

            // Middle: item image preview
            if (hoveredInvItem >= 0 && hoveredInvItem < inv.size()) {
                String item = inv.get(hoveredInvItem);
                String imgKey = "item_" + item.toLowerCase()
                    .replaceAll("[^a-z0-9]+", "_")
                    .replaceAll("^_|_$", "");
                BufferedImage img = AssetManager.get(imgKey);
                if (img != null) {
                    int maxW = col2W - 20, maxH = 400;
                    double ratio = Math.min((double) maxW / img.getWidth(), (double) maxH / img.getHeight());
                    int dw = (int)(img.getWidth() * ratio), dh = (int)(img.getHeight() * ratio);
                    int ix = col2X + (col2W - dw) / 2, iy = 200 + (maxH - dh) / 2;
                    g.drawImage(img, ix, iy, dw, dh, null);
                }

                // Right: description text
                String desc = ITEM_DESCS.get(item);
                if (desc != null) {
                    g.setFont(F_DESC); g.setColor(C_WHITE);
                    drawWrapped(g, desc, col3X, 240, col3W, 36);
                }
            }
        }

        drawTitleButton(g, "[ BACK ]", 260, 740, 200, 44, hoveredTitle == 0);
    }

    /** Returns the hit rectangle for inventory item row i. */
    private Rectangle inventoryItemRect(int i) {
        return new Rectangle(258, 258 + i * 38, 390, 36);
    }

    private void drawQuestLog(Graphics2D g) {
        drawOverlayPanel(g);
        g.setFont(F_HUGE); g.setColor(C_WHITE);
        g.drawString("QUEST LOG", 260, 200);
        g.setFont(F_DESC); g.setColor(C_WHITE);
        drawWrapped(g, gs.getQuestText(), 260, 280, 1080, 32);
        drawTitleButton(g, "[ BACK ]", 260, 740, 200, 44, hoveredTitle == 0);
    }

    private void drawWin(Graphics2D g) {
        g.setColor(C_PANEL); g.fillRect(0, 0, VW, VH);
        g.setFont(F_HUGE);
        String h = "SUCCESS ACHIEVED";
        drawGlitch(g, h, centreX(g, h), 220);
        String[] lines = {
            "You found the Student Success Center.",
            "You inserted 256 GB of RAM.",
            "You achieved success.", "",
            "This concludes your academic journey."
        };
        g.setFont(F_DESC); g.setColor(C_WHITE);
        int ly = 320;
        for (String l : lines) { g.drawString(l, centreX(g, l), ly); ly += 44; }
        drawMenuButtons(g, new String[]{ "Play Again", "Quit" }, 630);
    }

    // DRAWING HELPERS
    private void drawNpc(Graphics2D g, String key, int nx) {
        if (key == null) return;
        java.util.List<java.awt.image.BufferedImage> frames = AssetManager.getFrames(key);
        if (frames.isEmpty()) return;
        int idx = (int)((System.currentTimeMillis() / 275) % frames.size());
        g.drawImage(frames.get(idx), nx, IMG_Y + (IMG_H - 480) / 2, 280, 480, null);
    }

    private void drawChoices(Graphics2D g) {
        if (choices.isEmpty()) return;
        g.setFont(F_CHOICE);
        FontMetrics fm = g.getFontMetrics();
        for (int i = 0; i < choices.size() && i < 4; i++) {
            Rectangle r = choiceRect(i);
            boolean hov = (hoveredChoice == i);
            if (hov) { g.setColor(C_WHITE); g.fillRoundRect(r.x, r.y, r.width, r.height, 6, 6); g.setColor(C_PANEL); }
            else g.setColor(C_WHITE);
            int ty = r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2;
            g.drawString((i + 1) + ".  " + choices.get(i).text, r.x + 12, ty);
        }
    }

    private Rectangle choiceRect(int i) {
        int pad = 6, slotH = (CHO_H - pad * 2) / 4;
        return new Rectangle(CHO_X + pad, CHO_Y + pad + i * slotH, CHO_W - pad * 2, slotH - 3);
    }

    private void drawMapNode(Graphics2D g, MapNode n) {
        String key = switch (n.type) {
            case "pin"        -> "icons/location";
            case "blocked"    -> "icons/blocked";
            case "arrow_left" -> "icons/arrow_left";
            default           -> "icons/arrow_right";
        };
        BufferedImage icon = AssetManager.get(key);
        if (icon == null) return;
        int iw = icon.getWidth(), ih = icon.getHeight();
        int ix = n.cx - iw / 2, iy = n.cy - ih / 2;
        if (n == hoveredNode) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f));
            g.setColor(Color.YELLOW); g.fillRect(ix, iy, iw, ih);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        g.drawImage(icon, ix, iy, null);
        g.setFont(F_SMALL);
        FontMetrics fm = g.getFontMetrics();
        int lx = n.cx - fm.stringWidth(n.label) / 2, ly = n.cy + ih / 2 + 18;
        g.setColor(Color.BLACK); g.drawString(n.label, lx + 1, ly + 1);
        g.setColor(Color.WHITE); g.drawString(n.label, lx, ly);
    }

    private void drawPanel(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(C_PANEL); g.fillRoundRect(x, y, w, h, 16, 16);
        g.setColor(new Color(55, 55, 55)); g.drawRoundRect(x, y, w, h, 16, 16);
    }

    private void drawOverlayPanel(Graphics2D g) {
        g.setColor(C_PANEL); g.fillRoundRect(180, 80, 1240, 720, 20, 20);
        g.setColor(new Color(55, 55, 55)); g.drawRoundRect(180, 80, 1240, 720, 20, 20);
    }

    private void drawTitleButton(Graphics2D g, String text, int x, int y, int w, int h, boolean hov) {
        if (hov) { g.setColor(C_WHITE); g.fillRoundRect(x, y, w, h, 8, 8); g.setColor(C_PANEL); }
        else { g.setColor(C_PANEL); g.fillRoundRect(x, y, w, h, 8, 8); g.setColor(new Color(80, 80, 80)); g.drawRoundRect(x, y, w, h, 8, 8); g.setColor(C_WHITE); }
        g.setFont(F_BTN);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, x + (w - fm.stringWidth(text)) / 2, y + (h + fm.getAscent() - fm.getDescent()) / 2);
    }

    private void drawWrapped(Graphics2D g, String text, int x, int y, int maxW, int lineH) {
        if (text == null || text.isEmpty()) return;
        FontMetrics fm = g.getFontMetrics();
        for (String para : text.split("\n", -1)) {
            if (para.isEmpty()) { y += lineH; continue; }
            StringBuilder line = new StringBuilder();
            for (String word : para.split(" ")) {
                String test = line.length() > 0 ? line + " " + word : word;
                if (fm.stringWidth(test) > maxW && line.length() > 0) {
                    g.drawString(line.toString(), x, y); y += lineH;
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(test);
                }
            }
            if (line.length() > 0) { g.drawString(line.toString(), x, y); y += lineH; }
        }
    }

    private void drawGlitch(Graphics2D g, String text, int x, int y) {
        g.setColor(C_G1); g.drawString(text, x + 3, y - 1);
        g.setColor(C_G2); g.drawString(text, x - 2, y + 2);
        g.setColor(C_WHITE); g.drawString(text, x, y);
    }

    private int centreX(Graphics2D g, String text) {
        return (VW - g.getFontMetrics().stringWidth(text)) / 2;
    }

    // CHOICE / NAVIGATION HELPERS
    private void setChoices(Choice... cs) {
        choices.clear();
        for (Choice c : cs) choices.add(c);
        hoveredChoice = -1;
    }

    private Choice back(LocationId dest) {
        return new Choice("Go Back", () -> enterLocation(dest));
    }

    private void setDesc(String text) { description = text; repaint(); }

    /**
     * Loads name, imageKey, and npcKey from the LOCATIONS map and sets screen to LOCATION_VIEW.
     * Call this at the start of every LOCATION_VIEW case in enterLocation() to avoid repeating
     * the same three assignments in every single case.
     */
    private void loadLocation(LocationId loc) {
        Location l = LOCATIONS.get(loc);
        if (l == null) return;
        gs.screen    = ScreenState.LOCATION_VIEW;
        locationName = l.getName();
        imageKey     = l.getImageKey();
        npcKey       = l.getNpcKey();
        npcKey2      = null;
    }

    // ENTER LOCATION  -  central dispatch
    private void enterLocation(LocationId loc) {
        gs.currentLocation = loc;
        mapNodes.clear();
        npcKey = null; npcKey2 = null;

        switch (loc) {

        case STREET -> {
            loadLocation(loc);
            description = "You are standing near the parking lot. Somewhere beyond this pavement, higher education is probably happening.";
            setChoices(
                new Choice("Enter Parking Lot",    () -> enterLocation(LocationId.PARKING_LOT)),
                new Choice("Look Around",          () -> setDesc("You look around. Pavement. More pavement.")),
                new Choice("Stand Still",          () -> setDesc("You stand still with great determination.")),
                new Choice("Question Your Choices",() -> setDesc("You question your choices. You're still in the parking lot though."))
            );
        }

        case PARKING_LOT -> {
            loadLocation(loc);
            description = "You are in the parking lot. The painted lines suggest order.";
            if (!gs.campusUnlocked) gs.setQuestText("Find a way onto campus.");
            buildParkingLotChoices();
        }

        case WIZARD_HOUSE -> {
            loadLocation(loc);
            buildWizardChoices();
        }

        case CAMPUS_ENTRANCE -> {
            loadLocation(loc);
            imageKey = gs.campusUnlocked ? "campus_entrance_open" : "campus_entrance_blocked";
            if (gs.campusUnlocked) {
                description = "You are at the campus entrance. It is now visible.";
                setChoices(
                    new Choice("Enter Campus",          () -> enterLocation(LocationId.CAMPUS_MAP)),
                    new Choice("Go Back to Parking Lot",() -> enterLocation(LocationId.PARKING_LOT)),
                    new Choice("Look at Entrance",      () -> setDesc("The entrance stands open. It waited patiently.")),
                    new Choice("Question Reality",      () -> setDesc("You question reality. Reality is unbothered."))
                );
            } else {
                description = "The campus entrance is not visible here.";
                setChoices(
                    new Choice("Look Around", () -> setDesc("There is a faint shimmer where a campus might be. Or might not be.")),
                    back(LocationId.PARKING_LOT)
                );
            }
        }

        case CAMPUS_MAP -> {
            gs.screen = ScreenState.MAP_VIEW;
            locationName = "Campus"; imageKey = "campus_map";
            description = "The campus map. Click a location to travel there.";
            choices.clear(); buildMapNodesCampus();
        }

        case LEFT_OF_CAMPUS -> {
            gs.screen = ScreenState.MAP_VIEW;
            locationName = "Left of Campus"; imageKey = "campus_left_map";
            description = "Left of campus. Several establishments of varying importance.";
            choices.clear(); buildMapNodesLeft();
        }

        case RIGHT_OF_CAMPUS -> {
            gs.screen = ScreenState.MAP_VIEW;
            locationName = "Right of Campus"; imageKey = "campus_right_map";
            description = "Right of campus. The Fitness Center is here.";
            choices.clear(); buildMapNodesRight();
        }

        case BUILDING_16 -> {
            loadLocation(loc);
            description = "You are standing outside Building 16. Somehow you get the strange feeling that some numbers were skipped.";
            gs.setQuestText("Find the Student Success Center near Building 2.");
            setChoices(
                new Choice("Ask About Success",   () -> setDesc("\"Looking for success? Most students are. Try near Building 2.\"")),
                new Choice("Ask About Buildings", () -> setDesc("\"Building numbers are a test. Nobody knows what kind.\"")),
                new Choice("Ask About RAM",       () -> setDesc("\"Success requires memory. That might be advice, or it might be literal. Noone knows\"")),
                back(LocationId.CAMPUS_MAP)
            );
        }

        case BUILDING_19 -> {
            loadLocation(loc);
            buildBuilding19Choices();
        }

        case BUILDING_2 -> {
            loadLocation(loc);
            description = "You are standing outside Building 2. There is a suspicious amount of wall nearby.";
            setChoices(
                new Choice("Follow the Wall",      () -> enterLocation(LocationId.BRICK_WALL)),
                new Choice("Look Around",          () -> setDesc("You look around. Brick buildings and brick walls as far as the eyes can see.")),
                new Choice("Ask Where Success Is", () -> setDesc("The answer appears to be behind the brick wall")),
                back(LocationId.CAMPUS_MAP)
            );
        }

        case BRICK_WALL -> {
            loadLocation(loc);
            locationName = gs.wallBroken ? "Student Success Center" : "Brick Wall";
            imageKey     = gs.wallBroken ? "student_success_entrance" : "brick_wall";
            buildBrickWallChoices();
        }

        case DOLLAR_TREE -> {
            loadLocation(loc);
            description = "You are inside Dollar Tree. The prices are low but quest importance is high.";
            setChoices(
                new Choice("Buy Ultimate Protein Drink", () -> {
                    if (gs.hasProteinDrink) { setDesc("You already have the Ultimate Protein Drink."); return; }
                    gs.hasProteinDrink = true;
                    gs.addItem("Ultimate Protein Drink");
                    setDesc("You obtain the Ultimate Protein Drink.");
                    gs.setQuestText(gs.hasSuperChickenPatty
                        ? "Return to the Fitness Center with your supplies."
                        : "You found the Ultimate Protein Drink. Now find the chicken patties.");
                }),
                new Choice("Search Shelves",     () -> setDesc("You search the shelves and the shelves search back emotionally.")),
                new Choice("Ask About Strength", () -> setDesc("The answer appears to be protein")),
                back(LocationId.LEFT_OF_CAMPUS)
            );
        }

        case KFC -> {
            loadLocation(loc);
            description = "You are inside KFC. You can smell a blend of 11 herbs and spices.";
            setChoices(
                new Choice("Order Chicken Patty", () -> {
                    if (gs.hasKFCPatty || gs.hasSuperChickenPatty) { setDesc("You already got what you needed here."); return; }
                    gs.hasKFCPatty = true;
                    gs.addItem("KFC Chicken Patty");
                    tryMakeSuperChicken();
                }),
                new Choice("Ask About Protein", () -> setDesc("The server gestures at everything around them.")),
                new Choice("Look at Menu",       () -> setDesc("You are on a mission, and can't get distracted the sides and combo meals")),
                back(LocationId.LEFT_OF_CAMPUS)
            );
        }

        case POPEYES -> {
            loadLocation(loc);
            description = "You are inside Popeyes. They have a surprising lack of spinach.";
            setChoices(
                new Choice("Order Chicken Patty", () -> {
                    if (gs.hasPopeyesPatty || gs.hasSuperChickenPatty) { setDesc("You already got what you needed here."); return; }
                    gs.hasPopeyesPatty = true;
                    gs.addItem("Popeyes Chicken Patty");
                    tryMakeSuperChicken();
                }),
                new Choice("Ask About Flavor", () -> setDesc("\"It's the brine,\" says an employee who was not asked.")),
                new Choice("Look at Menu",      () -> setDesc("You are on a mission, and can't get distracted the sides and combo meals.")),
                back(LocationId.LEFT_OF_CAMPUS)
            );
        }

        case CVS -> {
            loadLocation(loc);
            description = "You are inside CVS. There is no one at the register and the receipt printer watches from the shadows.";
            buildCVSChoices();
        }

        case THEATER -> {
            loadLocation(loc);
            buildTheaterChoices();
        }

        case RADIO_STATION -> {
            loadLocation(loc);
            buildRadioChoices();
        }

        case BOOKSTORE -> {
            loadLocation(loc);
            buildBookstoreChoices();
        }

        case FITNESS_CENTER -> {
            loadLocation(loc);
            npcKey2 = "npc_gym_woman"; // second NPC - handled here since Location only stores one
            buildGymChoices();
        }

        case STUDENT_SUCCESS_ENTRANCE -> {
            loadLocation(loc);
            buildSSCEntranceChoices();
        }

        case STUDENT_SUCCESS_CENTER -> {
            loadLocation(loc);
            description = "You are inside the Student Success Center. After everything, it looks surprisingly normal.";
            setChoices(
                new Choice("Ask for Success",    () -> win()),
                new Choice("Say You Found It",   () -> win()),
                new Choice("Look Around",        () -> win()),
                new Choice("Stand Triumphantly", () -> win())
            );
        }

        } // end switch
        MusicPlayer.play(loc);
        repaint();
    }

    // LOCATION BUILDERS  -  handle state-dependent choices per location
    private void buildParkingLotChoices() {
        if (gs.campusUnlocked) {
            setChoices(
                new Choice("Go to Campus Entrance", () -> enterLocation(LocationId.CAMPUS_ENTRANCE)),
                new Choice("Visit Wizard's House",  () -> enterLocation(LocationId.WIZARD_HOUSE)),
                new Choice("Look at Entrance",      () -> { imageKey = "campus_entrance_open";    setDesc("The campus entrance is now visible and open. Progress."); }),
                back(LocationId.STREET)
            );
        } else if (!gs.hasUltimateParkingPass) {
            setChoices(
                new Choice("Search Parking Lot", () -> {
                    gs.hasUltimateParkingPass = true;
                    gs.addItem("Ultimate Parking Pass");
                    gs.setQuestText("Bring the Ultimate Parking Pass to the Parking Lot Wizard.");
                    setDesc("You find the Ultimate Parking Pass lying on the ground. It hums with unreasonable authority.");
                    buildParkingLotChoices();
                }),
                new Choice("Visit Wizard's House",     () -> enterLocation(LocationId.WIZARD_HOUSE)),
                new Choice("Look at Blocked Entrance", () -> { imageKey = "campus_entrance_blocked"; setDesc("The campus entrance is completely blocked. Whatever is on the other side would rather you weren't."); }),
                back(LocationId.STREET)
            );
        } else {
            setChoices(
                new Choice("Visit Wizard's House",     () -> enterLocation(LocationId.WIZARD_HOUSE)),
                new Choice("Look at Parking Pass",     () -> setDesc("The Ultimate Parking Pass hums with unreasonable authority.")),
                new Choice("Look at Blocked Entrance", () -> { imageKey = "campus_entrance_blocked"; setDesc("Still blocked. The Parking Lot Wizard probably has something to say about this."); }),
                back(LocationId.STREET)
            );
        }
    }

    private void buildWizardChoices() {
        if (!gs.hasUltimateParkingPass && !gs.campusUnlocked) {
            description = "The Parking Lot Wizard blocks your path. His power appears to be mostly administrative.";
            gs.setQuestText("Find the Ultimate Parking Pass.");
            setChoices(
                new Choice("Talk to Wizard",    () -> setDesc("\"None may enter the campus without the Ultimate Parking Pass.\"")),
                new Choice("Ask About Campus",  () -> setDesc("\"The campus is hidden from those unworthy of parking.\"")),
                new Choice("Ask About Parking", () -> setDesc("\"Without parking guidelines the world would fall into chaos.\"")),
                back(LocationId.PARKING_LOT)
            );
        } else if (!gs.campusUnlocked) {
            description = "The Parking Lot Wizard eyes your parking pass.";
            setChoices(
                new Choice("Give Parking Pass", () -> {
                    gs.campusUnlocked = true;
                    gs.removeItem("Ultimate Parking Pass");
                    gs.hasUltimateParkingPass = false;
                    gs.setQuestText("The campus has appeared. Enter it before it disappears again.");
                    buildWizardChoices();
                    description = "The Parking Lot Wizard raises the pass into the air. Mystical runes and parking symbols glow and the campus fades into existence.";
                }),
                new Choice("Ask About Campus", () -> setDesc("\"The campus is hidden from those without proper parking authority.\"")),
                new Choice("Look at Wizard",   () -> setDesc("Yup that's a wizard fo sho.")),
                back(LocationId.PARKING_LOT)
            );
        } else {
            description = "The Parking Lot Wizard nods. The campus is visible now. His work here is done.";
            setChoices(
                new Choice("Ask About Campus",  () -> setDesc("\"You've already unlocked it. Shoooo.\"")),
                new Choice("Ask About Parking", () -> setDesc("\"Parking is a trial that all must overcome.\"")),
                back(LocationId.PARKING_LOT)
            );
        }
    }

    private void buildBrickWallChoices() {
        if (gs.wallBroken) {
            description = "The wall is rubble. The Student Success Center entrance waits.";
            setChoices(
                new Choice("Enter Entrance", () -> enterLocation(LocationId.STUDENT_SUCCESS_ENTRANCE)),
                new Choice("Look at Rubble", () -> setDesc("Punching bricks is very satisfying.")),
                back(LocationId.CAMPUS_MAP)
            );
        } else if (!gs.canPunchWall) {
            description = "A brick wall blocks your path. The Student Success Center is allegedly behind it which feels symbolic.";
            if (!gs.gymQuestStarted) gs.setQuestText("Become strong enough to punch the brick wall.");
            setChoices(
                new Choice("Punch It",     () -> setDesc("Oww. Your hand hurts.")),
                new Choice("Walk Into It", () -> setDesc("Yes, you saw this in a movie once. Or maybe you read it in a book? Either way you run into the wall to get to platfo\u2014Ouch you hit the wall.")),
                new Choice("Look At It",   () -> setDesc("Yup. That's a brick wall.")),
                back(LocationId.CAMPUS_MAP)
            );
        } else {
            description = "A brick wall blocks your path. Your fists disagree with its continued existence.";
            setChoices(
                new Choice("Punch It!", () -> {
                    gs.wallBroken = true;
                    gs.setQuestText("The wall is gone. Find all four 64 GB RAM sticks.");
                    enterLocation(LocationId.BRICK_WALL);
                }),
                new Choice("Look At It", () -> setDesc("One punch. That's all it would take.")),
                back(LocationId.CAMPUS_MAP)
            );
        }
    }

    private void buildBuilding19Choices() {
        Choice lookAtComputer = new Choice("Look at Computer", () -> setDesc("The computer looks undescriptive, due to not being worthwhile to write about for a complex assignment."));
        if (!gs.hasJavaSwingManual && !gs.hasRAM2) {
            description = "You are standing outside Building 19. Somewhere nearby, a computer is failing with confidence.";
            setChoices(
                new Choice("Talk to Lab Person", () -> setDesc("\"The lab computer refuses to display the interface. We even tried to ask it nicely.\"")),
                lookAtComputer,
                new Choice("Ask About RAM",      () -> setDesc("\"We have RAM, but we cannot give it away until someone solves the interface problem.\"")),
                back(LocationId.CAMPUS_MAP)
            );
        } else if (!gs.hasRAM2) {
            description = "You are standing outside Building 19. The Computer Lab Person eyes the Java Swing Manual.";
            setChoices(
                new Choice("Give Java Swing Manual", () -> {
                    gs.removeItem("Java Swing Manual");
                    gs.hasJavaSwingManual = false; gs.hasRAM2 = true; gs.manualGivenToLab = true;
                    gs.addItem("RAM Stick 2 (64 GB)");
                    updateRamQuestText();
                    buildBuilding19Choices();
                    description = "The Lab Monitor flips through the Java Swing Manual. \"Of course. Custom JPanel. MouseListener. Repaint. It was obvious.\"\nRAM Stick 2 obtained.";
                }),
                lookAtComputer,
                new Choice("Ask About RAM",    () -> setDesc("\"Solve the interface problem first.\"")),
                back(LocationId.CAMPUS_MAP)
            );
        } else {
            description = "Building 19 hums with a recently solved interface problem.";
            setChoices(
                new Choice("Look at Computer", () -> setDesc("The computer now displays a JPanel. Beautiful, just don't open it in Eclipse.")),
                back(LocationId.CAMPUS_MAP)
            );
        }
    }

    private void updateRamQuestText() {
        int count = gs.getRamCount();
        gs.setQuestText(count >= 4
            ? "You have all four RAM sticks. Insert them at the Student Success Center."
            : "RAM sticks found: " + count + " of 4. Keep searching.");
    }

    private void buildCVSChoices() {
        setChoices(
            new Choice("Ask About Batteries", () -> {
                if (gs.batteriesObtained) { setDesc("You already have the Batteries."); return; }
                gs.batteriesObtained = true; gs.hasBatteries = true;
                gs.addItem("Batteries");
                gs.setQuestText("Bring the Batteries to someone who thinks books need them.");
                setDesc("The CVS Clerk hands you Batteries. \"Just take them. I won't even charge you. The C in CVS is for Compassion.\"");
            }),
            new Choice("Ask About RAM", () -> {
                if (gs.hasRAM1) { setDesc("You already have RAM Stick 1."); return; }
                gs.hasRAM1 = true; gs.addItem("RAM Stick 1 (64 GB)");
                updateRamQuestText();
                setDesc("The CVS Clerk gives you RAM Stick 1. You decide not to ask why CVS sells memory.");
            }),
            new Choice("Look at Receipt Printer", () -> setDesc("The receipt printer spits out a receipt longer than your academic transcript.")),
            back(LocationId.LEFT_OF_CAMPUS)
        );
    }

    private void buildBookstoreChoices() {
        if (gs.bookstoreResolved) {
            description = "The student reads their battery-powered book contentedly.";
            setChoices(new Choice("Ask About Books", () -> setDesc("\"This one runs on AA.\"")), back(LocationId.CAMPUS_MAP));
        } else if (!gs.hasBatteries) {
            description = "You are inside the Bookstore. The textbooks radiate financial danger.";
            setChoices(
                new Choice("Ask About the Book",         () -> setDesc("\"I can't read this book. The batteries are dead.\"")),
                new Choice("Suggest Opening It",         () -> setDesc("\"Don't be ridiculous. That's not how books work.\"")),
                new Choice("Ask Why It Needs Batteries", () -> setDesc("\"Because it's educational.\"")),
                back(LocationId.CAMPUS_MAP)
            );
        } else {
            description = "The student at the Bookstore clutches a dead book. They notice your batteries.";
            setChoices(
                new Choice("Give Batteries", () -> {
                    gs.removeItem("Batteries"); gs.hasBatteries = false;
                    gs.hasJavaSwingManual = true; gs.bookstoreResolved = true;
                    gs.addItem("Java Swing Manual");
                    gs.setQuestText("Bring the Java Swing Manual to Building 19.");
                    buildBookstoreChoices();
                    description = "The student pops the batteries into the book. The book turns on. You accept it... understand it.... books just read better with batteries.\nThey hand you a Java Swing Manual.";
                }),
                new Choice("Ask About the Book", () -> setDesc("\"I can't read this book. The batteries are dead.\"")),
                new Choice("Suggest Opening It", () -> setDesc("\"Don't be ridiculous.\"")),
                back(LocationId.CAMPUS_MAP)
            );
        }
    }

    private void buildTheaterChoices() {
        if (gs.hasRAM3) {
            description = "You are inside the Theater. Romeo has been located.";
            setChoices(
                new Choice("Watch the Play", () -> setDesc("The play continues. Romeo is very found.")),
                new Choice("Look Around",    () -> setDesc("Stage. Balcony. Theaterkid Drama. Stagelights.")),
                back(LocationId.CAMPUS_MAP)
            );
            return;
        }
        description = "You are inside the Theater. Someone is on a balcony asking about some Romeo guy.\n\n\"Romeo Romeo, where art thou Romeo?\"";
        setChoices(
            new Choice("Try the parking lot.",  () -> setDesc("\"This play is a tragedy, not a parking dispute.\"")),
            new Choice("Maybe he transferred.", () -> setDesc("\"No you're thinking of Macbeth. I hear they went to Elms College.\"")),
            new Choice("He's over there.", () -> {
                gs.hasRAM3 = true; gs.addItem("RAM Stick 3 (64 GB)");
                updateRamQuestText(); buildTheaterChoices();
                description = "\"Oh wow yeah. Thanks.\"\nRAM Stick 3 obtained.";
            }),
            back(LocationId.CAMPUS_MAP)
        );
    }

    private void buildRadioChoices() {
        if (gs.hasRAM4) {
            description = "You are inside the Radio Station. The music continues. You stay here for a while and take a break until you're ready to go.";
            setChoices(new Choice("Listen to Music", () -> setDesc("The music is good. It was always good.")), back(LocationId.CAMPUS_MAP));
            return;
        }
        switch (gs.radioRound) {
            case 1 -> {
                description = "You are inside the Radio Station. The campus plays 24/7 stream of \"lofi hip hop beats - music to study/relax to\".\n\n\"People think radio is about microphones. Or wires. Or little blinking lights. But it's not. It's about the music, maaaaaaan. It's all about the music.\"";
                setChoices(
                    new Choice("Parking.", () -> setDesc("\"Huh? You're listening, but you aren't hearing.\"")),
                    new Choice("Weather.", () -> setDesc("\"No. You're listening, but you aren't hearing.\"")),
                    new Choice("Music.",   () -> { gs.radioRound = 2; description = "\"Exactly. Music.\""; setChoices(new Choice("Continue", () -> buildRadioChoices())); }),
                    new Choice("Silence.", () -> setDesc("\"The opposite. You're listening, but you aren't hearing.\""))
                );
            }
            case 2 -> {
                description = "\"When the world gets loud, what remains?\"";
                setChoices(
                    new Choice("The vending machine.", () -> setDesc("\"Naw. Think less. Feel the playlist.\"")),
                    new Choice("A brick wall.",        () -> setDesc("\"Naw. Think less. Feel the playlist.\"")),
                    new Choice("The music.",           () -> { gs.radioRound = 3; description = "\"Yes. The music remains.\""; setChoices(new Choice("Continue", () -> buildRadioChoices())); }),
                    new Choice("Academic advising.",   () -> setDesc("\"Naw. Think less. Feel the playlist.\""))
                );
            }
            case 3 -> {
                description = "\"And when the music stops?\"";
                setChoices(
                    new Choice("A receipt from CVS.", () -> setDesc("\"Noooo feel the playlist.\"")),
                    new Choice("Less music.",         () -> setDesc("\"Noooo feel the playlist.\"")),
                    new Choice("More music.", () -> {
                        gs.hasRAM4 = true; gs.addItem("RAM Stick 4 (64 GB)");
                        updateRamQuestText(); gs.radioRound = 1;
                        buildRadioChoices();
                        description = "\"Yes... you understand.\"\nRAM Stick 4 obtained.";
                    }),
                    new Choice("Silence, but worse.", () -> setDesc("\"Noooo feel the playlist.\""))
                );
            }
        }
    }

    private void buildGymChoices() {
        if (!gs.gymItemsGiven) {
            description = "You are inside the Fitness Center. Two Gym Enthusiasts stand here with the confidence of 20 gyms.";
            if (!gs.gymQuestStarted) {
                gs.gymQuestStarted = true;
                gs.setQuestText("Bring the Gym Enthusiasts an Ultimate Protein Drink and a Super Chicken Patty.");
            }
            setChoices(
                new Choice("Talk to Gym Enthusiasts", () -> setDesc("\"We can train you, but first you must bring proper fuel. We want one protein drink and one meal that is also high in protein.\"")),
                new Choice("Give Protein Items", () -> {
                    if (gs.hasProteinDrink && gs.hasSuperChickenPatty) {
                        gs.gymItemsGiven = true;
                        gs.removeItem("Ultimate Protein Drink"); gs.removeItem("Super Chicken Patty");
                        gs.hasProteinDrink = false; gs.hasSuperChickenPatty = false;
                        gs.setQuestText("Use all three exercise machines.");
                        buildGymChoices();
                        description = "The Gym Enthusiasts accept the protein offerings with reverence. The equipment is now available.";
                    } else {
                        setDesc("\"You need the Ultimate Protein Drink and the Super Chicken Patty. Strength has rules.\"");
                    }
                }),
                new Choice("Look at Equipment", () -> setDesc("The equipment looks usable, but the Gym Enthusiasts are blocking it with athletic confidence.")),
                back(LocationId.RIGHT_OF_CAMPUS)
            );
        } else if (!gs.canPunchWall) {
            description = "The equipment awaits. Use all three to complete your training.";
            List<Choice> c = new ArrayList<>();
            if (!gs.treadmillDone) c.add(new Choice("Use Treadmill", () -> {
                gs.treadmillDone = true; checkGymComplete(); buildGymChoices();
                description = gs.canPunchWall
                    ? "You feel stronger than ever, like you could punch a brick wall. That's a typical thing people say."
                    : "You run on the treadmill and feel faster than ever. The treadmill can't even keep up with your speed.";
                repaint();
            }));
            if (!gs.weightsDone) c.add(new Choice("Lift Weights", () -> {
                gs.weightsDone = true; checkGymComplete(); buildGymChoices();
                description = gs.canPunchWall
                    ? "You feel stronger than ever, like you could punch a brick wall. That's a typical thing people say."
                    : "You lift the weights. Your arms reposition the weights along the vertical axis IRL.";
                repaint();
            }));
            if (!gs.machineDone) c.add(new Choice("Use Complex Machine", () -> {
                gs.machineDone = true; checkGymComplete(); buildGymChoices();
                description = gs.canPunchWall
                    ? "You feel stronger than ever, like you could punch a brick wall. That's a typical thing people say."
                    : "You use the complex machine. You are not sure what it trained, but something about you had changed.";
                repaint();
            }));
            c.add(back(LocationId.RIGHT_OF_CAMPUS));
            choices = c; hoveredChoice = -1;
        } else {
            description = "All the equipment maximized your strength. You feel like you could punch a brick wall.";
            setChoices(
                new Choice("Flex",        () -> setDesc("You flex. The room respects this.")),
                new Choice("Check Fists", () -> setDesc("Your fists are ready for masonry.")),
                back(LocationId.RIGHT_OF_CAMPUS)
            );
        }
    }

    private void checkGymComplete() {
        if (gs.treadmillDone && gs.weightsDone && gs.machineDone) {
            gs.canPunchWall = true;
            gs.setQuestText("Return to the brick wall and punch it with academic confidence.");
        }
    }

    private void buildSSCEntranceChoices() {
        int count = gs.getRamCount();
        if (!gs.ramInserted && count < 4) {
            description = "The Student Success Center is finally visible. A strange panel beside the door has four RAM slots.\nYou have " + count + " of 4 RAM sticks.";
            gs.setQuestText("Find all four 64 GB RAM sticks. You have " + count + " of 4.");
            setChoices(
                new Choice("Insert RAM",      () -> setDesc("The panel demands four sticks. You have " + gs.getRamCount() + " of 4.")),
                new Choice("Look at Door",    () -> setDesc("The door waits for 256 GB of RAM. ")),
                new Choice("Count RAM Slots", () -> setDesc("There are four RAM slots each with a max capacity of 64 GB.")),
                back(LocationId.CAMPUS_MAP)
            );
        } else if (!gs.ramInserted) {
            description = "All four RAM sticks glow in your hands. The panel awaits.";
            setChoices(
                new Choice("Insert RAM", () -> {
                    gs.ramInserted = true;
                    gs.removeItem("RAM Stick 1 (64 GB)"); gs.removeItem("RAM Stick 2 (64 GB)");
                    gs.removeItem("RAM Stick 3 (64 GB)"); gs.removeItem("RAM Stick 4 (64 GB)");
                    gs.setQuestText("Enter the Student Success Center.");
                    buildSSCEntranceChoices();
                    description = "You insert all four 64 GB RAM sticks. The Student Success Center now has 256 GB of success memory.";
                }),
                new Choice("Look at Door", () -> setDesc("The door waits for 256 GB of RAM.")),
                back(LocationId.CAMPUS_MAP)
            );
        } else {
            description = "The panel is full. The door stands open.";
            setChoices(
                new Choice("Enter Student Success Center", () -> enterLocation(LocationId.STUDENT_SUCCESS_CENTER)),
                back(LocationId.CAMPUS_MAP)
            );
        }
    }

    // MAP NODE BUILDERS
    private void buildMapNodesCampus() {
        mapNodes.add(new MapNode(275, 300, LocationId.BUILDING_16,   "Building 16",   "pin"));
        mapNodes.add(new MapNode(383, 415, LocationId.BUILDING_19,   "Building 19",   "pin"));
        mapNodes.add(new MapNode(768, 415, LocationId.BUILDING_2,    "Building 2",    "pin"));
        mapNodes.add(new MapNode(675, 450, LocationId.THEATER,       "Theater",       "pin"));
        mapNodes.add(new MapNode(250, 375, LocationId.RADIO_STATION, "Radio Station", "pin"));
        mapNodes.add(new MapNode(575, 650, LocationId.BOOKSTORE,     "Bookstore",     "pin"));
        mapNodes.add(new MapNode(225, 455, LocationId.BRICK_WALL,
            gs.wallBroken ? "Student Success" : "BLOCKED",
            gs.wallBroken ? "pin" : "blocked"));
        mapNodes.add(new MapNode( 80, 470, LocationId.LEFT_OF_CAMPUS,  "Left >",  "arrow_left"));
        mapNodes.add(new MapNode(880, 470, LocationId.RIGHT_OF_CAMPUS, "Right >", "arrow_right"));
    }

    private void buildMapNodesLeft() {
        mapNodes.add(new MapNode(440, 500, LocationId.DOLLAR_TREE, "Dollar Tree", "pin"));
        mapNodes.add(new MapNode(145, 425, LocationId.KFC,         "KFC",         "pin"));
        mapNodes.add(new MapNode(815, 535, LocationId.POPEYES,     "Popeyes",     "pin"));
        mapNodes.add(new MapNode(75, 600, LocationId.CVS,         "CVS",         "pin"));
        mapNodes.add(new MapNode(880, 470, LocationId.CAMPUS_MAP,  "Campus >",    "arrow_right"));
    }

    private void buildMapNodesRight() {
        mapNodes.add(new MapNode(570, 470, LocationId.FITNESS_CENTER, "Fitness Center", "pin"));
        mapNodes.add(new MapNode( 80, 470, LocationId.CAMPUS_MAP,     "< Campus",      "arrow_left"));
    }

    // CHICKEN PATTY COMBO
    private void tryMakeSuperChicken() {
        if (gs.hasKFCPatty && gs.hasPopeyesPatty) {
            gs.hasSuperChickenPatty = true;
            gs.removeItem("KFC Chicken Patty"); gs.removeItem("Popeyes Chicken Patty");
            gs.addItem("Super Chicken Patty");
            gs.setQuestText("The Super Chicken Patty is forged. Return to the Fitness Center.");
            setDesc("The KFC Chicken Patty and Popeyes Chicken Patty combine into the Super Chicken Patty. Imagine what you could have done with a Big Mac, Whopper, and a Crunchwrap.");
        } else if (gs.hasKFCPatty) {
            setDesc("You obtain the KFC Chicken Patty.");
            if (!gs.hasSuperChickenPatty) gs.setQuestText("KFC Chicken Patty obtained. Now find the Popeyes Chicken Patty.");
        } else {
            setDesc("You obtain the Popeyes Chicken Patty.");
            if (!gs.hasSuperChickenPatty) gs.setQuestText("Popeyes Chicken Patty obtained. Now find the KFC Chicken Patty.");
        }
    }

    // WIN
    private void win() {
        gs.gameWon = true;
        gs.screen  = ScreenState.WIN_SCREEN;
        hoveredTitle = -1;
        MusicPlayer.playDefault();
        repaint();
    }

    // MOUSE EVENTS
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e)  {}
    @Override public void mouseDragged(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        Point vp = toVirtual(e.getX(), e.getY());
        switch (gs.screen) {
            case TITLE_SCREEN -> {
                int idx = titleButtonIndex(vp);
                if      (idx == 0) { gs.reset(); gs.screen = ScreenState.LOCATION_VIEW; enterLocation(LocationId.STREET); }
                else if (idx == 1) { gs.screen = ScreenState.INSTRUCTIONS; hoveredTitle = -1; MusicPlayer.playDefault(); }
                else if (idx == 2) System.exit(0);
            }
            case INSTRUCTIONS -> { if (hitsBack(vp)) { gs.screen = ScreenState.TITLE_SCREEN; hoveredTitle = -1; } }
            case INVENTORY_VIEW, QUEST_LOG_VIEW -> { if (hitsBack(vp)) { gs.screen = previousScreen; hoveredTitle = -1; } }
            case WIN_SCREEN -> {
                int idx = winButtonIndex(vp);
                if      (idx == 0) { gs.reset(); enterLocation(LocationId.STREET); }
                else if (idx == 1) System.exit(0);
            }
            default -> {
                if (inv(vp)) { previousScreen = gs.screen; gs.screen = ScreenState.INVENTORY_VIEW; hoveredTitle = -1; return; }
                if (qst(vp)) { previousScreen = gs.screen; gs.screen = ScreenState.QUEST_LOG_VIEW; hoveredTitle = -1; return; }
                if (gs.screen == ScreenState.MAP_VIEW)
                    for (MapNode n : mapNodes)
                        if (n.bounds().contains(vp)) { handleMapNodeClick(n); return; }
                for (int i = 0; i < choices.size() && i < 4; i++)
                    if (choiceRect(i).contains(vp)) { choices.get(i).action.run(); repaint(); return; }
            }
        }
        repaint();
    }

    private void handleMapNodeClick(MapNode n) {
        switch (n.type) {
            case "pin" -> {
                description = "Travel to " + n.label + "?";
                setChoices(
                    new Choice("Go There",   () -> enterLocation(n.dest)),
                    new Choice("Never Mind", () -> enterLocation(gs.currentLocation))
                );
            }
            case "blocked" -> {
                if (gs.wallBroken) enterLocation(LocationId.STUDENT_SUCCESS_ENTRANCE);
                else setDesc("The path is blocked. Blocked by a brick wall different from the other brick walls here. Perhaps your fists have an opinion about that.");
            }
            case "arrow_left", "arrow_right" -> enterLocation(n.dest);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Point vp = toVirtual(e.getX(), e.getY());
        hoveredChoice = -1; hoveredTitle = -1; hoveredNode = null; hoveredInvItem = -1;
        hoverInv = false; hoverQst = false;
        switch (gs.screen) {
            case TITLE_SCREEN -> hoveredTitle = titleButtonIndex(vp);
            case INSTRUCTIONS, QUEST_LOG_VIEW -> { if (hitsBack(vp)) hoveredTitle = 0; }
            case INVENTORY_VIEW -> {
                if (hitsBack(vp)) { hoveredTitle = 0; }
                else {
                    List<String> inv = gs.getInventory();
                    for (int i = 0; i < inv.size(); i++)
                        if (inventoryItemRect(i).contains(vp)) { hoveredInvItem = i; break; }
                }
            }
            case WIN_SCREEN -> hoveredTitle = winButtonIndex(vp);
            default -> {
                hoverInv = inv(vp); hoverQst = qst(vp);
                for (int i = 0; i < choices.size() && i < 4; i++)
                    if (choiceRect(i).contains(vp)) { hoveredChoice = i; break; }
                if (gs.screen == ScreenState.MAP_VIEW)
                    for (MapNode n : mapNodes)
                        if (n.bounds().contains(vp)) { hoveredNode = n; break; }
            }
        }
        boolean wantHand = hoveredChoice >= 0 || hoveredTitle >= 0 || hoverInv || hoverQst || hoveredNode != null || hoveredInvItem >= 0;
        setCursor(Cursor.getPredefinedCursor(wantHand ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        repaint();
    }

    @Override public void mouseReleased(MouseEvent e) {}

    // HIT-TEST HELPERS
    private Point toVirtual(int sx, int sy) {
        return new Point((int) ((sx - offX) / scale), (int) ((sy - offY) / scale));
    }

    private boolean inv(Point vp)      { return new Rectangle(INV_X, INV_Y, INV_W, INV_H).contains(vp); }
    private boolean qst(Point vp)      { return new Rectangle(QST_X, QST_Y, QST_W, QST_H).contains(vp); }
    private boolean hitsBack(Point vp) { return new Rectangle(260, 740, 200, 44).contains(vp); }

    private int titleButtonIndex(Point vp) {
        String[] opts = { "Start Game", "Instructions", "Quit" };
        FontMetrics fm = getFontMetrics(F_CHOICE);
        if (fm == null) return -1;
        for (int i = 0; i < opts.length; i++) {
            int oy = 420 + i * 72, ow = fm.stringWidth(opts[i]) + 44, ox = (VW - ow) / 2;
            if (new Rectangle(ox, oy - 30, ow, 50).contains(vp)) return i;
        }
        return -1;
    }

    private int winButtonIndex(Point vp) {
        String[] opts = { "Play Again", "Quit" };
        FontMetrics fm = getFontMetrics(F_CHOICE);
        if (fm == null) return -1;
        for (int i = 0; i < opts.length; i++) {
            int oy = 630 + i * 72, ow = fm.stringWidth(opts[i]) + 44, ox = (VW - ow) / 2;
            if (new Rectangle(ox, oy - 30, ow, 50).contains(vp)) return i;
        }
        return -1;
    }
}
