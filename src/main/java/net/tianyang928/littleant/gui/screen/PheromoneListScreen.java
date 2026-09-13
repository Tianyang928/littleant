package net.tianyang928.littleant.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tianyang928.littleant.LittleAnt;
import net.tianyang928.littleant.gui.PheromoneListMenu;
import net.tianyang928.littleant.network.SetPheromonePayload;

import java.util.LinkedHashMap;

public class PheromoneListScreen extends AbstractContainerScreen<PheromoneListMenu> {

    private static final ResourceLocation BACKGROUND_LOCATION = ResourceLocation.fromNamespaceAndPath(LittleAnt.MOD_ID,"textures/gui/container/pheromone_list_background.png");
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.fromNamespaceAndPath(LittleAnt.MOD_ID,"textures/gui/container/scroller.png");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.fromNamespaceAndPath(LittleAnt.MOD_ID,"textures/gui/container/scroller_disabled.png");
    private static final ResourceLocation ADD_BUTTON = ResourceLocation.fromNamespaceAndPath(LittleAnt.MOD_ID,"textures/gui/container/add_button.png");
    private static final ResourceLocation MINUS_BUTTON = ResourceLocation.fromNamespaceAndPath(LittleAnt.MOD_ID,"textures/gui/container/minus_button.png");

    private int scrollOff;
    private int selectedItem;
    private boolean isDragging;
    // buttons rendered on screen
    private final PheromoneListScreen.PheromoneStringWidget[] pheromoneTextWidget = new PheromoneListScreen.PheromoneStringWidget[6];
    private final PheromoneListScreen.PheromoneButton[] pheromoneAddButtons = new PheromoneListScreen.PheromoneButton[6];
    private final PheromoneListScreen.PheromoneButton[] pheromoneMinusButtons = new PheromoneListScreen.PheromoneButton[6];
    private final PheromoneListScreen.PheromoneButton[] pheromoneAddItemButton = new PheromoneListScreen.PheromoneButton[1];
    private final PheromoneListScreen.PheromoneLineEdit[] pheromoneLineEdit = new PheromoneListScreen.PheromoneLineEdit[2];

    public PheromoneListScreen(PheromoneListMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.inventoryLabelY = 500;
    }

    private void sendChangePheromoneData(String pheromoneId, int pheromoneAmount) {
        PacketDistributor.sendToServer(
                new SetPheromonePayload(
                        this.menu.containerId,
                        pheromoneId,
                        pheromoneAmount
                )
        );

        LittleAnt.LOGGER.info("[PheromoneListScreen] sendChangePheromoneData: {} {}", pheromoneId, pheromoneAmount);
    }

    // send id and amount to add new pheromone
    private void sendNewPheromoneData() {
        String id = this.pheromoneLineEdit[0].getValue().trim();
        if (id.isEmpty()) {
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(this.pheromoneLineEdit[1].getValue());//amount
        } catch (NumberFormatException exception) {
            return;
        }

        PacketDistributor.sendToServer(
                new SetPheromonePayload(
                        this.menu.containerId,
                        id,
                        amount
                )
        );

        LittleAnt.LOGGER.info("[PheromoneListScreen] sendNewPheromoneData: {} {}", id, amount);
    }

    @Override
    protected void init() {
        super.init();
        int xo = (this.width - this.imageWidth) / 2;
        int yo = (this.height - this.imageHeight) / 2;
        int buttonY = yo + 16 + 2;

        for (int i = 0; i < 6; i++) {
            this.pheromoneTextWidget[i] = this.addRenderableWidget(new PheromoneStringWidget(this.font, xo + 5, buttonY, 120, 20, i + 1, Component.literal("")));
            this.pheromoneTextWidget[i].setWidth(120);

            this.pheromoneAddButtons[i] = this.addRenderableWidget(new PheromoneButton(xo + 5 + 122, buttonY, 20, 20, i + 6 + 1, button -> {
                if (button instanceof PheromoneButton) {
                    //this.selectedItem = (((PheromoneButton)button).getIndex() - 6 + this.scrollOff)*2;
                    int index = ((PheromoneButton)button).getIndex();
                    String pheromoneId = this.pheromoneTextWidget[index-7].pheromoneId;
                    int pheromoneAmount = this.pheromoneTextWidget[index-7].pheromoneAmount;
                    pheromoneAmount++;
                    this.sendChangePheromoneData(pheromoneId, pheromoneAmount);
                }
            }));

            this.pheromoneMinusButtons[i] = this.addRenderableWidget(new PheromoneButton(xo + 5 + 122 + 20, buttonY, 20, 20, i+12+1, button -> {
                if (button instanceof PheromoneButton) {
                    //this.selectedItem = (((PheromoneButton)button).getIndex() - 12 + this.scrollOff)*3;
                    int index = ((PheromoneButton)button).getIndex();
                    String pheromoneId = this.pheromoneTextWidget[index-13].pheromoneId;
                    int pheromoneAmount = this.pheromoneTextWidget[index-13].pheromoneAmount;
                    pheromoneAmount--;
                    this.sendChangePheromoneData(pheromoneId, pheromoneAmount);
                }
            }));

            buttonY += 20;
        }

        this.pheromoneAddItemButton[0] = this.addRenderableWidget(new PheromoneButton(xo+5 +122, buttonY, 40, 20, 0, button -> {
            if (button instanceof PheromoneButton) {
                this.selectedItem = 0;
                this.sendNewPheromoneData();
            }
        }));

        //initialize line edits
        this.pheromoneLineEdit[0] = this.addRenderableWidget(new PheromoneLineEdit(this.font, xo + 5 , buttonY, 60, 20, 0, Component.translatable("gui.littleant.pheromone_id")));
        this.pheromoneLineEdit[0].setTooltip(
                Tooltip.create(Component.translatable("gui.littleant.pheromone_id_hint"))
        );
        this.pheromoneLineEdit[0].setFilter(v -> v.isEmpty() || v.matches("^[a-zA-Z0-9_]+$"));
        this.pheromoneLineEdit[0].setMaxLength(64);
        this.pheromoneLineEdit[1] = this.addRenderableWidget(new PheromoneLineEdit(this.font, xo + 5 +61, buttonY, 60, 20, 1, Component.translatable("gui.littleant.pheromone_amount")));
        this.pheromoneLineEdit[1].setTooltip(
                Tooltip.create(Component.translatable("gui.littleant.pheromone_amount_hint"))
        );
        this.pheromoneLineEdit[1].setFilter(v -> v.isEmpty() || v.matches("\\d+"));
        this.pheromoneLineEdit[1].setMaxLength(8);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float a, int mouseX, int mouseY) {
        int xo = (this.width - this.imageWidth) / 2;
        int yo = (this.height - this.imageHeight) / 2;
        graphics.blit(BACKGROUND_LOCATION, xo, yo, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 512, 256);
    }

    private void extractScroller(GuiGraphics graphics, int xo, int yo, int mouseX, int mouseY, LinkedHashMap<String, Integer> pheromones) {
        int steps = pheromones.size() + 1 - 6;
        if (steps > 1) {
            int leftOver = 139 - (27 + (steps - 1) * 139 / steps);
            int stepHeight = 1 + leftOver / steps + 139 / steps;
            int maxScrollerOff = 113;
            int scrollerYOff = Math.min(113, this.scrollOff * stepHeight);
            if (this.scrollOff == steps - 1) {
                scrollerYOff = 113;
            }

            int scrollerX = xo + 167;
            int scrollerY = yo + 18 + scrollerYOff;
            graphics.blit(SCROLLER_SPRITE, scrollerX, scrollerY, 0.0F,0.0F, 6, 27,6,27);
        } else {
            graphics.blit(SCROLLER_DISABLED_SPRITE, xo + 167, yo + 18, 0.0F, 0.0F, 6, 27,6, 27);
        }
    }

    private void extractAddImage(GuiGraphics graphics, int xo, int yo) {
        graphics.blit(ADD_BUTTON, xo, yo + 5, 0.0F,0.0F,10, 10,10,10);
    }

    private void extractMinusImage(GuiGraphics graphics, int xo, int yo) {
        graphics.blit(MINUS_BUTTON, xo, yo + 5, 0.0F,0.0F,10, 10,10,10);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float a) {
        super.render(graphics, mouseX, mouseY, a);
        LinkedHashMap<String, Integer> pheromones = this.menu.getPheromoneList();
        int xo = (this.width - this.imageWidth) / 2;
        int yo = (this.height - this.imageHeight) / 2;
        int pheromoneY = yo + 16 + 1;
        if (!pheromones.isEmpty()) {

            this.extractScroller(graphics, xo, yo, mouseX, mouseY, pheromones);
            int currentPheromoneIndex = 0;
            int textButtonIndex = 0;

            for (String pheromone : pheromones.keySet()) {
                if (!this.canScroll(pheromones.size()) || currentPheromoneIndex >= this.scrollOff && currentPheromoneIndex < 6 + this.scrollOff) {
                    int decorHeight = pheromoneY + 1;

                    String text = "Id: " + pheromone.toString() + " N: " + pheromones.get(pheromone).toString();
                    this.pheromoneTextWidget[textButtonIndex].setMessage(Component.literal(text));
                    this.extractAddImage(graphics, xo + 5 + 127, decorHeight);
                    this.extractMinusImage(graphics, xo + 5 + 127 + 20, decorHeight);

                    pheromoneY += 20;
                    currentPheromoneIndex++;

                    // store in the button, the current text written on it
                    this.pheromoneTextWidget[textButtonIndex].pheromoneId = pheromone;
                    this.pheromoneTextWidget[textButtonIndex].pheromoneAmount = pheromones.get(pheromone);
                    textButtonIndex++;
                } else {
                    currentPheromoneIndex++;
                }
            }

            // 如果这个条目上有信息素显示，则显示当前按钮
            for (PheromoneStringWidget widget : this.pheromoneTextWidget) {
                widget.visible = widget.index-1 < pheromones.size();
            }
            for (PheromoneButton button : this.pheromoneMinusButtons) {
                button.visible = button.index-13 < pheromones.size();
            }
            for (PheromoneButton button : this.pheromoneAddButtons) {
                button.visible = button.index-7 < pheromones.size();
            }

        }

        this.extractAddImage(graphics, xo+5+122+15, yo+16+1+6*20+1);
        // 显示添加信息素种类按钮
        this.pheromoneAddItemButton[0].visible = true;
    }

    private boolean canScroll(int numberOfOffers) {
        return numberOfOffers > 6;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (super.mouseScrolled(x, y, scrollX, scrollY)) {
            return true;
        } else {
            int numberOfOffers = this.menu.getPheromoneList().size();
            if (this.canScroll(numberOfOffers)) {
                int maxScrollOff = numberOfOffers - 6;
                this.scrollOff = Mth.clamp((int)(this.scrollOff - scrollY), 0, maxScrollOff);
            }

            return true;
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        int numberOfOffers = this.menu.getPheromoneList().size();
        if (this.isDragging) {
            int fullScrollTopPos = this.topPos + 18;
            int fullScrollBottomPos = fullScrollTopPos + 139;
            int maxScrollOff = numberOfOffers - 6;
            float scrolling = ((float)mouseY - fullScrollTopPos - 13.5F) / (fullScrollBottomPos - fullScrollTopPos - 27.0F);
            scrolling = scrolling * maxScrollOff + 0.5F;
            this.scrollOff = Mth.clamp((int)scrolling, 0, maxScrollOff);
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, dx, dy);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int xo = (this.width - this.imageWidth) / 2;
        int yo = (this.height - this.imageHeight) / 2;
        if (this.canScroll(this.menu.getPheromoneList().size())
                && mouseX > xo + 167 && mouseX < xo + 167 + 6
                && mouseY > yo + 18 && mouseY <= yo + 18 + 139 + 1) {
            this.isDragging = true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.isDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private class PheromoneButton extends Button {
        final int index;

        public PheromoneButton(int x, int y, int width, int height, int index, Button.OnPress onPress) {
            super(x, y, width, height, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
            this.index = index;
            this.visible = false;
        }

        public int getIndex() {
            return this.index;
        }
    }

    private class PheromoneLineEdit extends EditBox {
        final int index;

        public PheromoneLineEdit(Font font, int x, int y, int width, int height, int index, Component suggestion) {
            super(font, x, y, width, height, suggestion);
            this.index = index;
            this.visible = true;
        }

        public int getIndex() {
            return this.index;
        }
    }

    private class PheromoneStringWidget extends StringWidget {
        final int index;
        String pheromoneId = "";
        int pheromoneAmount;
        private int marqueeOffset;
        private long lastMarqueeTick;

        public PheromoneStringWidget(Font font, int x, int y, int width, int height, int index, Component message) {
            super(x, y, width, height,message,font);
            this.index = index;
        }

        public int getIndex() {
            return this.index;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            Component message = this.getMessage();
            int textWidth = PheromoneListScreen.this.font.width(message);
            int width = this.getWidth();
            if (textWidth <= width) {
                super.renderWidget(graphics, mouseX, mouseY, partialTick);
                return;
            }

            long tick = PheromoneListScreen.this.minecraft.level == null
                    ? System.currentTimeMillis() / 50L
                    : PheromoneListScreen.this.minecraft.level.getGameTime();
            if (tick != this.lastMarqueeTick) {
                this.lastMarqueeTick = tick;
                if (tick % 3L == 0L) this.marqueeOffset++;
            }
            int cycle = textWidth + PheromoneListScreen.this.font.width("   ");
            if (this.marqueeOffset >= cycle) this.marqueeOffset = 0;

            int x = this.getX();
            int y = this.getY() + (this.getHeight() - 9) / 2;
            graphics.enableScissor(x, this.getY(), x + width, this.getY() + this.getHeight());
            graphics.drawString(this.getFont(), message, x - this.marqueeOffset, y, this.getColor());
            if (this.marqueeOffset > textWidth) {
                graphics.drawString(this.getFont(), message, x + cycle - this.marqueeOffset, y, this.getColor());
            }
            graphics.disableScissor();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (getFocused() instanceof EditBox editBox
                && editBox.isFocused()
                && minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode))) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
