package ca.teamdman.sfm.common.facade;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FacadeData implements Comparable<FacadeData> {
    private final IBlockState facadeBlockState;
    public final EnumFacing facadeDirection;
    private final FacadeTextureMode facadeTextureMode;

    public FacadeData(IBlockState state, EnumFacing facing, FacadeTextureMode textureMode) {
        this.facadeBlockState = state;
        this.facadeDirection = facing;
        this.facadeTextureMode = textureMode;
    }

    // Getter methods
    public IBlockState getFacadeBlockState() {
        return facadeBlockState;
    }

    public EnumFacing getFacadeDirection() {
        return facadeDirection;
    }

    public FacadeTextureMode getFacadeTextureMode() {
        return facadeTextureMode;
    }

    public void save(NBTTagCompound tag) {
        NBTTagCompound facadeTag = new NBTTagCompound();
        NBTUtil.writeBlockState(facadeTag, this.facadeBlockState);
        facadeTag.setString("direction", this.facadeDirection.getName());
        facadeTag.setString("texture_mode", this.facadeTextureMode.name());
        tag.setTag("sfm:facade", facadeTag);
    }

    public static @Nullable FacadeData load(@Nullable World world, NBTTagCompound tag) {
        if (tag.hasKey("sfm:facade", 10)) {
            NBTTagCompound facadeTag = tag.getCompoundTag("sfm:facade");
            //todo del readBlockState()
            IBlockState facadeState = NBTUtil.readBlockState(facadeTag);
            EnumFacing facadeDirection = EnumFacing.byName(facadeTag.getString("direction"));
            FacadeTextureMode facadeTextureMode = FacadeTextureMode.byName(facadeTag.getString("texture_mode"));
            if (facadeTextureMode != null && facadeDirection != null) {
                return new FacadeData(facadeState, facadeDirection, facadeTextureMode);
            }
        }
        return null;
    }

    @Override
    public int compareTo(@NotNull FacadeData o) {
        return 0;
    }

    public IBlockState facadeBlockState() {
        return this.facadeBlockState;
    }
}
