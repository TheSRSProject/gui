package snw.srs.gui;

import com.google.common.collect.ImmutableSet;
import com.google.common.math.IntMath;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import snw.srs.gui.util.ItemBuilder;
import snw.srs.gui.util.TranslatedItem;
import snw.srs.nms.AdapterRetriever;

import java.math.RoundingMode;
import java.util.*;

import static snw.srs.gui.GUIButtonCallback.clickAndRedraw;
import static snw.srs.gui.GUISharedObjects.CANCEL_BUTTON;
import static snw.srs.gui.GUIUtils.runTask;
import static snw.srs.gui.i18n.TranslationKeys.*;
import static snw.srs.gui.util.BukkitGenericUtils.playSound;

public abstract class AbstractPlayersSelectorGUI extends AbstractPagedGUI {
    private static final int MAX_PLAYERS_PER_PAGE = 28;
    private static final TranslatedItem SUBMIT_BUTTON;
    private static final TranslatedItem SELECT_ALL;
    private static final TranslatedItem DESELECT_ALL;
    protected final Set<UUID> selectedPlayers = new HashSet<>();
    private UUID pageUniqueId;

    static {
        SUBMIT_BUTTON = new TranslatedItem(PLAYER_SELECTOR_FINISH, Material.EMERALD_BLOCK, ChatColor.GREEN);
        SELECT_ALL = new TranslatedItem(PLAYER_SELECTOR_SELECT_ALL, Material.MILK_BUCKET);
        DESELECT_ALL = new TranslatedItem(PLAYER_SELECTOR_DESELECT_ALL, Material.BUCKET);
    }

    @Deprecated
    protected AbstractPlayersSelectorGUI(Plugin plugin, String title, UUID viewer) {
        super(plugin, title, viewer);
    }

    @Deprecated
    protected AbstractPlayersSelectorGUI(Plugin plugin, String title, UUID viewer, Set<UUID> selected) {
        this(plugin, LegacyComponentSerializer.legacySection().deserialize(title), viewer, selected);
    }

    protected AbstractPlayersSelectorGUI(Plugin plugin, Component title, UUID viewer) {
        super(plugin, title, viewer);
    }

    protected AbstractPlayersSelectorGUI(Plugin plugin, Component title, UUID viewer, Set<UUID> selected) {
        this(plugin, title, viewer);
        this.selectedPlayers.addAll(selected);
    }

    protected abstract OptionalInt getMaxSelectablePlayers();

    @Override
    public OptionalInt getMaxPage() {
        int size = getPlayersBaseList().size();
        int result = IntMath.divide(size, MAX_PLAYERS_PER_PAGE, RoundingMode.CEILING);
        return OptionalInt.of(result);
    }

    protected Collection<? extends Player> getPlayersBaseList() {
        return Bukkit.getOnlinePlayers();
    }

    @Override
    protected void drawImmediatelyImpl(int page) {
        Player viewerHandle = Bukkit.getPlayer(getViewer());
        if (viewerHandle == null) {
            return;
        }
        pageUniqueId = UUID.randomUUID();
        drawFrame();
        drawButtons();

        List<? extends Player> players = new ArrayList<>(getPlayersBaseList())
                .stream()
                .sorted(Comparator.comparing((Player o) -> o.getName()))
                .skip((long) (page - 1) * MAX_PLAYERS_PER_PAGE)
                .toList();

        final UUID pageId = pageUniqueId;
        for (int i = 0; i < players.size() && i <= (MAX_PLAYERS_PER_PAGE - 1); i++) {
            Player player = players.get(i);
            String name = player.getName();
            UUID uuid = player.getUniqueId();
            final int indexPlus = i + 1;
            AdapterRetriever.ADAPTER.getPlayerHead(player, getPlugin()).thenAcceptAsync(item -> {
                if (!pageUniqueId.equals(pageId)) { // page changed
                    return;
                }
                final ItemStack finalItem;
                final String selectStatus;
                final ChatColor nameColor;
                final String nameKey;
                if (selectedPlayers.contains(uuid)) {
                    nameColor = ChatColor.GREEN;
                    nameKey = PLAYER_SELECTOR_SELECTED;
                } else {
                    nameColor = ChatColor.YELLOW;
                    nameKey = PLAYER_SELECTOR_SELECT_PROMPT;
                }
                // we regard name as template because there is no argument to fill in
                selectStatus = nameColor + getI18nEngine().getTemplateOrAsIs(viewerHandle, nameKey);
                final ItemBuilder builder = new ItemBuilder(item)
                        .setDisplayName(name)
                        .setLore("", selectStatus);
                postProcessPlayerHeadButton(player, builder);
                finalItem = builder.build();
                int slot = listPosToInvPos(indexPlus);
                getButtonHelper().setButton(slot, finalItem, (clicker, clickType) -> {
                    handlePlayerHeadClick(clicker, uuid);
                    return GUIClickResult.CANCEL_CLICK;
                });
            }, runTask(getPlugin()));
        }
    }

    protected void postProcessPlayerHeadButton(Player who, ItemBuilder builder) {
    }

    private int listPosToInvPos(int indexPlus) {
        final int row = indexPlus / 7;
        final int col = indexPlus % 7;
        final int base = 8; // skip first line
        final int skipped = row * 9;
        final int off;
        if (col == 0) {
            off = -2;
        } else {
            off = 0;
        }
        return base + skipped + 1 + col + off;
    }

    @Override
    public GUIClickResult handleClick(Player clicker, int slot, ClickType clickType) {
        GUIClickResult superResult = super.handleClick(clicker, slot, clickType);
        if (superResult != GUIClickResult.NOP) {
            return superResult;
        }
        return getButtonHelper().handleClick(clicker, slot, clickType);
    }

    private void handlePlayerHeadClick(Player clicker, UUID uuid) {
        if (selectedPlayers.contains(uuid)) {
            selectedPlayers.remove(uuid);
        } else {
            OptionalInt maxSelectablePlayers = getMaxSelectablePlayers();
            if (maxSelectablePlayers.isPresent()) {
                int maxSelectablePlayersAsInt = maxSelectablePlayers.getAsInt();
                if (selectedPlayers.size() > maxSelectablePlayersAsInt) {
                    handleSelectionOverLimit(clicker, maxSelectablePlayersAsInt);
                    return;
                }
            }
            selectedPlayers.add(uuid);
        }
        playSound(clicker, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        scheduleDraw();
    }

    protected void handleSelectionOverLimit(Player clicker, int max) {
        final String overMax = ChatColor.RED +
                getI18nEngine().formatMessage(clicker, PLAYER_SELECTOR_OVER_LIMIT, List.of(max));
        clicker.sendMessage(overMax);
        playSound(clicker, Sound.BLOCK_ANVIL_PLACE);
    }

    @Override
    public void handleClose() {
        super.handleClose();
        // reopen if not disposed yet
        getPlugin().getServer().getScheduler().runTaskLater(getPlugin(), this::show, 1L);
    }

    @Override
    protected void drawButtons() {
        super.drawButtons();

        GUIButtonHelper helper = getButtonHelper();
        helper.setButton(49, buildTranslated(SUBMIT_BUTTON), (clicker, clickType) -> {
            if (requireNonEmptySelectedPlayersSet() && selectedPlayers.isEmpty()) {
                String failMessage = getI18nEngine().getTemplateOrAsIs(clicker, PLAYER_SELECTOR_NOTHING_WAS_SELECTED);
                clicker.sendMessage(ChatColor.RED + failMessage);
                playSound(clicker, Sound.BLOCK_ANVIL_PLACE);
            } else {
                ImmutableSet<UUID> selected = ImmutableSet.copyOf(selectedPlayers);
                boolean ok = onSubmit(clicker, selected);
                if (ok) {
                    dispose(false);
                }
            }
            return GUIClickResult.CANCEL_CLICK;
        });
        helper.setButton(53, buildTranslated(CANCEL_BUTTON), (clicker, clickType) -> {
            dispose(false);
            afterCancel(clicker);
            return GUIClickResult.CANCEL_CLICK;
        });

        if (getMaxSelectablePlayers().isEmpty()) {
            helper.setButton(46, buildTranslated(SELECT_ALL), clickAndRedraw(this, (clicker, clickType) -> {
                selectedPlayers.addAll(getPlayersBaseList().stream().map(Player::getUniqueId).toList());
            }));
            helper.setButton(47, buildTranslated(DESELECT_ALL), clickAndRedraw(this, (clicker, clickType) -> {
                selectedPlayers.clear();
            }));
        }

        if (showBroadcastButton()) {
            getOrCreateBroadcastChoiceHelper().drawButton(helper);
        }
    }

    protected abstract boolean onSubmit(Player submitter, Set<UUID> selectedPlayers);

    protected abstract void afterCancel(Player canceller);

    protected boolean requireNonEmptySelectedPlayersSet() {
        return false;
    }

    // The following deprecated things are only exist for backwards compatibility, DO NOT USE.
    // They will be removed in a random commit, so they are not reliable.
    @Deprecated(forRemoval = true)
    public boolean isBroadcast() {
        return broadcastChoiceHelper != null && broadcastChoiceHelper.isStatusTrue();
    }

    @Deprecated(forRemoval = true)
    public void setBroadcast(boolean broadcast) {
        getOrCreateBroadcastChoiceHelper().setStatusTrue(broadcast);
    }

    @Deprecated(forRemoval = true)
    private BroadcastChoiceHelper broadcastChoiceHelper;
    @Deprecated(forRemoval = true)
    private BroadcastChoiceHelper getOrCreateBroadcastChoiceHelper() {
        if (broadcastChoiceHelper == null) {
            broadcastChoiceHelper = new BroadcastChoiceHelper(this, 45);
        }
        return broadcastChoiceHelper;
    }
    /**
     * @deprecated Use {@link BroadcastChoiceHelper#drawButton(GUIButtonHelper)} on
     *  your own instance of it if you need to show broadcast button.
     */
    @Deprecated(forRemoval = true)
    protected boolean showBroadcastButton() {
        return false;
    }
}
