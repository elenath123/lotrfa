package lotrfa.common;

import lotrfa.common.core.LOTRFARegistries;
import lotrfa.common.init.LOTRFAItems;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

@Mod(LOTRFAReference.MOD_ID)
public class LOTRFAMod {
    public static final Logger LOGGER = LogManager.getLogger();

    public LOTRFAMod() {
        LOGGER.info("═══════════════════════════════════════");
        LOGGER.info("  LOTR: First Age - 第一纪元");
        LOGGER.info("  By Elenath123");
        LOGGER.info("═══════════════════════════════════════");

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        LOTRFARegistries.registerAll(modEventBus);

        LOTRFAItems.init();

        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::clientSetup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Common setup complete - 通用设置完成");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Client setup complete - 客户端设置完成");
    }
}
