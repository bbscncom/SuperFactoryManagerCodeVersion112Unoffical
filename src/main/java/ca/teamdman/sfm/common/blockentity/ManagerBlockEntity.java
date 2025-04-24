package ca.teamdman.sfm.common.blockentity;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.common.config.SFMServerConfig;
import ca.teamdman.sfm.common.container.ManagerContainerMenu;
import ca.teamdman.sfm.common.handler.OpenContainerTracker;
import ca.teamdman.sfm.common.item.DiskItem;
import ca.teamdman.sfm.common.localization.LocalizationEntry;
import ca.teamdman.sfm.common.localization.LocalizationKeys;
import ca.teamdman.sfm.common.logging.TranslatableLogEvent;
import ca.teamdman.sfm.common.logging.TranslatableLogger;
import ca.teamdman.sfm.common.net.ClientboundManagerGuiUpdatePacket;
import ca.teamdman.sfm.common.net.ClientboundManagerLogLevelUpdatedPacket;
import ca.teamdman.sfm.common.net.ClientboundManagerLogsPacket;
import ca.teamdman.sfm.common.program.LabelPositionHolder;
import ca.teamdman.sfm.common.registry.SFMPackets;
import ca.teamdman.sfm.common.util.SFMContainerUtil;
import ca.teamdman.sfml.ast.Program;
import my.ContainerHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.apache.logging.log4j.core.time.MutableInstant;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Set;

public class ManagerBlockEntity extends TileEntity implements ITickable, IInventory {
    public static final int TICK_TIME_HISTORY_SIZE = 20;
    public final TranslatableLogger logger;
    private final NonNullList<ItemStack> ITEMS = NonNullList.withSize(1, ItemStack.EMPTY);
    private final long[] tickTimeNanos = new long[TICK_TIME_HISTORY_SIZE];
    private @Nullable Program program = null;
    private int configRevision = -1;
    private int tick = 0;
    private int unprocessedRedstonePulses = 0; // used by redstone trigger
    private boolean shouldRebuildProgram = false;
    private boolean shouldRebuildProgramLock = false;
    private int tickIndex = 0;


    public ManagerBlockEntity() {
        super();
        // Logger name should be unique to (isClient,managerpos)
        // We can't check isClient here, so instead to guarantee uniqueness we can just use hash
        // This is necessary because setLogLevel in game tests will get clobbered when the client constructs the block entity
        // so the name must be unique so that the client default logger construction doesn't overwrite changes to the server logger
        String loggerName = SFM.MOD_ID 
                            + ":manager@"
                            + pos.toString() + "@" + Integer.toHexString(System.identityHashCode(this));
        logger = new TranslatableLogger(loggerName);
    }
    @Override
    public String toString() {
        return "ManagerBlockEntity{" +
               "hasDisk=" + (getDisk() != null) +
               '}';
    }

    /**
     * Used to prevent tests which modify configs from interfering with other tests.
     * <p>
     * When the manager detects a config change and rebuilds, it clobbers the monkey patching used by the tests.
     */
    public void enableRebuildProgramLock() {
        shouldRebuildProgramLock = true;
    }

    public static void serverTick(
            World world,
            BlockPos pos,
            IBlockState state,
            ManagerBlockEntity manager
    ) {
        try {
            long start = System.nanoTime();
            manager.tick++;
            if (manager.configRevision != SFMServerConfig.getRevision()) {
                manager.shouldRebuildProgram = true;
            }
            if (manager.shouldRebuildProgram && !manager.shouldRebuildProgramLock) {
                manager.rebuildProgramAndUpdateDisk();
                manager.shouldRebuildProgram = false;
            }
            if (manager.program != null) {
                boolean didSomething = manager.program.tick(manager);
                if (didSomething) {
                    long nanoTimePassed = Math.min(System.nanoTime() - start, Integer.MAX_VALUE);
                    manager.tickTimeNanos[manager.tickIndex] = (int) nanoTimePassed;
                    manager.tickIndex = (manager.tickIndex + 1) % manager.tickTimeNanos.length;
                    manager.logger.trace(x -> x.accept(LocalizationKeys.PROGRAM_TICK_TIME_MS.get(nanoTimePassed
                                                                                                 / 1_000_000f)));
                    manager.sendUpdatePacket();
                    manager.logger.pruneSoWeDontEatAllTheRam();

                    if (manager.logger.getLogLevel() == org.apache.logging.log4j.Level.TRACE
                        || manager.logger.getLogLevel() == org.apache.logging.log4j.Level.DEBUG
                        || manager.logger.getLogLevel() == org.apache.logging.log4j.Level.INFO) {
                        org.apache.logging.log4j.Level newLevel = org.apache.logging.log4j.Level.OFF;
                        manager.logger.info(x -> x.accept(LocalizationKeys.LOG_LEVEL_UPDATED.get(newLevel)));
                        org.apache.logging.log4j.Level oldLevel = manager.logger.getLogLevel();
                        manager.setLogLevel(newLevel);
                        SFM.LOGGER.debug(
                                "SFM updated manager {} {} log level to {} after a single execution at {} level",
                                manager.getPos(),
                                manager.getWorld(),
                                newLevel,
                                oldLevel
                        );
                    }
                }
            }
        } catch (Throwable t) {
            // tell the user that they can disable the manager in the config
           
            throw t;
        }
    }

    @Override
    public void addInfoToCrashReport(CrashReportCategory category) {
        super.addInfoToCrashReport(category);
        {
            category.addCrashSection("SFM Reminder", "error info delete ,addInfoToCrashReport");
        }
        {
            ItemStack disk = getDisk();
            if (disk != null && !disk.isEmpty()) {
                category.addCrashSection("SFM Details", "error info delete ,addInfoToCrashReport");
            }
        }
    }

    public void setLogLevel(org.apache.logging.log4j.Level logLevelObj) {
        logger.setLogLevel(logLevelObj);
        sendUpdatePacket();
    }

    public int getTick() {
        return tick;
    }

    public @Nullable Program getProgram() {
        return program;
    }

    public void setProgram(String program) {
        ItemStack disk = getDisk();
        if (disk != null) {
            DiskItem.setProgram(disk, program);
            rebuildProgramAndUpdateDisk();
            markDirty();
        }
    }

    public void trackRedstonePulseUnprocessed() {
        unprocessedRedstonePulses++;
    }

    public void clearRedstonePulseQueue() {
        unprocessedRedstonePulses = 0;
    }

    public int getUnprocessedRedstonePulseCount() {
        return unprocessedRedstonePulses;
    }

    public State getState() {
        if (getDisk() == null) return State.NO_DISK;
        if (getProgramString() == null) return State.NO_PROGRAM;
        if (program == null) return State.INVALID_PROGRAM;
        return State.RUNNING;
    }

    public @Nullable String getProgramString() {
        ItemStack disk = getDisk();
        if (disk == null) {
            return null;
        }

        String program = DiskItem.getProgram(disk);
        return program.isEmpty() ? null : program;
    }

    public String getProgramStringOrEmptyIfNull() {
        String programString = this.getProgramString();
        return programString == null ? "" : programString;
    }

    public Set<String> getReferencedLabels() {
        if (program == null) return Collections.emptySet();
        return program.referencedLabels;
    }

    public @Nullable ItemStack getDisk() { // TODO: make this not nullable, should be fine to return empty :P
        ItemStack item = getStackInSlot(0);
        if (item.getItem() instanceof DiskItem) return item;
        return null;
    }

    public void rebuildProgramAndUpdateDisk() {
        if (world != null && world.isRemote) return; 
        ItemStack disk = getDisk();
        if (disk == null) {
            this.program = null;
        } else {
            this.program = DiskItem.compileAndUpdateErrorsAndWarnings(disk, this);
        }
        this.configRevision = SFMServerConfig.getRevision();
        sendUpdatePacket();
    }

    @Override
    public int getSizeInventory() {
        return ITEMS.size();
    }

    @Override
    public boolean isEmpty() {
        return ITEMS.isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot < 0 || slot >= ITEMS.size()) return ItemStack.EMPTY;
        return ITEMS.get(slot);
    }


    @Override
    public ItemStack decrStackSize(
            int slot,
            int amount
    ) {
        ItemStack result = ContainerHelper.removeItem(ITEMS, slot, amount);
        if (slot == 0) rebuildProgramAndUpdateDisk();
        markDirty();
        return result;
    }

    @Override
    public ItemStack removeStackFromSlot(int slot) {
        ItemStack result = ContainerHelper.takeItem(ITEMS, slot);
        if (slot == 0) rebuildProgramAndUpdateDisk();
        markDirty();
        return result;
    }


    @Override
    public void setInventorySlotContents(
            int slot,
            ItemStack stack
    ) {
        if (slot < 0 || slot >= ITEMS.size()) return;
        ITEMS.set(slot, stack);
        if (slot == 0) rebuildProgramAndUpdateDisk();
        markDirty();
    }

    protected NonNullList<ItemStack> getItems() {
        return ITEMS;
    }

    protected void setItems(NonNullList<ItemStack> pItems) {
        ITEMS.clear();
        ITEMS.addAll(pItems);
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isItemValidForSlot(
            int slot,
            ItemStack stack
    ) {
        return stack.getItem() instanceof DiskItem;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return SFMContainerUtil.isUseableByPlayer(this, player);
    }

    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }


    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        ContainerHelper.saveAllItems(compound, ITEMS,true);
        return compound;
    }


    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        ContainerHelper.loadAllItems(compound, ITEMS);
        this.shouldRebuildProgram = true;
        if (world != null) {
            this.tick = world.rand.nextInt();
        }
    }

    public void reset() {
        ItemStack disk = getDisk();
        if (disk != null) {
            LabelPositionHolder.clearNbt(disk);
            DiskItem.clearData(disk);
            setInventorySlotContents(0, disk);
            markDirty();
        }
    }

    public long[] getTickTimeNanos() {
        // tickTimeNanos is used as a cyclical buffer, transform it to have the first index be the most recent tick
        long[] result = new long[tickTimeNanos.length];
        System.arraycopy(tickTimeNanos, tickIndex, result, 0, tickTimeNanos.length - tickIndex);
        System.arraycopy(tickTimeNanos, 0, result, tickTimeNanos.length - tickIndex, tickIndex);
        return result;
    }

    public void sendUpdatePacket() {
        // Create one packet and clone it for each receiver
        ClientboundManagerGuiUpdatePacket managerUpdatePacket = new ClientboundManagerGuiUpdatePacket(
                -1,
                getProgramStringOrEmptyIfNull(),
                getState(),
                getTickTimeNanos()
        );

        OpenContainerTracker.getOpenManagerMenus(getPos())
                .forEach(entry -> {
                    ManagerContainerMenu menu = entry.getValue();

                    // Send a copy of the manager update packet
                    SFMPackets.sendToPlayer(entry.getKey(), managerUpdatePacket.cloneWithWindowId(menu.windowId));

                    // The rest of the sync is only relevant if the log screen is open
                    if (!menu.isLogScreenOpen) return;

                    // Send log level changes
                    if (!menu.logLevel.equals(logger.getLogLevel().name())) {
                        SFMPackets.sendToPlayer(entry.getKey(), new ClientboundManagerLogLevelUpdatedPacket(
                                menu.windowId,
                                logger.getLogLevel().name()
                        ));
                        menu.logLevel = logger.getLogLevel().name();
                    }

                    // Send new logs
                    MutableInstant hasSince = new MutableInstant();
                    if (!menu.logs.isEmpty()) {
                        hasSince.initFrom(menu.logs.getLast().instant());
                    }
                    ArrayDeque<TranslatableLogEvent> logsToSend = logger.getLogsAfter(hasSince);
                    if (!logsToSend.isEmpty()) {
                        // Add the latest entry to the server copy
                        // since the server copy is only used for checking what the latest log timestamp is
                        menu.logs.add(logsToSend.getLast());

                        // Send the logs
                        while (!logsToSend.isEmpty()) {
                            int remaining = logsToSend.size();
                            SFMPackets.sendToPlayer(entry.getKey(), ClientboundManagerLogsPacket.drainToCreate(
                                    menu.windowId,
                                    logsToSend
                            ));
                            if (logsToSend.size() >= remaining) {
                                throw new IllegalStateException("Failed to send logs, infinite loop detected");
                            }
                        }
                    }
                });
    }


    @Override
    public void update() {
        if (world.isRemote) return;
        serverTick(world, pos, world.getBlockState(pos), this);
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }


    public enum State {
        NO_PROGRAM(
                TextFormatting.RED,
                LocalizationKeys.MANAGER_GUI_STATE_NO_PROGRAM
        ), NO_DISK(
                TextFormatting.RED,
                LocalizationKeys.MANAGER_GUI_STATE_NO_DISK
        ), RUNNING(TextFormatting.GREEN, LocalizationKeys.MANAGER_GUI_STATE_RUNNING), INVALID_PROGRAM(
                TextFormatting.DARK_RED,
                LocalizationKeys.MANAGER_GUI_STATE_INVALID_PROGRAM
        );

        public final TextFormatting COLOR;
        public final LocalizationEntry LOC;

        State(
                TextFormatting color,
                LocalizationEntry loc
        ) {
            COLOR = color;
            LOC = loc;
        }
    }

}
