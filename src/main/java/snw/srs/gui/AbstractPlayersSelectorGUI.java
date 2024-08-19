package snw.srs.gui;

import com.google.common.collect.ImmutableSet;
import com.google.common.math.IntMath;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import snw.srs.gui.util.ItemBuilder;
import snw.srs.nms.AdapterRetriever;

import java.math.RoundingMode;
import java.util.*;

import static snw.srs.gui.GUIButtonCallback.clickAndRedraw;
import static snw.srs.gui.GUISharedObjects.CANCEL_BUTTON;
import static snw.srs.gui.GUIUtils.runTask;
import static snw.srs.gui.util.BukkitGenericUtils.playSound;

public abstract class AbstractPlayersSelectorGUI extends AbstractPagedGUI {
    private static final int MAX_PLAYERS_PER_PAGE = 28;
    private static final ItemStack SUBMIT_BUTTON;
    private static final ItemStack NOTICE_TARGET_Y;
    private static final ItemStack NOTICE_TARGET_N;
    private static final ItemStack SELECT_ALL;
    private static final ItemStack DESELECT_ALL;
    private final Set<UUID> selectedPlayers = new HashSet<>();
    private UUID pageUniqueId;
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private boolean broadcast;

    static {
        SUBMIT_BUTTON = new ItemBuilder(Material.EMERALD_BLOCK)
                .setDisplayName(ChatColor.GREEN + "完成选择")
                .build();
        NOTICE_TARGET_Y = new ItemBuilder(Material.GLOWSTONE_DUST)
                .setDisplayName("是否通报: " + ChatColor.GREEN + "是")
                .build();
        NOTICE_TARGET_N = new ItemBuilder(Material.REDSTONE)
                .setDisplayName("是否通报: " + ChatColor.RED + "否")
                .build();
        SELECT_ALL = new ItemBuilder(Material.MILK_BUCKET)
                .setDisplayName("全选")
                .build();
        DESELECT_ALL = new ItemBuilder(Material.BUCKET)
                .setDisplayName("全不选")
                .build();
    }

    protected AbstractPlayersSelectorGUI(Plugin plugin, String title) {
        super(plugin, title);
    }

    protected AbstractPlayersSelectorGUI(Plugin plugin, String title, Set<UUID> selected) {
        this(plugin, title);
        this.selectedPlayers.addAll(selected);
    }

    protected abstract OptionalInt getMaxSelectablePlayers();

    @Override
    public OptionalInt getMaxPage() {
        int size = Bukkit.getOnlinePlayers().size();
        int result = IntMath.divide(size, MAX_PLAYERS_PER_PAGE, RoundingMode.CEILING);
        return OptionalInt.of(result);
    }

    protected Collection<? extends Player> getPlayersBaseList() {
        return Bukkit.getOnlinePlayers();
    }

    @Override
    protected void drawImmediatelyImpl(int page) {
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
                if (selectedPlayers.contains(uuid)) {
                    selectStatus = ChatColor.GREEN + "已选择!";
                } else {
                    selectStatus = ChatColor.YELLOW + "点击选择!";
                }
                finalItem = new ItemBuilder(item)
                        .setDisplayName(name)
                        .setLore("", selectStatus)
                        .build();
                int slot = listPosToInvPos(indexPlus);
                getButtonHelper().setButton(slot, finalItem, (clicker, clickType) -> {
                    handlePlayerHeadClick(clicker, uuid);
                    return GUIClickResult.CANCEL_CLICK;
                });
            }, runTask(getPlugin()));
        }
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
                    final String overMax =
                            "%s已达到最大可选数量。最大可选 %d 个玩家。".formatted(
                                    ChatColor.RED, maxSelectablePlayersAsInt);
                    clicker.sendMessage(overMax);
                    playSound(clicker, Sound.BLOCK_ANVIL_PLACE);
                    return;
                }
            }
            selectedPlayers.add(uuid);
        }
        playSound(clicker, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        scheduleDraw();
    }

    @Override
    public void handleClose(Player viewer) {
        // reopen if not disposed yet
        getPlugin().getServer().getScheduler().runTaskLater(getPlugin(), () -> showTo(viewer), 1L);
    }

    @Override
    public void drawButtons() {
        super.drawButtons();

        GUIButtonHelper helper = getButtonHelper();
        helper.setButton(49, SUBMIT_BUTTON, (clicker, clickType) -> {
            if (requireNonEmptySelectedPlayersSet() && selectedPlayers.isEmpty()) {
                clicker.sendMessage(ChatColor.RED + "你尚未选择任何玩家！");
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
        helper.setButton(53, CANCEL_BUTTON, (clicker, clickType) -> {
            dispose(false);
            afterCancel(clicker);
            return GUIClickResult.CANCEL_CLICK;
        });

        helper.setButton(46, SELECT_ALL, clickAndRedraw(this, (clicker, clickType) -> {
            selectedPlayers.addAll(getPlayersBaseList().stream().map(Player::getUniqueId).toList());
        }));
        helper.setButton(47, DESELECT_ALL, clickAndRedraw(this, (clicker, clickType) -> {
            selectedPlayers.clear();
        }));

        if (showBroadcastButton()) {
            final ItemStack switchButton = broadcast ? NOTICE_TARGET_Y : NOTICE_TARGET_N;
            helper.setButton(45, switchButton, clickAndRedraw(this, (clicker, clickType) -> {
                setBroadcast(!isBroadcast());
            }));
        }
    }

    protected abstract boolean onSubmit(Player submitter, Set<UUID> selectedPlayers);

    protected abstract void afterCancel(Player canceller);

    protected boolean requireNonEmptySelectedPlayersSet() {
        return false;
    }

    protected boolean showBroadcastButton() {
        return false;
    }
}
