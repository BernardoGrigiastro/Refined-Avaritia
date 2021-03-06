package com.expensivekoala.refined_avaritia.block;

import com.expensivekoala.refined_avaritia.RefinedAvaritia;
import com.expensivekoala.refined_avaritia.gui.handlers.GuiHandler;
import com.expensivekoala.refined_avaritia.tile.TileExtremePatternEncoder;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class BlockExtremePatternEncoder extends Block implements ITileEntityProvider{

    public BlockExtremePatternEncoder() {
        super(Material.ROCK);
        setHardness(1.9f);
        setRegistryName(RefinedAvaritia.MODID, "extreme_pattern_encoder");
        setCreativeTab(RefinedAvaritia.instance.tab);
    }

    @Override
    public String getTranslationKey() {
        return "block." + RefinedAvaritia.MODID + ":extreme_pattern_encoder";
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("block.refined_avaritia:extreme_pattern_encoder.tooltip"));
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileExtremePatternEncoder();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(!worldIn.isRemote) {
            playerIn.openGui(RefinedAvaritia.instance, GuiHandler.EXTREME_PATTERN_ENCODER_GUI, worldIn, pos.getX(), pos.getY(), pos.getZ());
        }

        return true;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof TileExtremePatternEncoder) {
            for (int i = 0; i < ((TileExtremePatternEncoder) tile).getPatterns().getSlots(); i++) {
                ItemStack stack = ((TileExtremePatternEncoder) tile).getPatterns().getStackInSlot(i);
                if(stack != null) {
                    worldIn.spawnEntity(new EntityItem(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack));
                }
            }
        }

        super.breakBlock(worldIn, pos, state);
    }

    public ItemBlock createItem() {
        ItemBlock itemBlock = new ItemBlock(this);
        itemBlock.setRegistryName(this.getRegistryName());
        return itemBlock;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileExtremePatternEncoder();
    }
}
