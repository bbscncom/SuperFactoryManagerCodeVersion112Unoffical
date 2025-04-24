package ca.teamdman.sfm.common.registry;

import ca.teamdman.sfm.common.capability.*;
import ca.teamdman.sfm.common.item.LabelGunItem;
import ca.teamdman.sfm.common.program.LabelPositionHolder;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Base64;

public class SFMDataComponents {
    private static final CapabilityManager register = CapabilityManager.INSTANCE;
    @CapabilityInject(ProgramData.class)
    public static Capability<ProgramData> PROGRAM_DATA;
    @CapabilityInject(LabelPositionHolder.class)
    public static Capability<LabelPositionHolder> LABEL_POSITION_HOLDER;
    @CapabilityInject(Errors.class)
    public static Capability<Errors> ERRORS;
    @CapabilityInject(Warnings.class)
    public static Capability<Warnings> WARNINGS;
    @CapabilityInject(ActiveLabel.class)
    public static Capability<ActiveLabel> ACTIVE_LABEL;
    @CapabilityInject(LabelGunViewMod.class)
    public static Capability<LabelGunViewMod> LABEL_GUN_VIEW_MODE;

    public static void init() {
        register.register(ProgramData.class, new Capability.IStorage<ProgramData>() {
            @Nullable
            @Override
            public NBTBase writeNBT(Capability<ProgramData> capability, ProgramData instance, EnumFacing side) {
                NBTTagCompound nbtTagCompound = new NBTTagCompound();
                nbtTagCompound.setString("data", instance.get());
                return nbtTagCompound;
            }

            @Override
            public void readNBT(Capability<ProgramData> capability, ProgramData instance, EnumFacing side, NBTBase nbt) {
                NBTTagCompound nbt1 = (NBTTagCompound) nbt;
                instance.set(nbt1.getString("data"));
            }
        }, ProgramData::new);
        register.register(LabelPositionHolder.class, new Capability.IStorage<LabelPositionHolder>() {
            @Nullable
            @Override
            public NBTBase writeNBT(Capability<LabelPositionHolder> capability, LabelPositionHolder instance, EnumFacing side) {
                NBTTagCompound nbtTagCompound = new NBTTagCompound();
                PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
                boolean encode = LabelPositionHolder.encode(instance.data, buffer);
                if(!encode){
                    return nbtTagCompound;
                }
                int readableBytes = buffer.readableBytes();
                byte[] allBytes = new byte[readableBytes];
                String encoded = Base64.getEncoder().encodeToString(buffer.readBytes(allBytes).array());
                nbtTagCompound.setString("labels", encoded);
                return nbtTagCompound;
            }

            @Override
            public void readNBT(Capability<LabelPositionHolder> capability, LabelPositionHolder instance, EnumFacing side, NBTBase nbt) {
                String string = ((NBTTagCompound) nbt).getString("labels");
                if(string.isEmpty()){
                    instance.data=LabelPositionHolder.empty();
                    return;
                }
                byte[] decode = Base64.getDecoder().decode(string);
                instance.data = LabelPositionHolder.decode(new PacketBuffer(Unpooled.wrappedBuffer(decode)));
            }
        }, LabelPositionHolder::new);

        register.register(Errors.class, new Capability.IStorage<Errors>() {
            @Nullable
            @Override
            public NBTBase writeNBT(Capability<Errors> capability, Errors instance, EnumFacing side) {
                NBTTagCompound nbtTagCompound = new NBTTagCompound();
                StringBuilder builder = new StringBuilder();
                for (ITextComponent iTextComponent : instance.data) {
                    builder.append(ITextComponent.Serializer.componentToJson(iTextComponent));
                    builder.append("-----------------");
                }
                nbtTagCompound.setString("errors", new String(builder));
                return nbtTagCompound;
            }

            @Override
            public void readNBT(Capability<Errors> capability, Errors instance, EnumFacing side, NBTBase nbt) {
                NBTTagCompound nbt1 = (NBTTagCompound) nbt;
                ArrayList<TextComponentTranslation> iTextComponents = new ArrayList<>();
                String string = nbt1.getString("errors");
                String[] split = string.split("-----------------");
                for (int i = 0; i < split.length - 1; i++) {
                    iTextComponents.add(new TextComponentTranslation(ITextComponent.Serializer.jsonToComponent(split[i]).getUnformattedComponentText()));
                }
                instance.data = iTextComponents;
            }
        }, Errors::new);

        register.register(Warnings.class, new Capability.IStorage<Warnings>() {
            @Nullable
            @Override
            public NBTBase writeNBT(Capability<Warnings> capability, Warnings instance, EnumFacing side) {
                NBTTagCompound nbtTagCompound = new NBTTagCompound();
                StringBuilder builder = new StringBuilder();
                for (ITextComponent iTextComponent : instance.data) {
                    builder.append(ITextComponent.Serializer.componentToJson(iTextComponent));
                    builder.append("-----------------");
                }
                nbtTagCompound.setString("warnings", new String(builder));
                return nbtTagCompound;
            }

            @Override
            public void readNBT(Capability<Warnings> capability, Warnings instance, EnumFacing side, NBTBase nbt) {
                NBTTagCompound nbt1 = (NBTTagCompound) nbt;
                ArrayList<TextComponentTranslation> iTextComponents = new ArrayList<>();
                String string = nbt1.getString("warnings");
                String[] split = string.split("-----------------");
                for (int i = 0; i < split.length - 1; i++) {
                    iTextComponents.add((TextComponentTranslation) ITextComponent.Serializer.jsonToComponent(split[i]));
                }
                instance.data = iTextComponents;
            }
        }, Warnings::new);

        register.register(ActiveLabel.class, new Capability.IStorage<ActiveLabel>() {
            @Nullable
            @Override
            public NBTBase writeNBT(Capability<ActiveLabel> capability, ActiveLabel instance, EnumFacing side) {
                NBTTagCompound nbtTagCompound = new NBTTagCompound();
                nbtTagCompound.setString("active_label", instance.data);
                return nbtTagCompound;
            }

            @Override
            public void readNBT(Capability<ActiveLabel> capability, ActiveLabel instance, EnumFacing side, NBTBase nbt) {
                NBTTagCompound nbt1 = (NBTTagCompound) nbt;
                instance.data = nbt1.getString("active_label");
            }
        }, ActiveLabel::new);

        register.register(LabelGunViewMod.class, new Capability.IStorage<LabelGunViewMod>() {
            @Nullable
            @Override
            public NBTBase writeNBT(Capability<LabelGunViewMod> capability, LabelGunViewMod instance, EnumFacing side) {
                NBTTagCompound nbtTagCompound = new NBTTagCompound();
                PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
                buf.writeEnumValue(instance.data);

                int readableBytes = buf.readableBytes();
                byte[] allBytes = new byte[readableBytes];
                nbtTagCompound.setByteArray("label_gun_view_mode", buf.readBytes(allBytes).array());
                return nbtTagCompound;
            }

            @Override
            public void readNBT(Capability<LabelGunViewMod> capability, LabelGunViewMod instance, EnumFacing side, NBTBase nbt) {
                NBTTagCompound nbt1 = (NBTTagCompound) nbt;
                byte[] labelGunViewModes = nbt1.getByteArray("label_gun_view_mode");
                PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
                buf.writeBytes(labelGunViewModes);
                instance.data = buf.readEnumValue(LabelGunItem.LabelGunViewMode.class);
            }
        }, LabelGunViewMod::new);
    }
//    public static final Supplier<DataComponentType<String>> ACTIVE_LABEL = DATA_COMPONENT_TYPES.register(
//            "active_label",
//            () -> DataComponentType
//                    .<String>builder()
//                    .persistent(Codec.STRING)
//                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
//                    .cacheEncoding()
//                    .build()
//    );
//    public static final Supplier<DataComponentType<LabelGunItem.LabelGunViewMode>> LABEL_GUN_VIEW_MODE = DATA_COMPONENT_TYPES.register(
//            "label_gun_view_mode",
//            () -> DataComponentType
//                    .<LabelGunItem.LabelGunViewMode>builder()
//                    .persistent(LabelGunItem.LabelGunViewMode.CODEC)
//                    .networkSynchronized(LabelGunItem.LabelGunViewMode.STREAM_CODEC)
//                    .cacheEncoding()
//                    .build()
//    );
//    public static final Supplier<DataComponentType<Boolean>> OVERLAY_ENABLED = DATA_COMPONENT_TYPES.register(
//            "overlay_enabled",
//            () -> DataComponentType
//                    .<Boolean>builder()
//                    .persistent(Codec.BOOL)
//                    .networkSynchronized(ByteBufCodecs.BOOL)
//                    .cacheEncoding()
//                    .build()
//    );
//    public static final Supplier<DataComponentType<List<Component>>> PROGRAM_WARNINGS = DATA_COMPONENT_TYPES.register(
//            "warnings",
//            () -> DataComponentType
//                    .<List<Component>>builder()
//                    .persistent(Codec.list(ComponentSerialization.CODEC))
//                    .networkSynchronized(ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list()))
//                    .cacheEncoding()
//                    .build()
//    );
//    public static final Supplier<DataComponentType<List<Component>>> PROGRAM_ERRORS = DATA_COMPONENT_TYPES.register(
//            "errors",
//            () -> DataComponentType
//                    .<List<Component>>builder()
//                    .persistent(Codec.list(ComponentSerialization.CODEC))
//                    .networkSynchronized(ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list()))
//                    .cacheEncoding()
//                    .build()
//    );
//    public static final Supplier<DataComponentType<LabelPositionHolder>> LABEL_POSITION_HOLDER = DATA_COMPONENT_TYPES.register(
//            "labels",
//            () -> DataComponentType
//                    .<LabelPositionHolder>builder()
//                    .persistent(LabelPositionHolder.CODEC.codec())
//                    .networkSynchronized(LabelPositionHolder.STREAM_CODEC)
//                    .cacheEncoding()
//                    .build()
//    );
//
//    public static final Supplier<DataComponentType<ItemStackBox>> FORM_REFERENCE = DATA_COMPONENT_TYPES.register(
//            "form_reference",
//            () -> DataComponentType
//                    .<ItemStackBox>builder()
//                    .persistent(ItemStackBox.CODEC)
//                    .networkSynchronized(ItemStackBox.STREAM_CODEC)
//                    .cacheEncoding()
//                    .build()
//    );
//
//    public static final Supplier<DataComponentType<CompressedBlockPosSet>> CABLE_POSITIONS = DATA_COMPONENT_TYPES.register(
//            "cable_positions",
//            () -> DataComponentType
//                    .<CompressedBlockPosSet>builder()
//                    .networkSynchronized(CompressedBlockPosSet.STREAM_CODEC)
//                    .persistent(CompressedBlockPosSet.CODEC)
//                    .cacheEncoding()
//                    .build()
//    );
//    public static final Supplier<DataComponentType<CompressedBlockPosSet>> CAPABILITY_POSITIONS = DATA_COMPONENT_TYPES.register(
//            "capability_positions",
//            () -> DataComponentType
//                    .<CompressedBlockPosSet>builder()
//                    .networkSynchronized(CompressedBlockPosSet.STREAM_CODEC)
//                    .persistent(CompressedBlockPosSet.CODEC)
//                    .cacheEncoding()
//                    .build()
//    );

}
