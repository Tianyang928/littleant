package net.tianyang928.littleant.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.tianyang928.littleant.LittleAnt;
import net.tianyang928.littleant.gui.AntInventoryMenu;

/** A compact chest-style screen for editing an ant's carried items and armor. */
public class AntInventoryScreen extends AbstractContainerScreen<AntInventoryMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(LittleAnt.MOD_ID, "textures/gui/container/ant_inventory_background.png");

    public AntInventoryScreen(AntInventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 256;
        this.imageHeight = 256;
        this.inventoryLabelY = 79;
        this.inventoryLabelX = 82;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(BACKGROUND, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        if(this.menu.ant == null) {
            LittleAnt.LOGGER.info("[AntInventoryScreen] client ant is null");
            return;
        }
        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, x+26, y+18, x+77, y+86, 25, 0.1F, mouseX, mouseY, this.menu.ant);
    }

}
