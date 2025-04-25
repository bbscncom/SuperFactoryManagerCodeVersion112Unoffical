package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.common.container.ManagerContainerMenu;
import ca.teamdman.sfm.common.localization.LocalizationKeys;
import ca.teamdman.sfm.common.registry.SFMPackets;
import ca.teamdman.sfm.common.registry.SFMResourceTypes;
import ca.teamdman.sfm.common.resourcetype.ResourceType;
import ca.teamdman.sfm.common.util.SFMASTUtils;
import ca.teamdman.sfm.common.util.SFMDirections;
import ca.teamdman.sfml.ast.*;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import my.Tools;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class ServerboundContainerExportsInspectionRequestPacket implements SFMMessage<ServerboundContainerExportsInspectionRequestPacket, IMessage> {
    public int windowId;
    public BlockPos pos;

    public ServerboundContainerExportsInspectionRequestPacket() {
    }

    public ServerboundContainerExportsInspectionRequestPacket(int windowId, BlockPos pos) {
        this.windowId=windowId;
        this.pos=pos;
    }

    public static String buildInspectionResults(
            World level,
            BlockPos pos
    ) {
        StringBuilder sb = new StringBuilder();
        for (EnumFacing direction : SFMDirections.DIRECTIONS_WITH_NULL) {
            sb.append("-- ").append(direction).append("\n");
            int len = sb.length();
            //noinspection unchecked,rawtypes
            SFMResourceTypes.RESOURCE_TYPES.forEach((resourceLocation, resourceType) -> {
                String s = buildInspectionResults(
                        resourceLocation,
                        resourceType,
                        level,
                        pos,
                        direction
                );
                if(!s.isEmpty()){
                    sb.append(s).append("\n");
                }
            });
            if (sb.length() == len) {
                sb.append("No exports found");
            }
            sb.append("\n");
        }

//        if (SFMModCompat.isMekanismLoaded()) {
//            BlockEntity be = level.getBlockEntity(pos);
//            if (be != null) {
//                sb.append(SFMMekanismCompat.gatherInspectionResults(be)).append("\n");
//            }
//        }

        return sb.toString();
    }

    public static <STACK, ITEM, CAP> String buildInspectionResults(
            ResourceLocation resourceLocation,
            ResourceType<STACK, ITEM, CAP> resourceType,
            World level,
            BlockPos pos,
            @Nullable EnumFacing direction
    ) {
        StringBuilder sb = new StringBuilder();
        CAP cap = level.getTileEntity(pos).getCapability(resourceType.CAPABILITY_KIND, direction);
        if (cap != null) {
            int slots = resourceType.getSlots(cap);
            Int2ObjectMap<STACK> slotContents = new Int2ObjectArrayMap<>(slots);
            for (int slot = 0; slot < slots; slot++) {
                STACK stack = resourceType.getStackInSlot(cap, slot);
                if (!resourceType.isEmpty(stack)) {
                    slotContents.put(slot, stack);
                }
            }

            if (!slotContents.isEmpty()) {
                slotContents.forEach((slot, stack) -> {
                    InputStatement inputStatement = SFMASTUtils.getInputStatementForStack(
                            resourceLocation,
                            resourceType,
                            stack,
                            "target",
                            slot,
                            false,
                            direction
                    );
                    sb.append(inputStatement.toStringPretty()).append("\n");
                });

                List<ResourceLimit> resourceLimitList = new ArrayList<>();
                slotContents.forEach((slot, stack) -> {
                    ResourceLocation stackId = resourceType.getRegistryKeyForStack(stack);
                    ResourceIdentifier<STACK, ITEM, CAP> resourceIdentifier = new ResourceIdentifier<>(
                            resourceLocation,
                            stackId
                    );
                    ResourceLimit resourceLimit = new ResourceLimit(
                            new ResourceIdSet(Collections.singletonList(resourceIdentifier)),
                            Limit.MAX_QUANTITY_NO_RETENTION, With.ALWAYS_TRUE
                    );
                    resourceLimitList.add(resourceLimit);
                });
                InputStatement inputStatement = new InputStatement(
                        new LabelAccess(
                                Collections.singletonList(new Label("target")),
                                new DirectionQualifier(direction == null
                                                       ? EnumSet.noneOf(EnumFacing.class)
                                                       : EnumSet.of(direction)),
                                NumberRangeSet.MAX_RANGE,
                                RoundRobin.disabled()
                        ),
                        new ResourceLimits(
                                resourceLimitList.stream().distinct().collect(Collectors.toList()),
                                ResourceIdSet.EMPTY
                        ),
                        false
                );
                sb.append(inputStatement.toStringPretty());
            }
        }
        String result = sb.toString();
        if (!result.isEmpty()) {
            TileEntity be = level.getTileEntity(pos);
            //noinspection DataFlowIssue
            if (be != null && direction == null && be.getClass().getName().contains("mekanism")) {
                return "-- "
                       + LocalizationKeys.CONTAINER_INSPECTOR_MEKANISM_NULL_DIRECTION_WARNING.getStub()
                       + "\n"
                       + result;
            }
        }
        return result;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeVarInt(windowId);
        packetBuffer.writeBlockPos(pos);
    }

    @Override
    public IMessage onMessage(ServerboundContainerExportsInspectionRequestPacket msg, MessageContext context) {
        new SFMPacketHandlingContext(context).handleServerboundContainerPacket(
                Container.class,
                TileEntity.class,
                msg.pos,
                msg.windowId,
                (menu, blockEntity) -> {
                    assert blockEntity.getWorld() != null;
                    String payload = buildInspectionResults(blockEntity.getWorld(), blockEntity.getPos());
                    EntityPlayerMP player = context.getServerHandler().player;

                    SFMPackets.sendToPlayer(player, new ClientboundContainerExportsInspectionResultsPacket(
                            msg.windowId,
                            Tools.truncate(
                                    payload,
                                    ClientboundContainerExportsInspectionResultsPacket.MAX_RESULTS_LENGTH
                            )
                    ));
                }
        );
        return null;
    }

    @Override
    public Side getSide() {
        return Side.SERVER;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        this.windowId=packetBuffer.readVarInt();
        this.pos=packetBuffer.readBlockPos();
    }


}
