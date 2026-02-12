package lotrfa.common.init;

import lotrfa.common.LOTRFAMod;
import lotrfa.common.core.LOTRFARegistries;
import lotrfa.common.enums.SilmarilsType;
import lotrfa.common.legendary.Silmarils;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;

public class LOTRFAItems {
    public static final RegistryObject<Item> SILMARIL_VARDA = LOTRFARegistries.ITEMS.register("silmaril_varda", () -> new Silmarils(SilmarilsType.VARDA));
    public static final RegistryObject<Item> SILMARIL_EARENDIL = LOTRFARegistries.ITEMS.register("silmaril_earendil", () -> new Silmarils(SilmarilsType.EARENDIL));
    public static final RegistryObject<Item> SILMARIL_MAEDHROS = LOTRFARegistries.ITEMS.register("silmaril_maedhros", () -> new Silmarils(SilmarilsType.MAEDHROS));

    public static void init() {
        LOTRFAMod.LOGGER.info("First Age items initialized - {} items registered", LOTRFARegistries.ITEMS.getEntries().size());
    }
}
