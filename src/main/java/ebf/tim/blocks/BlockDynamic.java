package ebf.tim.blocks;

import ebf.tim.TrainsInMotion;
import ebf.tim.TrainsInMotion.blockTypes;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * <h1>Block core</h1>
 * Generic block class to simplify the process of creating new blocks.
 * All the functionality for every block can be managed here, and then just instanced in the registry with new parameters.
 *
 * @author Eternal Blue Flame
 */
public class BlockDynamic extends BlockContainer {

    /**defines the type of tile entity that the block will spawn and manage*/
    private blockTypes type = blockTypes.CONTAINER;

    /**
     * <h2>block initializer</h2>
     *  Defines the material like what is necessary to make it and the creative tab for it, and the block name.
     */
    public BlockDynamic(String name, Material material, blockTypes blockType){
        super(material);
        setCreativeTab(TrainsInMotion.creativeTab);
        setBlockName(name);
        type = blockType;
    }

    /**
     * <h2>Block use</h2>
     * Called upon block activation (right click on the block.)
     * @return whether or not to animate the arm of the character for use.
     */
    @Override
    public boolean onBlockActivated(World worldOBJ, int x, int y, int z, EntityPlayer player, int p_149727_6_, float p_149727_7_, float p_149727_8_, float p_149727_9_) {

        switch (type) {
            case CONTAINER: case CRAFTING: {
                if (player.isSneaking()) {
                    return false;
                } else {
                    if (worldOBJ.isRemote) {
                        return true;
                    } else {
                        TileEntity entity = worldOBJ.getTileEntity(x, y, z);
                        if (entity != null) {
                            player.openGui(TrainsInMotion.instance, 0, worldOBJ, x, y, z);
                        }

                        return true;
                    }
                }
            }
            //cosmetic and otherwise
            default:{
                return false;
            }
        }

    }

    /**
     * <h2>Tile entity spawner</h2>
     * spawns the tile entity related to this block, if there isn't one already. Called on placing the block.
     */
    public TileEntity createNewTileEntity(World worldObj, int meta){
        return new TileEntityStorage();
    }

}
