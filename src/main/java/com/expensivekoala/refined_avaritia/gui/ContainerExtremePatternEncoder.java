package com.expensivekoala.refined_avaritia.gui;

import com.expensivekoala.refined_avaritia.gui.slots.SlotPhantom;
import com.expensivekoala.refined_avaritia.tile.TileExtremePatternEncoder;
import com.raoulvdberge.refinedstorage.container.ContainerBase;
import com.raoulvdberge.refinedstorage.container.slot.SlotOutput;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerExtremePatternEncoder extends ContainerBase {

    TileExtremePatternEncoder tile;

    public ContainerExtremePatternEncoder(TileExtremePatternEncoder tile, EntityPlayer player) {
        super(tile, player);
        this.tile = tile;
        addSlotToContainer(new SlotItemHandler(tile.getPatterns(), 0, 199, 37));
        addSlotToContainer(new SlotOutput(tile.getPatterns(), 1, 199, 77));

        int x = 12;
        int y = 8;
        for (int i = 0; i < 81; i++) {
            addSlotToContainer(new SlotPhantom(tile.getRecipe(), i, x, y, false, tile));

            x += 18;

            if((i + 1) % 9 == 0) {
                y += 18;
                x = 12;
            }
        }

        //210, 121 addSlotToContainer()
        addSlotToContainer(new SlotPhantom(tile.getRecipeOutput(), 0, 210, 121, true, tile));
        addPlayerInventory(39,174);
        tile.onContentsChanged();
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        ItemStack stack = null;
        Slot slot = getSlot(slotIndex);

        if(slot instanceof SlotPhantom && ((SlotPhantom)slot).isOutput()) {
            return ItemStack.EMPTY;
        }

        if(slot != null && !(slot instanceof SlotPhantom) && slot.getHasStack()) {
            stack = slot.getStack();

            if(slotIndex < 2) {
                if(!mergeItemStack(stack, 2 + 18, inventorySlots.size(), false))
                    return ItemStack.EMPTY;
            } else if(!mergeItemStack(stack, 0, 1, false))
                return ItemStack.EMPTY;

            if(stack.getCount() == 0)
                slot.putStack(ItemStack.EMPTY);
            else
                slot.onSlotChanged();
        }

        return stack == null ? ItemStack.EMPTY : stack;
    }

    @Override
    public ItemStack slotClick(int id, int dragType, ClickType clickType, EntityPlayer player) {
        Slot slot = id >= 0 ? getSlot(id) : null;

        if(slot instanceof SlotPhantom) {
            if(((SlotPhantom) slot).isOutput())
                return ItemStack.EMPTY;

            if(!player.inventory.getItemStack().isEmpty() && slot.isItemValid(player.inventory.getItemStack())) {
                slot.putStack(player.inventory.getItemStack().copy());
            } else if(player.inventory.getItemStack().isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            }
            return player.inventory.getItemStack();
        }
        return super.slotClick(id, dragType, clickType, player);
    }
}
