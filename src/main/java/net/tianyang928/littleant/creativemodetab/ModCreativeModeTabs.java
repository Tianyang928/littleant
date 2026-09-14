package net.tianyang928.littleant.creativemodetab;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tianyang928.littleant.LittleAnt;
import net.tianyang928.littleant.block.ModBlocks;
import net.tianyang928.littleant.item.ModItems;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LittleAnt.MOD_ID);

    public static final Supplier<CreativeModeTab> ANT_ITEMS_TAB = CREATIVE_MODE_TABS.register("ant_items_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.ANT_SPAWN_EGG.get()))
                    .title(Component.translatable("creative_mode_tab.littleant.ant_items_tab"))
                    .displayItems((displayParameters,output) -> {
                        output.accept(ModItems.ANT_SPAWN_EGG);
                        //output.accept(ModBlocks.ANT_CRAFTING_TABLE);
                        output.accept(ModItems.PHEROMONE_BLOCK);
                    })
                    .build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
