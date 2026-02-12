package lotrfa.common.init;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import lotr.common.init.LOTRTags;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.IItemTier;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.LazyValue;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.common.Tags;

public enum LOTRFAMaterial {
    BRONZE("bronze", 2, 230, 5.0F, 1.5F, 10, 0.65F, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, () -> Ingredient.of(LOTRTags.Items.INGOTS_BRONZE), () -> Ingredient.of(LOTRTags.Items.INGOTS_BRONZE), new Specials[0]),

    FUR("fur", 0, 180, 0.0F, 0.0F, 8, 0.65F, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, () -> Ingredient.of(LOTRTags.Items.FURS), () -> Ingredient.of(LOTRTags.Items.FURS), new Specials[0]),

    BONE("bone", 0, 150, 0.0F, 0.0F, 10, 0.3F, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, () -> Ingredient.of(Tags.Items.BONES), () -> Ingredient.of(Tags.Items.BONES), new Specials[0]);

    private static final int[] ARMOR_DURABILITY_ARRAY;

    private final String materialName;

    private AsTool asTool;

    private final int harvestLevel;

    private final int maxUses;

    private final float efficiency;

    private final float attackDamage;

    private final int enchantability;

    private final LazyValue toolRepairMaterial;

    private AsArmor asArmor;

    private final int armorDurabilityFactor;

    private final int[] armorProtectionArray;

    private final SoundEvent armorSoundEvent;

    private final float toughness;

    private final LazyValue armorRepairMaterial;

    private final Set specialProperties;

    static {
        ARMOR_DURABILITY_ARRAY = new int[] { 3600, 7200, 10800, 3600 };
    }

    LOTRFAMaterial(String name, int lvl, int uses, float eff, float atk, int ench, float pr, SoundEvent sound, float tough, Supplier repair, Supplier armorRepair, Specials... specs) {
        this.specialProperties = new HashSet();
        this.materialName = "lotrfa:" + name;
        this.harvestLevel = lvl;
        this.maxUses = uses;
        this.efficiency = eff;
        this.attackDamage = atk;
        this.enchantability = ench;
        this.toolRepairMaterial = new LazyValue(repair);
        this.armorDurabilityFactor = Math.round(this.maxUses * 0.06F);
        this.armorProtectionArray = ArmorHelper.getArmorProtectionArray(pr);
        this.armorSoundEvent = sound;
        this.toughness = tough;
        this.armorRepairMaterial = new LazyValue(armorRepair);
        Specials[] var15 = specs;
        int var16 = specs.length;
        this.specialProperties.addAll(Arrays.<Specials>asList(var15).subList(0, var16));
    }

    public AsArmor asArmor() {
        if (this.asArmor == null)
            this.asArmor = new AsArmor(this);
        return this.asArmor;
    }

    public AsTool asTool() {
        if (this.asTool == null)
            this.asTool = new AsTool(this);
        return this.asTool;
    }

    public static Optional ifLOTRArmorMaterial(IArmorMaterial material) {
        return (material instanceof AsArmor) ? Optional.<AsArmor>of((AsArmor)material) : Optional.empty();
    }

    public static Optional ifLOTRToolMaterial(IItemTier material) {
        return (material instanceof AsTool) ? Optional.<AsTool>of((AsTool)material) : Optional.empty();
    }

    private static class ArmorHelper {
        private static final float[] ARMOR_PART_WEIGHTING = new float[] { 0.14F, 0.28F, 0.4F, 0.14F };

        public static int[] getArmorProtectionArray(float protection) {
            return new int[] { 2, 5, 6, 2 };
        }
    }

    public static class AsArmor implements IArmorMaterial {
        private final LOTRFAMaterial materialReference;

        public AsArmor(LOTRFAMaterial m) {
            this.materialReference = m;
        }

        public int getDefenseForSlot(EquipmentSlotType slot) {
            return this.materialReference.armorProtectionArray[slot.getIndex()];
        }

        public int getDurabilityForSlot(EquipmentSlotType slot) {
            return LOTRFAMaterial.ARMOR_DURABILITY_ARRAY[slot.getIndex()];
        }

        public int getEnchantmentValue() {
            return this.materialReference.enchantability;
        }

        public SoundEvent getEquipSound() {
            return this.materialReference.armorSoundEvent;
        }

        public float getKnockbackResistance() {
            return 0.0F;
        }

        public String getName() {
            return this.materialReference.materialName;
        }

        public Ingredient getRepairIngredient() {
            try {
                return (Ingredient)this.materialReference.armorRepairMaterial.get();
            } catch (Exception e) {
                return Ingredient.EMPTY;
            }
        }

        public float getToughness() {
            return this.materialReference.toughness;
        }

        public boolean isUndamageable() {
            return this.materialReference.specialProperties.contains(LOTRFAMaterial.Specials.UNDAMAGEABLE);
        }
    }

    public static class AsTool implements IItemTier {
        private final LOTRFAMaterial materialReference;

        public AsTool(LOTRFAMaterial m) {
            this.materialReference = m;
        }

        public boolean canHarvestManFlesh() {
            return this.materialReference.specialProperties.contains(LOTRFAMaterial.Specials.MAN_FLESH);
        }

        public float getAttackDamageBonus() {
            return this.materialReference.attackDamage;
        }

        public int getEnchantmentValue() {
            return this.materialReference.enchantability;
        }

        public int getLevel() {
            return this.materialReference.harvestLevel;
        }

        public Ingredient getRepairIngredient() {
            try {
                return (Ingredient)this.materialReference.toolRepairMaterial.get();
            } catch (Exception e) {
                return Ingredient.EMPTY;
            }
        }

        public float getSpeed() {
            return this.materialReference.efficiency;
        }

        public int getUses() {
            return this.materialReference.maxUses;
        }
    }

    public enum Specials {
        MAN_FLESH, UNDAMAGEABLE;
    }

    @Nullable
    public static LOTRFAMaterial byName(String name) {
        return Arrays.<LOTRFAMaterial>stream(values()).filter(LOTRFAMaterial -> LOTRFAMaterial.materialName.equals(name)).findFirst().orElse(null);
    }
}

