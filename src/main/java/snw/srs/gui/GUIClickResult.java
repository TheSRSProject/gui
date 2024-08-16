package snw.srs.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public enum GUIClickResult {
    /**
     * Don't change anything about the event,
     * allowing player to take items (if they clicked valid slots).
     */
    ALLOW_CLICK,
    /**
     * Use this if you cannot handle the click (e.g. don't know the meaning of the click). <br>
     * When the {@link AbstractPluginGUI#handleClick(Player, int, ClickType)}
     * method finally returned this, it is equivalent to {@link #CANCEL_CLICK}.
     */
    NOP,
    /**
     * Cancel the click event to prevent them from taking the items.
     */
    CANCEL_CLICK
}
