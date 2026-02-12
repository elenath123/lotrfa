package lotrfa.common.legendary;

import lotr.common.LOTRLog;
import lotr.common.data.AlignmentDataModule;
import lotr.common.data.LOTRLevelData;
import lotr.common.data.LOTRPlayerData;
import lotr.common.fac.Faction;
import lotr.common.fac.FactionSettings;
import lotr.common.fac.FactionSettingsManager;
import lotrfa.common.core.LOTRFARegistries;
import lotrfa.common.enums.SilmarilsType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class Silmarils extends Item {
    private final SilmarilsType silmarilsType;

    private static final float EVIL_ALIGNMENT_THRESHOLD = -100.0F;
    private static final float GOOD_ALIGNMENT_REQUIRED = 100.0F;

    public Silmarils(SilmarilsType silmarilsType) {
        super(new Properties().tab(LOTRFARegistries.FIRST_AGE_LEGENDARY_TAB).stacksTo(1).fireResistant().rarity(Rarity.EPIC));
        this.silmarilsType = silmarilsType;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        super.inventoryTick(stack, world, entity, itemSlot, isSelected);

        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;

            if (!world.isClientSide) {
                if (world.getGameTime() % 20 == 0) {
                    boolean isEvil = living instanceof PlayerEntity ? isEvilPlayer((PlayerEntity) living, world) : isEvilEntity(living);

                    if (isEvil) {
                        living.hurt(createHolyDamage(), 2.0F);
                        if (living instanceof PlayerEntity) {
                            ((PlayerEntity) living).displayClientMessage(new TranslationTextComponent("item.lotrfa.silmaril.burns").withStyle(TextFormatting.RED, TextFormatting.BOLD), true);
                        }
                    } else {
                        applyHolyAura(living, world);
                    }
                }
            }
        }
    }

    private boolean isEvilEntity(LivingEntity entity) {
        if (!entity.getType().getCategory().isFriendly()) {
            return true;
        }

        String entityName = entity.getType().getRegistryName().toString().toLowerCase();

        if (entityName.contains("mordor") || entityName.contains("orc") || entityName.contains("uruk") || entityName.contains("gundabad") || entityName.contains("angmar") || entityName.contains("dol_guldur") || entityName.contains("isengard") && entityName.contains("snaga")) {
            return true;
        }

        return false;
    }

    private boolean isEvilPlayer(PlayerEntity player, World world) {
        try {
            LOTRPlayerData playerData = LOTRLevelData.sidedInstance(world).getData(player);
            AlignmentDataModule alignData = playerData.getAlignmentData();

            FactionSettings factionSettings = FactionSettingsManager.sidedInstance(world).getCurrentLoadedFactions();

            if (factionSettings == null) {
                LOTRLog.warn("FactionSettings is null when checking Silmaril for player {}", player.getName().getString());
                return false;
            }

            float totalGoodAlignment = 0.0F;
            int goodFactionsCount = 0;
            float totalEvilAlignment = 0.0F;
            int evilFactionsCount = 0;

            for (Faction faction : factionSettings.getAllPlayableAlignmentFactions()) {
                float alignment = alignData.getAlignment(faction);

                if (isGoodFaction(faction)) {
                    totalGoodAlignment += alignment;
                    goodFactionsCount++;
                } else if (isEvilFaction(faction)) {
                    totalEvilAlignment += alignment;
                    evilFactionsCount++;
                }
            }

            float avgGoodAlignment = goodFactionsCount > 0 ? totalGoodAlignment / goodFactionsCount : 0;
            float avgEvilAlignment = evilFactionsCount > 0 ? totalEvilAlignment / evilFactionsCount : 0;

            if (avgEvilAlignment > GOOD_ALIGNMENT_REQUIRED) {
                LOTRLog.debug("Player {} is evil due to high evil alignment: {}", player.getName().getString(), avgEvilAlignment);
                return true;
            }

            if (avgGoodAlignment < EVIL_ALIGNMENT_THRESHOLD) {
                LOTRLog.debug("Player {} is evil due to low good alignment: {}", player.getName().getString(), avgGoodAlignment);
                return true;
            }

            return false;

        } catch (Exception e) {
            LOTRLog.error("Error checking player alignment for Silmaril: {}", player.getName().getString(), e);
            return false;
        }
    }

    private boolean isGoodFaction(Faction faction) {
        String factionName = faction.getName().toString().toLowerCase();

        if (factionName.contains("gondor") || factionName.contains("rohan") || factionName.contains("shire") || factionName.contains("hobbit") || factionName.contains("bree") || factionName.contains("ranger") || factionName.contains("dunedain") || factionName.contains("dale") || factionName.contains("esgaroth")) {
            return true;
        }

        if (factionName.contains("elf") || factionName.contains("galadhrim") || factionName.contains("lothlorien") || factionName.contains("rivendell") || factionName.contains("lindon") || factionName.contains("woodland")) {
            return true;
        }

        if (factionName.contains("dwarf") || factionName.contains("durin") || factionName.contains("erebor") || factionName.contains("iron_hills") || factionName.contains("blue_mountain")) {
            return true;
        }

        return false;
    }

    private boolean isEvilFaction(Faction faction) {
        String factionName = faction.getName().toString().toLowerCase();

        if (factionName.contains("mordor") || factionName.contains("isengard") || factionName.contains("angmar") || factionName.contains("dol_guldur") || factionName.contains("gundabad") || factionName.contains("rhun") || factionName.contains("khand") || factionName.contains("harad") && !factionName.contains("coast")) {
            return true;
        }

        return false;
    }

    private DamageSource createHolyDamage() {
        return new DamageSource("silmaril_holy_light").bypassArmor().bypassMagic().setMagic();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    private void applyHolyAura(LivingEntity entity, World world) {
        double auraRadius = 8.0D;

        List<LivingEntity> nearbyEntities = world.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(auraRadius), (target) -> target != entity && target.isAlive());

        for (LivingEntity target : nearbyEntities) {
            if (isGoodEntity(target, world)) {
                if (world.getGameTime() % 80 == 0) {
                    float healAmount = 1.0F;
                    target.heal(healAmount);

                    if (world.isClientSide) {
                        spawnHealingParticles(world, target);
                    }
                }
            }

            if (target.getMobType() == CreatureAttribute.UNDEAD) {
                if (world.getGameTime() % 40 == 0) {
                    float damage = 2.0F;
                    target.hurt(createHolyDamage(), damage);

                    if (world.isClientSide) {
                        spawnBanishParticles(world, target);
                    }
                }
            }

            if (isAllyEntity(target, entity, world)) {
                target.addEffect(new EffectInstance(Effects.DAMAGE_BOOST, 100, 0, true, true));

                target.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 100, 0, true, true));

                target.addEffect(new EffectInstance(Effects.REGENERATION, 100, 0, true, true));
            }
        }

        if (world.isClientSide && world.getGameTime() % 10 == 0) {
            spawnAuraParticles(world, entity, auraRadius);
        }
    }

    private boolean isGoodEntity(LivingEntity entity, World world) {
        if (entity instanceof PlayerEntity) {
            return !isEvilPlayer((PlayerEntity) entity, world);
        }

        if (entity.getType().getCategory().isFriendly()) {
            return true;
        }

        String entityName = entity.getType().getRegistryName().toString().toLowerCase();
        if (entityName.contains("gondor") || entityName.contains("rohan") || entityName.contains("elf") || entityName.contains("dwarf") || entityName.contains("hobbit")) {
            return true;
        }

        return false;
    }

    private boolean isAllyEntity(LivingEntity target, LivingEntity holder, World world) {
        if (holder instanceof PlayerEntity) {
            if (target instanceof TameableEntity) {
                TameableEntity tamable = (TameableEntity) target;
                return tamable.isTame() && tamable.getOwner() == holder;
            }

            if (target instanceof PlayerEntity) {
                return !isEvilPlayer((PlayerEntity) target, world);
            }

            return isGoodEntity(target, world);
        }

        return false;
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnHealingParticles(World world, LivingEntity entity) {
        for (int i = 0; i < 5; i++) {
            double x = entity.getX() + (world.random.nextDouble() - 0.5) * entity.getBbWidth();
            double y = entity.getY() + world.random.nextDouble() * entity.getBbHeight();
            double z = entity.getZ() + (world.random.nextDouble() - 0.5) * entity.getBbWidth();

            world.addParticle(ParticleTypes.HEART, x, y, z, 0, 0.1, 0);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnBanishParticles(World world, LivingEntity entity) {
        for (int i = 0; i < 8; i++) {
            double x = entity.getX() + (world.random.nextDouble() - 0.5) * entity.getBbWidth();
            double y = entity.getY() + world.random.nextDouble() * entity.getBbHeight();
            double z = entity.getZ() + (world.random.nextDouble() - 0.5) * entity.getBbWidth();

            world.addParticle(ParticleTypes.SOUL, x, y, z,
                    (world.random.nextDouble() - 0.5) * 0.1,
                    world.random.nextDouble() * 0.1,
                    (world.random.nextDouble() - 0.5) * 0.1
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnAuraParticles(World world, LivingEntity entity, double radius) {
        int particleCount = 16;
        double angleStep = 2 * Math.PI / particleCount;

        for (int i = 0; i < particleCount; i++) {
            double angle = i * angleStep + world.getGameTime() * 0.05;
            double x = entity.getX() + Math.cos(angle) * radius;
            double z = entity.getZ() + Math.sin(angle) * radius;
            double y = entity.getY() + 0.1;

            BasicParticleType particleType = ParticleTypes.END_ROD;
            switch (silmarilsType) {
                case VARDA:
                    particleType = ParticleTypes.END_ROD;
                    break;
                case EARENDIL:
                    particleType = ParticleTypes.BUBBLE;
                    break;
                case MAEDHROS:
                    particleType = ParticleTypes.FLAME;
                    break;
            }

            world.addParticle(particleType, x, y, z, 0, 0.01, 0);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new TranslationTextComponent("item.lotrfa.silmaril." + silmarilsType.getName() + ".desc").withStyle(TextFormatting.GOLD, TextFormatting.ITALIC));

        tooltip.add(new StringTextComponent(""));

        tooltip.add(new TranslationTextComponent("item.lotrfa.silmaril.property.eternal_light").withStyle(TextFormatting.YELLOW));

        tooltip.add(new TranslationTextComponent("item.lotrfa.silmaril.property.holy").withStyle(TextFormatting.AQUA));

        tooltip.add(new TranslationTextComponent("item.lotrfa.silmaril.property.burns_evil").withStyle(TextFormatting.RED));

        tooltip.add(new TranslationTextComponent("item.lotrfa.silmaril.property.indestructible").withStyle(TextFormatting.LIGHT_PURPLE));

        tooltip.add(new StringTextComponent(""));

        tooltip.add(new TranslationTextComponent("item.lotrfa.silmaril.warning").withStyle(TextFormatting.DARK_RED, TextFormatting.ITALIC));
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, PlayerEntity player) {
        if (player.isCreative()) {
            return super.onDroppedByPlayer(item, player);
        }

        player.displayClientMessage(new TranslationTextComponent("item.lotrfa.silmaril.cannot_drop").withStyle(TextFormatting.RED), true);

        return false;
    }

    @Override
    public int getEntityLifespan(ItemStack itemStack, World world) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isFireResistant() {
        return true;
    }
}