package ca.teamdman.sfm.common.facade;

import ca.teamdman.sfm.common.blockentity.IFacadeTileEntity;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Set;

public class FacadePlanAnalysisResult {
    public final Map<FacadeData, Integer> facadeDataToCount;
    public final Map<Block, Integer> unfacadedCount;
    public final Set<BlockPos> positions;

    public FacadePlanAnalysisResult(
            Map<FacadeData, Integer> facadeDataToCount,
            Map<Block, Integer> unfacadedCount,
            Set<BlockPos> positions
    ) {
        this.facadeDataToCount = facadeDataToCount;
        this.unfacadedCount = unfacadedCount;
        this.positions = positions;
    }

    public static FacadePlanAnalysisResult analyze(World world, Set<BlockPos> positions) {
        Object2IntOpenHashMap<FacadeData> facadeDataToCount = new Object2IntOpenHashMap<>();
        Object2IntOpenHashMap<Block> unfacadedCount = new Object2IntOpenHashMap<>();
        for (BlockPos position : positions) {
            if (world.getTileEntity(position) instanceof IFacadeTileEntity) {
                IFacadeTileEntity blockEntity = (IFacadeTileEntity) world.getTileEntity(position);
                FacadeData facadeData = blockEntity.getFacadeData();
                facadeDataToCount.put(facadeData, facadeDataToCount.getInt(facadeData) + 1);
            } else {
                Block block = world.getBlockState(position).getBlock();
                unfacadedCount.put(block, unfacadedCount.getInt(block) + 1);
            }
        }
        return new FacadePlanAnalysisResult(facadeDataToCount, unfacadedCount, positions);
    }

    public boolean coversBigArea() {
        // 计算边界盒
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        
        if (positions.isEmpty()) {
            return false;
        }
        
        for (BlockPos pos : positions) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        
        int xSpan = maxX - minX+1;
        int ySpan = maxY - minY+1;
        int zSpan = maxZ - minZ+1;
        
        return xSpan > 8 || ySpan > 8 || zSpan > 10;
    }
    
    public boolean affectingMany() {
        return facadeDataToCount.values().stream().mapToInt(i -> i).sum() > 10;
    }
    
    public boolean affectingManyUnique() {
        return facadeDataToCount.keySet().size() > 1;
    }
    
    public boolean shouldWarn() {
        return affectingMany() || affectingManyUnique() || coversBigArea();
    }
    
    public int countAffected() {
        return facadeDataToCount.values().stream().mapToInt(i -> i).sum() + 
               unfacadedCount.values().stream().mapToInt(i -> i).sum();
    }
    
    // Getters
    public Map<FacadeData, Integer> getFacadeDataToCount() {
        return facadeDataToCount;
    }
    
    public Map<Block, Integer> getUnfacadedCount() {
        return unfacadedCount;
    }
    
    public Set<BlockPos> getPositions() {
        return positions;
    }
}
