Java Final Project



1\. Project Overview

Campus Quest is an adventure game written in Java using Swing and AWT. The player wanders the STCC campus talking to various NPCs and a bunch of fetch quests until eventually arriving at the Student Success Center.

**Main.java**: Creates the JFrame and inserts the GamePanel

**GamePanel.java**: Pretty much the whole game. Input, rendering, most logic, and screen states.

**GameState.java**: Inventory list, all the flags for game progression, and quest text.

**Location.java**: custom class for location information, image key, and NPC key

**Choice.java**: menu text and corresponding actions

**LocationId.java**: Enum of all locations the player can be at

**ScreenState.java**: Enum of the UI screens

**AssetManager.java**: Loads PNGs (animated if put into sequence), and returns null if a file is missing

**MusicPlayer.java**: Handles the background music



**Main.java\
**just builds the window

*SwingUtilities.invokeLater* ensures all UI construction happens on the Event Dispatch Thread

setPreferredSize(1280, 720) sets the canvas to 1280x720. And then followed by frame.pack() so that the window borders overlap inside the drawable area.

setLocationRelativeTo(null) centers the window



**ScreenState.java**

There are 7 UIs

TITLE_SCREEN: splash screen with start instructions and quit

INSTRUCTIONS: the how to play overlay

MAP_VIEW: viewing one of the three campus maps with clickable locations

LOCATION_VIEW: Inside a specific location (images, dialogue and four choices)

INVENTORY_VIEW: the inventory list

QUEST_LOG_VIEW: the current quest text

WIN_SCREEN: the ending menu with play again or quit

The paintComponent switch in GamePanel uses this enum to decide which draw routine to call



**Choice.java**

public class Choice {

    public final String text;

    public final Runnable action;

    public Choice(String text, Runnable action) {

        this.text = text;

        this.action = action;

    }

}

Used everywhere to pair a menu label like 'Search parking lot' with the lambda that runs when the player clicks it. Both fields are final and public because Choice is purely a value carrier so encapsulation is not needed

GamePanel keeps a List<Choice> choices field and renders up to four of them in the bottom right panel so that when the player clicks slot *, the panel calls choices.get(*).action.run().



**GameState.java**

This class holds every piece of mutable game state

Boolean flags track quest progress



**AssetManager.java**

class that loads PNGs and caches them. If a file is missing it returns null using callers so a missing asset is skipped rather than crashing the game

If a missing file is confirmed to be missing that is cached using containsKey so a missing file isn't retried on every frame
