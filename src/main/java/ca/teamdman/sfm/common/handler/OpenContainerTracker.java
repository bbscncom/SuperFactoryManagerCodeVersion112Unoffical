package ca.teamdman.sfm.common.handler;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.common.container.ManagerContainerMenu;
import ca.teamdman.sfm.common.util.NotStored;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Stream;

// TODO: consider replacing with ContainerOpenersCounter, see BarrelBlockEntity
@Mod.EventBusSubscriber(modid = SFM.MOD_ID)
public class OpenContainerTracker {
    private static final Map<BlockPos, Map<EntityPlayerMP, ManagerContainerMenu>> OPEN_CONTAINERS = new WeakHashMap<>();

    public static Stream<Map.Entry<EntityPlayerMP, ManagerContainerMenu>> getOpenManagerMenus(@NotStored BlockPos pos) {
        if (OPEN_CONTAINERS.containsKey(pos)) {
            return OPEN_CONTAINERS.get(pos).entrySet().stream();
        } else {
            return Stream.empty();
        }
    }

    @SubscribeEvent
    public static void onOpenContainer(PlayerContainerEvent.Open event) {
        if (event.getEntity() instanceof EntityPlayerMP
            && event.getContainer() instanceof ManagerContainerMenu) {
            ManagerContainerMenu container =  (ManagerContainerMenu)event.getContainer();
            OPEN_CONTAINERS.computeIfAbsent(container.MANAGER_POSITION, k -> new HashMap<>()).put((EntityPlayerMP) event.getEntity(), container);
        }
    }

    @SubscribeEvent
    public static void onCloseContainer(PlayerContainerEvent.Close event) {
        if (event.getEntity() instanceof EntityPlayerMP
            && event.getContainer() instanceof ManagerContainerMenu) {
            EntityPlayerMP serverPlayer = (EntityPlayerMP) event.getEntity();
            ManagerContainerMenu mcm =  (ManagerContainerMenu)event.getContainer();

            if (OPEN_CONTAINERS.containsKey(mcm.MANAGER_POSITION)) {
                OPEN_CONTAINERS.get(mcm.MANAGER_POSITION).remove( serverPlayer);
                if (OPEN_CONTAINERS.get(mcm.MANAGER_POSITION).isEmpty()) {
                    OPEN_CONTAINERS.remove(mcm.MANAGER_POSITION);
                }
            }
        }
    }
}
