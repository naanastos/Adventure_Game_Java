public class Choice {
    public final String text;
    public final Runnable action;

    public Choice(String text, Runnable action) {
        this.text   = text;
        this.action = action;
    }
}