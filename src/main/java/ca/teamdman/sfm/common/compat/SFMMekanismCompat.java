//package ca.teamdman.sfm.common.compat;
//
//import ca.teamdman.sfm.SFM;
//import ca.teamdman.sfm.common.localization.LocalizationKeys;
//import ca.teamdman.sfm.common.program.linting.IProgramLinter;
//import ca.teamdman.sfm.common.program.linting.MekanismSideConfigProgramLinter;
//import ca.teamdman.sfm.common.registry.SFMResourceTypes;
//import ca.teamdman.sfm.common.resourcetype.ChemicalResourceType;
//import ca.teamdman.sfm.common.resourcetype.ResourceType;
//import ca.teamdman.sfml.ast.DirectionQualifier;
//import ca.teamdman.sfml.ast.IOStatement;
//import ca.teamdman.sfml.ast.ResourceIdentifier;
//import mekanism.api.RelativeSide;
//import mekanism.api.transmitters.TransmissionType;
//import mekanism.common.base.ISideConfiguration;
//import mekanism.common.tile.component.TileComponentConfig;
//import mekanism.common.tile.component.config.ConfigInfo;
//import mekanism.common.tile.component.config.DataType;
//import mekanism.common.util.UnitDisplayUtils;
//import net.minecraft.util.EnumFacing;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.EnumSet;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//import java.util.function.Predicate;
//import java.util.stream.Collectors;
//
//public class SFMMekanismCompat {
//    public static @Nullable ResourceType<?, ?, ?> getResourceType(TransmissionType trans) {
//         switch (trans) {
//             case ITEM :return SFMResourceTypes.ITEM.get();
//            case FLUID :return SFMResourceTypes.FLUID.get();
////            case GAS -> {
////                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(SFM.MOD_ID, "gas");
////                yield SFMResourceTypes.registry().get(id);
////            }
////            case INFUSION -> {
////                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(SFM.MOD_ID, "infusion");
////                yield SFMResourceTypes.registry().get(id);
////            }
////            case PIGMENT -> {
////                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(SFM.MOD_ID, "pigment");
////                yield SFMResourceTypes.registry().get(id);
////            }
////            case SLURRY -> {
////                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(SFM.MOD_ID, "slurry");
////                yield SFMResourceTypes.registry().get(id);
////            }
//            case ENERGY :return SFMResourceTypes.FORGE_ENERGY.get();
//            default :return null;
//        }
//    }
//
//    public static EnumSet<TransmissionType> getReferencedTransmissionTypes(IOStatement statement) {
//        EnumSet<TransmissionType> transmissionTypes = EnumSet.noneOf(TransmissionType.class);
//        Set<? extends ResourceType<?, ?, ?>> referencedResourceTypes = statement
//                .getReferencedIOResourceIds()
//                .map(ResourceIdentifier::getResourceType)
//                .collect(Collectors.toSet());
//        for (TransmissionType transmissionType : TransmissionType.values()) {
//            if (referencedResourceTypes.contains(SFMMekanismCompat.getResourceType(transmissionType))) {
//                transmissionTypes.add(transmissionType);
//            }
//        }
//        return transmissionTypes;
//    }
//
//    public static long createForgeEnergy(long amount) {
//        return (long)2.5*amount;
////        return UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertFrom(amount);
//    }
//
//    public static Set<EnumFacing> getSides(ConfigInfo config, ISideConfiguration facing, Predicate<DataType> condition) {
//        Set<EnumFacing> rtn = EnumSet.noneOf(EnumFacing.class);
//        for (Map.Entry<RelativeSide, DataType> entry : config.getSideConfig()) {
//            if (condition.test(entry.getValue())) {
//                rtn.add(entry.getKey().getDirection(facing.getOrientation()));
//            }
//        }
//        return rtn;
//    }
//
//    public static String gatherInspectionResults(BlockEntity blockEntity) {
//        if (!(blockEntity instanceof ISideConfiguration sideConfiguration)) {
//            return "";
//        }
//        StringBuilder sb = new StringBuilder();
//        sb.append("-- Mekanism stuff\n");
//        TileComponentConfig config = sideConfiguration.getConfig();
//        for (TransmissionType type : TransmissionType.values()) {
//             resourceType = getResourceType(type);
//            if (resourceType == null) {
//                continue;
//            }
//
//             maybeResourceTypeKe = SFMResourceTypes.registry().getResourceKey(resourceType);
//            if (maybeResourceTypeKe.isEmpty()) {
//                continue;
//            }
//             resourceTypeKey = maybeResourceTypeKe.get();
//
//            ConfigInfo info = config.getConfig(type);
//            if (info == null) {
//                continue;
//            }
//
//            Set<EnumFacing> outputSides = getSides(info, sideConfiguration, DataType::canOutput);
//            if (!outputSides.isEmpty()) {
//                sb
//                        .append("-- ")
//                        .append(LocalizationKeys.CONTAINER_INSPECTOR_MEKANISM_MACHINE_OUTPUTS.getStub())
//                        .append("\n");
//                sb.append("INPUT ").append(resourceTypeKey.location()).append(":: FROM target ");
//                sb.append(outputSides
//                                  .stream()
//                                  .map(DirectionQualifier::directionToString)
//                                  .collect(Collectors.joining(", ")));
//                sb.append(" SIDE\n");
//            }
//
//            Set<EnumFacing> inputSides = new HashSet<>();
//            for (RelativeSide side : RelativeSide.values()) {
//                DataType dataType = info.getDataType(side);
//                if (dataType == DataType.INPUT
//                    || dataType == DataType.INPUT_1
//                    || dataType == DataType.INPUT_2
//                    || dataType == DataType.INPUT_OUTPUT) {
//                    inputSides.add(side.getDirection(sideConfiguration.getDirection()));
//                }
//            }
//            if (!inputSides.isEmpty()) {
//                sb
//                        .append("-- ")
//                        .append(LocalizationKeys.CONTAINER_INSPECTOR_MEKANISM_MACHINE_INPUTS.getStub())
//                        .append("\n");
//                sb.append("OUTPUT ").append(resourceTypeKey.location()).append(":: TO target ");
//                sb.append(inputSides
//                                  .stream()
//                                  .map(DirectionQualifier::directionToString)
//                                  .collect(Collectors.joining(", ")));
//                sb.append(" SIDE\n");
//            }
//        }
//        return sb.toString();
//    }
//
//    public static void registerResourceTypes(DeferredRegister<ResourceType<?, ?, ?>> types) {
//        types.register("chemical", ChemicalResourceType::new);
//        types.register("gas", ChemicalResourceType::new
//        );
//        types.register(
//                "infusion",
//                ChemicalResourceType::new
//        );
//
//        types.register(
//                "pigment",
//                ChemicalResourceType::new
//        );
//        types.register(
//                "slurry",
//                ChemicalResourceType::new
//        );
////        types.register(
////                "mekanism_energy",
////                MekanismEnergyResourceType::new
////        );
//    }
//
//    public static void registerProgramLinters(DeferredRegister<IProgramLinter> types) {
//        types.register(
//                "mekanism",
//                MekanismSideConfigProgramLinter::new
//        );
//    }
//
//    public static void configureExclusiveIO(
//            ISideConfiguration mekanismBlockEntity,
//            TransmissionType transmissionType,
//            RelativeSide relativeSide,
//            DataType dataType
//    ) {
//        TileComponentConfig config = mekanismBlockEntity.getConfig();
//        for (TransmissionType value : TransmissionType.values()) {
//            ConfigInfo info = config.getConfig(value);
//            if (info == null) continue;
//            for (RelativeSide side : RelativeSide.values()) {
//                info.setDataType(
//                        value == transmissionType && side == relativeSide ? dataType : DataType.NONE,
//                        side
//                );
//            }
//            config.sideChanged(value, relativeSide);
//        }
//    }
//}
