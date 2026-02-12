package lotrfa.common.core;

import lotr.common.init.LOTRItems;
import lotrfa.common.LOTRFAMod;
import lotrfa.common.LOTRFAReference;
import lotrfa.common.init.LOTRFAItems;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class LOTRFARegistries {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS , LOTRFAReference.MOD_ID);

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, LOTRFAReference.MOD_ID);

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, LOTRFAReference.MOD_ID);

    public static final DeferredRegister<Biome> BIOMES = DeferredRegister.create(ForgeRegistries.BIOMES, LOTRFAReference.MOD_ID);

    public static void registerAll(IEventBus modEventBus) {
        LOTRFAMod.LOGGER.info("Registering First Age registries...");

        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        ENTITIES.register(modEventBus);
        BIOMES.register(modEventBus);

        LOTRFAMod.LOGGER.info("All registries registered successfully!");
    }

    public static final ItemGroup FIRST_AGE_TAB = new ItemGroup("lotrfa") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(LOTRItems.ALE.get());
        }

        @Override
        public void fillItemList(NonNullList<ItemStack> items) {
            super.fillItemList(items);
        }
    };

    public static final ItemGroup FIRST_AGE_LEGENDARY_TAB = new ItemGroup("lotrfa_legendary") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(LOTRFAItems.SILMARIL_MAEDHROS.get());
        }

        @Override
        public void fillItemList(NonNullList<ItemStack> items) {
            super.fillItemList(items);
        }
    };
}
