package ca.teamdman.sfm.common.config;

import ca.teamdman.sfm.SFM;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// 移除@EventBusSubscriber注解
public class SFMServerConfig {
    public static boolean disableProgramExecution = false;
    public static boolean logResourceLossToConsole = true;
    public static int timerTriggerMinimumIntervalInTicks = 20;
    public static int timerTriggerMinimumIntervalInTicksWhenOnlyForgeEnergyIO = 1;
    public static int maxIfStatementsInTriggerBeforeSimulationIsntAllowed = 10;
    public static List<String> disallowedResourceTypesForTransfer = new ArrayList<>();
    public static LevelsToShards levelsToShards = LevelsToShards.JustOne;
    
    /**
     * This is used by managers to detect when the config has changed.
     * When the manager cached  differs from this, the manager will rebuild its program.
     */
    private static int revision = 0;

    // 配置文件名称
    private static final String CONFIG_FILE = "sfm.cfg";
    private static Configuration config;

    public static void init(File configFile) {
        if (config == null) {
            config = new Configuration(new File(configFile, CONFIG_FILE));
            loadConfig();
        }
    }

    private static void loadConfig() {
        // 从配置文件加载配置
        disableProgramExecution = config.getBoolean(
                "disableProgramExecution",
                Configuration.CATEGORY_GENERAL,
                false,
                "Prevents factory managers from compiling and running code (for emergencies)"
        );

        logResourceLossToConsole = config.getBoolean(
                "logResourceLossToConsole",
                Configuration.CATEGORY_GENERAL,
                true,
                "Log resource loss to console"
        );

        timerTriggerMinimumIntervalInTicks = config.getInt(
                "timerTriggerMinimumIntervalInTicks",
                Configuration.CATEGORY_GENERAL,
                20,
                1,
                Integer.MAX_VALUE,
                ""
        );

        timerTriggerMinimumIntervalInTicksWhenOnlyForgeEnergyIO = config.getInt(
                "timerTriggerMinimumIntervalInTicksWhenOnlyForgeEnergyIOStatementsPresent",
                Configuration.CATEGORY_GENERAL,
                1,
                1,
                Integer.MAX_VALUE,
                ""
        );

        maxIfStatementsInTriggerBeforeSimulationIsntAllowed = config.getInt(
                "maxIfStatementsInTriggerBeforeSimulationIsntAllowed",
                Configuration.CATEGORY_GENERAL,
                10,
                0,
                Integer.MAX_VALUE,
                "The number of scenarios to check is 2^n where n is the number of if statements in a trigger"
        );

        String[] defaultList = new String[0];
        String[] disallowedTypes = config.getStringList(
                "disallowedResourceTypesForTransfer",
                Configuration.CATEGORY_GENERAL,
                defaultList,
                "What resource types should SFM not be allowed to move"
        );
        disallowedResourceTypesForTransfer = Arrays.asList(disallowedTypes);

        levelsToShards = LevelsToShards.valueOf(config.getString(
                "levelsToShards",
                Configuration.CATEGORY_GENERAL,
                LevelsToShards.JustOne.name(),
                "How to convert Enchanted Books to Experience Shards\n" +
                "JustOne = always produces 1 shard regardless of enchantments\n" +
                "EachOne = produces 1 shard per enchantment on the book.\n" +
                "SumLevels = produces a number of shards equal to the sum of the enchantments' levels\n" +
                "SumLevelsScaledExponentially = produces a number of shards equal to the sum of 2 to the power of each enchantment's level"
        ));

        if (config.hasChanged()) {
            config.save();
            revision++;
            SFM.LOGGER.info("SFM config loaded, now on revision {}", revision);
        }
    }

    public static int getRevision() {
        return revision;
    }

    // 添加静态初始化块来注册事件处理器
    static {
        MinecraftForge.EVENT_BUS.register(new Object() {
            @SubscribeEvent
            public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
                if (event.getModID().equals(SFM.MOD_ID)) {
                    loadConfig();
                }
            }
        });
    }

    public enum LevelsToShards {
        JustOne,
        EachOne,
        SumLevels,
        SumLevelsScaledExponentially,
    }
}
