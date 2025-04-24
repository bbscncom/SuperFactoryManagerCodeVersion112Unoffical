package ca.teamdman.sfm.common.linting;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.common.blockentity.ManagerBlockEntity;
import ca.teamdman.sfm.common.cablenetwork.CableNetworkManager;
import ca.teamdman.sfm.common.item.DiskItem;
import ca.teamdman.sfm.common.program.LabelPositionHolder;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.registry.SFMBlockCapabilities;
import ca.teamdman.sfm.common.resourcetype.ResourceType;
import ca.teamdman.sfml.ast.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Optional;

import static ca.teamdman.sfm.common.localization.LocalizationKeys.*;
import static ca.teamdman.sfml.ast.RoundRobin.Behaviour.BY_BLOCK;
import static ca.teamdman.sfml.ast.RoundRobin.Behaviour.BY_LABEL;

public class ProgramLinter {
    @SuppressWarnings("ConstantValue")
    public static ArrayList<TextComponentTranslation> gatherWarnings(
            Program program,
            LabelPositionHolder labelPositionHolder,
            @Nullable ManagerBlockEntity manager
    ) {
        ArrayList<TextComponentTranslation> warnings = new ArrayList<>();
        World world = manager != null ? manager.getWorld() : null;

        // label smells
        int before = warnings.size();
        addWarningsForLabelsInProgramButNotInHolder(program, labelPositionHolder, warnings);
        addWarningsForLabelsInHolderButNotInProgram(program, labelPositionHolder, warnings);
        if (world != null) {
            addWarningsForLabelsUsedInWorldButNotConnectedByCables(manager, labelPositionHolder, warnings, world);
        }
        int after = warnings.size();
        if (before != after) {
            // add reminder to push labels
            warnings.add(PROGRAM_REMINDER_PUSH_LABELS.get());
        }

        // logical flow smells
        addWarningsForUsingIOWithoutCorrespondingOppositeIO(program, labelPositionHolder, warnings);

        // resource smells
        addWarningsForResourcesReferencedButNotFoundInRegistry(program, warnings);

        // simple io statement smells
        program
                .getDescendantStatements()
                .filter(IOStatement.class::isInstance)
                .map(IOStatement.class::cast)
                .forEach(statement -> {
                    addWarningsForSmellyRoundRobinUsage(warnings, statement);
                    addWarningsForUsingEachWithoutAPattern(warnings, statement);
                    if (world != null) {
                        addWarningsForSmellyMekanismAccess(statement, labelPositionHolder, statement, world, warnings);
                    }

                });

        return warnings;
    }

    public static void fixWarnings(
            ManagerBlockEntity manager,
            ItemStack disk,
            Program program
    ) {
        fixWarningsByRemovingBadLabelsFromDisk(manager, disk, program);
        LabelPositionHolder labelPositionHolder = LabelPositionHolder.from(disk);
        World world = manager.getWorld();
        if (world != null) {
            program
                    .getDescendantStatements()
                    .filter(IOStatement.class::isInstance)
                    .map(IOStatement.class::cast)
                    .forEach(statement -> fixWarningsByModifyingMekanismAccess(statement, labelPositionHolder, world));
        }
        manager.rebuildProgramAndUpdateDisk();
    }

    private static void fixWarningsByRemovingBadLabelsFromDisk(
            ManagerBlockEntity manager,
            ItemStack disk,
            Program program
    ) {
        LabelPositionHolder labels = LabelPositionHolder.from(disk);
        // remove labels not defined in code
        labels.removeIf(label -> !program.referencedLabels.contains(label));

        // remove labels not connected via cables
        CableNetworkManager
                .getOrRegisterNetworkFromManagerPosition(manager)
                .ifPresent(network -> labels.removeIf((label, pos) -> !network.isAdjacentToCable(pos)));

        // remove labels with no viable capability provider
        World world = manager.getWorld();
        if (world != null) {
            labels.removeIf((label, pos) -> !SFMBlockCapabilities.hasAnyCapabilityAnyDirection(world, pos));
        }
        // save new labels
        labels.save(disk);

        // update warnings
        DiskItem.setWarnings(disk, gatherWarnings(program, labels, manager));
    }

    private static void fixWarningsByModifyingMekanismAccess(
            IOStatement statement,
            LabelPositionHolder labelPositionHolder,
            World world
    ) {
//        if (!SFMModCompat.isMekanismLoaded()) return;
//        DirectionQualifier directions = statement.labelAccess().directions();
//        Stream<Pair<Label, BlockPos>> mekanismBlocks = statement
//                .labelAccess()
//                .getLabelledPositions(labelPositionHolder)
//                .stream()
//                .filter(pair -> world.isBlockLoaded(pair.getSecond()))
//                .filter(pair -> SFMModCompat.isMekanismBlock(world, pair.getSecond()));
//
//        // add warning if interacting with mekanism but the mekanism side config is not ALLOW
//        EnumSet<TransmissionType> referencedTransmissionTypes = SFMMekanismCompat
//                .getReferencedTransmissionTypes(statement);
//        Predicate<DataType> dataTypePredicate;
//        DataType fixed;
//        if (statement instanceof InputStatement) {
//            dataTypePredicate = DataType::canOutput;
//            fixed = DataType.OUTPUT; // to input from it, it must be set to output
//        } else if (statement instanceof OutputStatement) {
//            dataTypePredicate = dataType -> dataType == DataType.INPUT
//                                            || dataType == DataType.INPUT_OUTPUT
//                                            || dataType == DataType.INPUT_1
//                                            || dataType == DataType.INPUT_2;
//            fixed = DataType.INPUT; // to output from it, it must be set to input
//        } else {
//            throw new IllegalStateException("Unexpected value: " + statement);
//        }
//        mekanismBlocks.forEach(pair -> {
//            BlockPos blockPos = pair.getSecond();
//            if (world.getBlockEntity(blockPos) instanceof ISideConfiguration mekBlockEntity) {
//                TileComponentConfig mekBlockEntityConfig = mekBlockEntity.getConfig();
//                for (TransmissionType transmissionType : referencedTransmissionTypes) {
//                    boolean anySuccess = false;
//                    ConfigInfo transmissionConfig = mekBlockEntityConfig.getConfig(transmissionType);
//                    if (transmissionConfig != null) {
//                        Set<EnumFacing> activeSides = getSideConfig(
//                                transmissionConfig,
//                                dataTypePredicate,
//                                mekBlockEntity.getDirection()
//                        );
//                        for (EnumFacing direction : directions) {
//                            if (activeSides.contains(direction)) {
//                                anySuccess = true;
//                                break;
//                            }
//                        }
//                        if (!anySuccess) {
//                            // we want to enable the side for the transmission type
//                            // pick the first direction in the statement
//                            EnumFacing statementSide = directions.iterator().next();
//                            if (statementSide != null) {
//                                RelativeSide relativeSide = RelativeSide.fromDirections(
//                                        mekBlockEntity.getDirection(),
//                                        statementSide
//                                );
//                                transmissionConfig.setDataType(
//                                        fixed,
//                                        relativeSide
//                                );
//                                mekBlockEntityConfig.sideChanged(transmissionType, relativeSide);
//                            }
//                        }
//                    }
//                }
//            }
//        });
    }

    private static void addWarningsForSmellyMekanismAccess(
            IOStatement ioStatement,
            LabelPositionHolder labelPositionHolder,
            IOStatement statement,
            World world,
            ArrayList<TextComponentTranslation> warnings
    ) {
//        if (!SFMModCompat.isMekanismLoaded()) return;
//        DirectionQualifier directions = statement.labelAccess().directions();
//        Stream<Pair<Label, BlockPos>> mekanismBlocks = statement
//                .labelAccess()
//                .getLabelledPositions(labelPositionHolder)
//                .stream()
//                .filter(pair -> world.isBlockLoaded(pair.getSecond()))
//                .filter(pair -> SFMModCompat.isMekanismBlock(world, pair.getSecond()));
//        if (directions.equals(DirectionQualifier.NULL_DIRECTION)) {
//            // add warning if interacting with mekanism without specifying a side
//            // are any of the blocks mekanism?
//            mekanismBlocks
//                    .forEach(pair -> warnings.add(PROGRAM_WARNING_MEKANISM_USED_WITHOUT_DIRECTION.get(
//                            pair.getFirst(),
//                            statement.toStringPretty()
//                    )));
//        } else {
//            // add warning if interacting with mekanism but the mekanism side config is not ALLOW
//            EnumSet<TransmissionType> referencedTransmissionTypes = SFMMekanismCompat
//                    .getReferencedTransmissionTypes(statement);
//            Predicate<DataType> dataTypePredicate;
//            if (ioStatement instanceof InputStatement) {
//                dataTypePredicate = DataType::canOutput;
//            } else if (ioStatement instanceof OutputStatement) {
//                dataTypePredicate = dataType -> dataType == DataType.INPUT
//                                                || dataType == DataType.INPUT_OUTPUT
//                                                || dataType == DataType.INPUT_1
//                                                || dataType == DataType.INPUT_2;
//            } else {
//                throw new IllegalStateException("Unexpected value: " + ioStatement);
//            }
//            mekanismBlocks.forEach(pair -> {
//                BlockPos blockPos = pair.getSecond();
//                if (world.getBlockEntity(blockPos) instanceof ISideConfiguration mekBlockEntity) {
//                    TileComponentConfig config = mekBlockEntity.getConfig();
//                    for (TransmissionType transmissionType : referencedTransmissionTypes) {
//                        boolean anySuccess = false;
//                        ConfigInfo transmissionConfig = config.getConfig(transmissionType);
//                        if (transmissionConfig != null) {
//                            Set<EnumFacing> activeSides = getSideConfig(
//                                    transmissionConfig,
//                                    dataTypePredicate,
//                                    mekBlockEntity.getDirection()
//                            );
//                            for (EnumFacing direction : directions) {
//                                if (activeSides.contains(direction)) {
//                                    anySuccess = true;
//                                    break;
//                                }
//                            }
//                        }
//                        if (!anySuccess) {
//                            warnings.add(PROGRAM_WARNING_MEKANISM_BAD_SIDE_CONFIG.get(
//                                    blockPos,
//                                    pair.getFirst(),
//                                    statement.toStringPretty()
//                            ));
//                        }
//                    }
//                }
//            });
//        }
    }

//    private static Set<EnumFacing> getSideConfig(
//            ConfigInfo transmissionConfig,
//            Predicate<DataType> dataTypePredicate,
//            EnumFacing blockFacing
//    ) {
//        Set<Map.Entry<RelativeSide, DataType>> sideConfig = transmissionConfig.getSideConfig();
//        Set<RelativeSide> found = new HashSet<>();
//        for ( x : sideConfig) {
//            if (dataTypePredicate.test(x.getValue())) {
//                found.add(x.getKey());
//            }
//        }
//        return found.stream().map(x -> x.getDirection(blockFacing)).collect(Collectors.toSet());
//    }

    private static void addWarningsForUsingIOWithoutCorrespondingOppositeIO(
            Program program,
            LabelPositionHolder labelPositionHolder,
            ArrayList<TextComponentTranslation> warnings
    ) {
        program.tick(ProgramContext.createSimulationContext(
                program,
                labelPositionHolder,
                0,
                new GatherWarningsProgramBehaviour((text)-> warnings.addAll(text))
        ));
    }


    private static void addWarningsForUsingEachWithoutAPattern(
            ArrayList<TextComponentTranslation> warnings,
            IOStatement statement
    ) {
        boolean smells = statement
                .resourceLimits()
                .resourceLimitList()
                .stream()
                .anyMatch(rl -> rl.limit().quantity().idExpansionBehaviour()
                                == ResourceQuantity.IdExpansionBehaviour.EXPAND && !rl
                        .resourceIds()
                        .couldMatchMoreThanOne());
        if (smells) {
            warnings.add(PROGRAM_WARNING_RESOURCE_EACH_WITHOUT_PATTERN.get(statement.toStringPretty()));
        }
    }

    private static void addWarningsForSmellyRoundRobinUsage(
            ArrayList<TextComponentTranslation> warnings,
            IOStatement statement
    ) {
        RoundRobin roundRobin = statement.labelAccess().roundRobin();
        if (roundRobin.getBehaviour() == BY_BLOCK && statement.each()) {
            warnings.add(PROGRAM_WARNING_ROUND_ROBIN_SMELLY_EACH.get(statement.toStringPretty()));
        } else if (roundRobin.getBehaviour() == BY_LABEL
                   && statement.labelAccess().labels().size() == 1) {
            warnings.add(PROGRAM_WARNING_ROUND_ROBIN_SMELLY_COUNT.get(statement.toStringPretty()));
        }
    }

    private static void addWarningsForResourcesReferencedButNotFoundInRegistry(
            Program program,
            ArrayList<TextComponentTranslation> warnings
    ) {
        for (ResourceIdentifier<?, ?, ?> resource : program.referencedResources) {
            // skip regex resources
            Optional<ResourceLocation> loc = resource.getLocation();
            if (!loc.isPresent()) continue;

            // make sure resource type is registered
            @Nullable ResourceType<?, ?, ?> type = resource.getResourceType();
            if (type == null) {
                SFM.LOGGER.error(
                        "Resource type not found for resource: {}, should have been validated at program compile",
                        resource
                );
                continue;
            }

            // make sure resource exists in the registry
            if (!type.registryKeyExists(loc.get())) {
                warnings.add(PROGRAM_WARNING_UNKNOWN_RESOURCE_ID.get(resource));
            }
        }
    }

    private static void addWarningsForLabelsUsedInWorldButNotConnectedByCables(
            @NotNull ManagerBlockEntity manager,
            LabelPositionHolder labels,
            ArrayList<TextComponentTranslation> warnings,
            World world
    ) {
        CableNetworkManager
                .getOrRegisterNetworkFromManagerPosition(manager)
                .ifPresent(network -> labels.forEach((label, pos) -> {
                    boolean adjacent = network.isAdjacentToCable(pos);
                    if (!adjacent) {
                        warnings.add(PROGRAM_WARNING_DISCONNECTED_LABEL.get(
                                label,
                                String.format(
                                        "[%d,%d,%d]",
                                        pos.getX(),
                                        pos.getY(),
                                        pos.getZ()
                                )
                        ));
                    }
                    boolean viable = SFMBlockCapabilities.hasAnyCapabilityAnyDirection(world, pos);
                    if (!viable && adjacent) {
                        warnings.add(PROGRAM_WARNING_CONNECTED_BUT_NOT_VIABLE_LABEL.get(
                                label,
                                String.format(
                                        "[%d,%d,%d]",
                                        pos.getX(),
                                        pos.getY(),
                                        pos.getZ()
                                )
                        ));
                    }
                }));
    }

    private static void addWarningsForLabelsInHolderButNotInProgram(
            Program program,
            LabelPositionHolder labels,
            ArrayList<TextComponentTranslation> warnings
    ) {
        labels.Labels()
                .keySet()
                .stream()
                .filter(x -> !program.referencedLabels.contains(x))
                .forEach(label -> warnings.add(PROGRAM_WARNING_UNDEFINED_LABEL.get(label)));
    }

    private static void addWarningsForLabelsInProgramButNotInHolder(
            Program program,
            LabelPositionHolder labels,
            ArrayList<TextComponentTranslation> warnings
    ) {
        for (String label : program.referencedLabels) {
            boolean isUsed = !labels.getPositions(label).isEmpty();
            if (!isUsed) {
                warnings.add(PROGRAM_WARNING_UNUSED_LABEL.get(label));
            }
        }
    }
}
