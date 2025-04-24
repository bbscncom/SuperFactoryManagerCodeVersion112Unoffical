package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.common.blockentity.ManagerBlockEntity;
import ca.teamdman.sfm.common.container.ManagerContainerMenu;
import ca.teamdman.sfm.common.registry.SFMPackets;
import ca.teamdman.sfm.common.util.Stored;
import ca.teamdman.sfml.ast.Program;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;

public class SFMPacketHandlingContext {
    private final MessageContext inner;

    public SFMPacketHandlingContext(MessageContext inner) {
        this.inner = inner;
    }

    public @Nullable EntityPlayerMP sender() {
        return inner.getServerHandler().player;
    }

    public void finish() {
        // inner.setPacketHandled(true);
    }

    public void enqueueAndFinish(Runnable runnable) {
        inner.getServerHandler().player.getServerWorld().addScheduledTask(runnable);
        finish();
    }

    public <MENU extends Container, BE extends TileEntity> void handleServerboundContainerPacket(
            Class<MENU> menuClass,
            Class<BE> blockEntityClass,
            @Stored BlockPos pos,
            int containerId,
            BiConsumer<MENU, BE> callback
    ) {
        handleServerboundContainerPacket(
                this,
                menuClass,
                blockEntityClass,
                pos,
                containerId,
                callback
        );
    }

    public static <MENU extends Container, BE extends TileEntity> void handleServerboundContainerPacket(
            SFMPacketHandlingContext ctx,
            Class<MENU> menuClass,
            Class<BE> blockEntityClass,
            BlockPos pos,
            int containerId,
            BiConsumer<MENU, BE> callback
    ) {
        EntityPlayerMP sender = ctx.sender();
        if (sender == null) {
            SFM.LOGGER.warn("Invalid packet received: no sender");
            return;
        }
        if (sender.isSpectator()) {
            SFM.LOGGER.warn("Invalid packet received from {}: sender is spectator", sender.getName());
            return;
        }

        Container menu = sender.openContainer;
        if (!menuClass.isInstance(menu)) {
            SFM.LOGGER.warn(
                    "Invalid packet received from {}: menu is not instance of expected class",
                    sender.getName()
            );
            return;
        }
        if (menu.windowId != containerId) {
            SFM.LOGGER.warn(
                    "Invalid packet received from {}: containerId does not match",
                    sender.getName()
            );
            return;
        }

        World level = sender.world;
        if (level == null) {
            SFM.LOGGER.warn("Invalid packet received from {}: level is null", sender.getName());
            return;
        }
        if (!level.isBlockLoaded(pos)) {
            SFM.LOGGER.warn(
                    "Invalid packet received from {}: block entity is not loaded",
                    sender.getName()
            );
            return;
        }

        TileEntity blockEntity = level.getTileEntity(pos);
        if (!blockEntityClass.isInstance(blockEntity)) {
            SFM.LOGGER.warn(
                    "Invalid packet received from {}: block entity is not instance of expected class",
                    sender.getName()
            );
            return;
        }
        callback.accept(menuClass.cast(menu), blockEntityClass.cast(blockEntity));
    }

    public void compileAndThen(
            String programString,
            ProgramConsumer callback
    ) {
        EntityPlayerMP player = this.sender();
        if (player == null) return;
        ManagerBlockEntity manager;
        if (player.openContainer instanceof ManagerContainerMenu) {
            ManagerContainerMenu mcm = (ManagerContainerMenu) player.openContainer;
            TileEntity te = player.world.getTileEntity(mcm.MANAGER_POSITION);
            if (te instanceof ManagerBlockEntity) {
                manager = (ManagerBlockEntity) te;
            } else {
                return;
            }
        } else {
            //todo: localize
            SFMPackets.sendToPlayer(player, new ClientboundInputInspectionResultsPacket(
                    "This inspection is only available when editing inside a manager."));
            return;
        }
        Program.compile(
                programString,
                successProgram -> callback.accept(successProgram, player, manager),
                failure -> {
                    //todo: localize
                    SFMPackets.sendToPlayer(
                            player,
                            new ClientboundOutputInspectionResultsPacket("failed to compile program")
                    );
                }
        );
    }

    @FunctionalInterface
    public interface ProgramConsumer {
        void accept(
                Program program,
                EntityPlayerMP player,
                ManagerBlockEntity managerBlockEntity
        );
    }
}
