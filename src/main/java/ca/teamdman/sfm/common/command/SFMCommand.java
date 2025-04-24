package ca.teamdman.sfm.common.command;

//@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, modid = SFM.MOD_ID)
public class SFMCommand {
//    @SubscribeEvent
//    public static void onRegisterCommand(final RegisterCommandsEvent event) {
//         command = Commands.literal("sfm");
//        command.then(Commands.literal("bust_cable_network_cache")
//                             .requires(source -> source.hasPermission(Commands.LEVEL_ALL))
//                             .executes(ctx -> {
//                                 SFM.LOGGER.info(
//                                         "Busting cable networks - slash command used by {}",
//                                         ctx.getSource().getTextName()
//                                 );
//                                 CableNetworkManager.clear();
//                                 return SINGLE_SUCCESS;
//                             }));
//        command.then(Commands.literal("bust_water_network_cache")
//                             .requires(source -> source.hasPermission(Commands.LEVEL_ALL))
//                             .executes(ctx -> {
//                                 SFM.LOGGER.info(
//                                         "Busting water networks - slash command used by {}",
//                                         ctx.getSource().getTextName()
//                                 );
//                                 WaterNetworkManager.clear();
//                                 return SINGLE_SUCCESS;
//                             }));
//        command.then(Commands.literal("show_bad_cable_cache_entries")
//                             .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
//                             .then(Commands.argument("block", BlockStateArgument.block(event.getBuildContext()))
//                                           .executes(ctx -> {
//                                               ServerLevel level = ctx.getSource().getLevel();
//                                               CableNetworkManager.getBadCableCachePositions(level).forEach(pos -> {
//                                                   BlockInput block = BlockStateArgument
//                                                           .getBlock(
//                                                                   ctx,
//                                                                   "block"
//                                                           );
//                                                   block.place(
//                                                           level,
//                                                           pos,
//                                                           Block.UPDATE_ALL
//                                                   );
//                                               });
//                                               return SINGLE_SUCCESS;
//                                           })));
//        command.then(
//                Commands.literal("config")
//                        .then(Commands.literal("show")
//                                      .requires(source -> source.hasPermission(Commands.LEVEL_ALL))
//                                      .then(Commands
//                                                    .argument(
//                                                            "variant",
//                                                            EnumArgument.enumArgument(ConfigCommandVariantInput.class)
//                                                    )
//                                                    .executes(ctx -> new ConfigCommand(
//                                                            ConfigCommandBehaviourInput.SHOW,
//                                                            ctx.getArgument(
//                                                                    "variant",
//                                                                    ConfigCommandVariantInput.class
//                                                            )
//                                                    ).run(ctx))
//                                      )
//                        )
//                        .then(Commands.literal("edit")
//                                      .then(
//                                              Commands.literal(ConfigCommandVariantInput.SERVER.name())
//                                                      .requires(source -> source.hasPermission(Commands.LEVEL_OWNERS))
//                                                      .executes(new ConfigCommand(
//                                                              ConfigCommandBehaviourInput.EDIT,
//                                                              ConfigCommandVariantInput.SERVER
//                                                      ))
//                                      )
//                                      .then(
//                                              Commands.literal(ConfigCommandVariantInput.CLIENT.name())
//                                                      .requires(source -> source.hasPermission(Commands.LEVEL_ALL))
//                                                      .executes(new ConfigCommand(
//                                                              ConfigCommandBehaviourInput.EDIT,
//                                                              ConfigCommandVariantInput.CLIENT
//                                                      ))
//                                      )
//                        )
//        );
//        command.then(Commands.literal("changelog")
//                             .requires(source -> source.hasPermission(Commands.LEVEL_ALL))
//                             .executes(ctx -> {
//                                 ServerPlayer player = ctx.getSource().getPlayer();
//                                 if (player != null) {
//                                     // I tried making this a client command by registering in the client command event
//                                     // but what happened was that when the command is sent in the chat
//                                     // the mc logic is to set the screen to null after the command executes to close the chat
//                                     // which closes the changelog gui
//                                     // so doing it this way will keep the screen open lol
//                                     SFMPackets.sendToPlayer(
//                                             player,
//                                             new ClientboundShowChangelogPacket()
//                                     );
//                                 }
//                                 return SINGLE_SUCCESS;
//                             }));
//
//        if (FMLEnvironment.dist.isClient()) {
//            command.then(Commands.literal("export_info")
//                                 .requires(source -> source.hasPermission(Commands.LEVEL_ALL))
//                                 .then(Commands.argument("includeHidden", BoolArgumentType.bool())
//                                               .executes(ctx -> {
//                                                   boolean includeHidden = BoolArgumentType.getBool(
//                                                           ctx,
//                                                           "includeHidden"
//                                                   );
//                                                   SFM.LOGGER.info(
//                                                           "Exporting info, includeHidden={} - slash command used by {}",
//                                                           includeHidden,
//                                                           ctx.getSource().getTextName()
//                                                   );
//                                                   assert Minecraft.getInstance().player != null;
//                                                   new Thread(() -> {
//                                                       try {
//                                                            start = System.currentTimeMillis();
//                                                           Minecraft.getInstance().player.sendSystemMessage(
//                                                                   Component.literal("Beginning item export")
//                                                           );
//                                                           ClientExportHelper.dumpItems(ctx.getSource().getPlayer());
//                                                           Minecraft.getInstance().player.sendSystemMessage(
//                                                                   Component.literal("Beginning JEI export")
//                                                           );
////                                                           ClientExportHelper.dumpJei(
////                                                                   ctx.getSource().getPlayer(),
////                                                                   includeHidden
////                                                           );
//                                                            end = System.currentTimeMillis();
//                                                           Minecraft.getInstance().player.sendSystemMessage(
//                                                                   Component
//                                                                           .literal("Exported data in "
//                                                                                    + (end - start)
//                                                                                    + "ms")
//                                                                           .withStyle(ChatFormatting.GREEN)
//                                                           );
//                                                       } catch (Exception e) {
//                                                           SFM.LOGGER.error("Failed to export item data", e);
//                                                       }
//                                                   }).start();
//                                                   return SINGLE_SUCCESS;
//                                               })));
//        }
//        event.getDispatcher().register(command);
//    }

}
