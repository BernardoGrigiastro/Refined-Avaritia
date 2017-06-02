package com.expensivekoala.refined_avaritia.tile;

import com.expensivekoala.refined_avaritia.RefinedAvaritia;
import com.raoulvdberge.refinedstorage.RSUtils;
import com.raoulvdberge.refinedstorage.api.autocrafting.ICraftingPattern;
import com.raoulvdberge.refinedstorage.api.autocrafting.ICraftingPatternContainer;
import com.raoulvdberge.refinedstorage.api.autocrafting.ICraftingPatternProvider;
import com.raoulvdberge.refinedstorage.api.network.INetworkMaster;
import com.raoulvdberge.refinedstorage.api.util.IComparer;
import com.raoulvdberge.refinedstorage.inventory.ItemHandlerBasic;
import com.raoulvdberge.refinedstorage.inventory.ItemHandlerUpgrade;
import com.raoulvdberge.refinedstorage.item.ItemUpgrade;
import com.raoulvdberge.refinedstorage.tile.TileNode;
import com.raoulvdberge.refinedstorage.tile.data.ITileDataConsumer;
import com.raoulvdberge.refinedstorage.tile.data.ITileDataProducer;
import com.raoulvdberge.refinedstorage.tile.data.TileDataParameter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import java.util.ArrayList;
import java.util.List;

public class TileExtremeCrafter extends TileNode implements ICraftingPatternContainer {

    public static final TileDataParameter<Boolean> TRIGGERED_AUTOCRAFTING = new TileDataParameter<>(DataSerializers.BOOLEAN, false, new ITileDataProducer<Boolean, TileExtremeCrafter>() {
        @Override
        public Boolean getValue(TileExtremeCrafter tile) {
            return tile.triggeredAutocrafting;
        }
    }, new ITileDataConsumer<Boolean, TileExtremeCrafter>() {
        @Override
        public void setValue(TileExtremeCrafter tile, Boolean value) {
            tile.triggeredAutocrafting = value;

            tile.markDirty();
        }
    });

    private static final String NBT_TRIGGERED_AUTOCRAFTING = "TriggeredAutocrafting";

    private ItemHandlerBasic patterns = new ItemHandlerBasic(9, this, s -> {
        if(getWorld() != null) {
            return s.getItem() instanceof ICraftingPatternProvider && ((ICraftingPatternProvider)s.getItem()).create(getWorld(), s, this).isValid();
        }
        return true;
    }){
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);

            if(getWorld() != null && !getWorld().isRemote) {
                rebuildPatterns();
            }

            if(network != null) {
                network.rebuildPatterns();
            }
        }
    };

    private List<ICraftingPattern> actualPatterns = new ArrayList<>();

    private ItemHandlerUpgrade upgrades = new ItemHandlerUpgrade(4, this, ItemUpgrade.TYPE_SPEED);

    private boolean triggeredAutocrafting = false;

    public TileExtremeCrafter() {
        dataManager.addWatchedParameter(TRIGGERED_AUTOCRAFTING);
    }

    private void rebuildPatterns() {
        actualPatterns.clear();

        for (int i = 0; i < patterns.getSlots(); i++) {
            ItemStack patternStack = patterns.getStackInSlot(i);

            if(patternStack != null) {
                ICraftingPattern pattern = ((ICraftingPatternProvider)patternStack.getItem()).create(getWorld(), patternStack, this);
                if(pattern.isValid()) {
                    actualPatterns.add(pattern);
                }
            }
        }
    }

    @Override
    public int getSpeedUpdateCount()
    {
        return upgrades.getUpgradeCount(ItemUpgrade.TYPE_SPEED);
    }

    @Override
    public IItemHandler getFacingInventory()
    {
        return null;
    }

    @Override
    public List<ICraftingPattern> getPatterns()
    {
        return actualPatterns;
    }

    @Override
    public void update() {
        if(!getWorld().isRemote && ticks == 0) {
            rebuildPatterns();
        }
        super.update();
    }

    @Override
    public void updateNode()
    {
        if(triggeredAutocrafting && getWorld().isBlockPowered(pos)) {
            for(ICraftingPattern pattern: actualPatterns) {
                for(ItemStack output : pattern.getOutputs()) {
                    network.scheduleCraftingTask(output, 1, IComparer.COMPARE_DAMAGE | IComparer.COMPARE_NBT);
                }
            }
        }
    }

    @Override
    public void onConnectionChange(INetworkMaster network, boolean state) {
        if (!state) {
            network.getCraftingTasks().stream()
                    .filter(task -> task.getPattern().getContainer().getPosition().equals(pos))
                    .forEach(network::cancelCraftingTask);
        }

        //network.rebuildPatterns();
    }

    @Override
    public NBTTagCompound write(NBTTagCompound tag) {
        super.write(tag);

        RSUtils.writeItems(patterns, 0, tag);
        RSUtils.writeItems(upgrades, 1, tag);

        return tag;
    }

    @Override
    public NBTTagCompound writeConfiguration(NBTTagCompound tag) {
        super.writeConfiguration(tag);

        tag.setBoolean(NBT_TRIGGERED_AUTOCRAFTING, triggeredAutocrafting);

        return tag;
    }

    @Override
    public void readConfiguration(NBTTagCompound tag) {
        super.readConfiguration(tag);

        if (tag.hasKey(NBT_TRIGGERED_AUTOCRAFTING)) {
            triggeredAutocrafting = tag.getBoolean(NBT_TRIGGERED_AUTOCRAFTING);
        }
    }

    @Override
    public int getEnergyUsage()
    {
        int usage = RefinedAvaritia.instance.config.extremeCrafterUsage;

        for (int i = 0; i < patterns.getSlots(); i++) {
            if(patterns.getStackInSlot(i) != null)
                usage += RefinedAvaritia.instance.config.extremeCrafterUsagePerPattern;
        }

        return usage;
    }

    @Override
    public IItemHandler getDrops() {
        return new CombinedInvWrapper(patterns, upgrades);
    }

    public IItemHandler getPatternItems() {
        return patterns;
    }

    public IItemHandler getUpgrades() {
        return upgrades;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {

        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return (T) patterns;
        return super.getCapability(capability, facing);
    }
}
