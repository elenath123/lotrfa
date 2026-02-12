package lotrfa.common.entity.misc;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;
import lotr.common.data.AlignmentDataModule;
import lotr.common.fac.AlignmentBonus;
import lotr.common.fac.AlignmentBonusMap;
import lotr.common.fac.Faction;
import lotr.common.init.LOTREntities;

public class AlignmentBonusEntity extends Entity {
    private AlignmentBonus bonusSource;

    private Faction mainFaction;

    private float prevMainAlignment;

    private AlignmentBonusMap factionBonusMap;

    private float conquestBonus;

    private int particleAge;

    private int particlePrevAge;

    private int particleMaxAge;

    public AlignmentBonusEntity(EntityType type, World w) {
        super(type, w);
        this.particlePrevAge = this.particleAge = 0;
    }

    protected void addAdditionalSaveData(CompoundNBT nbt) {}

    private void calcMaxAge() {
        float mostSignificantBonus = 0.0F;
        Iterator<Faction> var2 = this.factionBonusMap.getChangedFactions().iterator();
        while (var2.hasNext()) {
            Faction fac = var2.next();
            float bonus = Math.abs(((Float)this.factionBonusMap.get(fac)).floatValue());
            if (bonus > mostSignificantBonus)
                mostSignificantBonus = bonus;
        }
        float conq = Math.abs(this.conquestBonus);
        if (conq > mostSignificantBonus)
            mostSignificantBonus = conq;
        this.particleMaxAge = 80;
        int extra = (int)(Math.min(1.0F, mostSignificantBonus / 50.0F) * 220.0F);
        this.particleMaxAge += extra;
    }

    protected void defineSynchedData() {}

    public IPacket getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public float getAlignmentBonusFor(Faction faction) {
        return ((Float)this.factionBonusMap.getOrDefault(faction, Float.valueOf(0.0F))).floatValue();
    }

    public float getBonusAgeF(float f) {
        return (this.particlePrevAge + (this.particleAge - this.particlePrevAge) * f) / this.particleMaxAge;
    }

    public ITextComponent getBonusDisplayText() {
        return this.bonusSource.name;
    }

    public float getConquestBonus() {
        return this.conquestBonus;
    }

    public Faction getFactionToDisplay(AlignmentDataModule alignData) {
        if (!this.factionBonusMap.isEmpty()) {
            Faction currentViewedFaction = alignData.getCurrentViewedFaction();
            if (this.factionBonusMap.containsKey(currentViewedFaction))
                return currentViewedFaction;
            if (this.factionBonusMap.size() == 1 && this.mainFaction.isPlayableAlignmentFaction())
                return this.mainFaction;
            if (this.mainFaction.isPlayableAlignmentFaction() && this.prevMainAlignment >= 0.0F && ((Float)this.factionBonusMap.get(this.mainFaction)).floatValue() < 0.0F)
                return this.mainFaction;
            Optional<Faction> highestFactionWithBonus = this.factionBonusMap.keySet().stream().filter(hummel -> ((Faction)hummel).isPlayableAlignmentFaction()).filter(fac -> (((Float)this.factionBonusMap.get(fac)).floatValue() > 0.0F)).sorted(Comparator.comparingDouble(fac -> alignData.getAlignment((Faction)fac)).reversed()).findFirst();
            if (highestFactionWithBonus.isPresent())
                return highestFactionWithBonus.get();
            if (this.mainFaction.isPlayableAlignmentFaction() && ((Float)this.factionBonusMap.get(this.mainFaction)).floatValue() < 0.0F)
                return this.mainFaction;
            Optional<Faction> highestFactionWithPenalty = this.factionBonusMap.keySet().stream().filter(hummel -> ((Faction)hummel).isPlayableAlignmentFaction()).filter(fac -> (((Float)this.factionBonusMap.get(fac)).floatValue() < 0.0F)).sorted(Comparator.comparingDouble(fac -> alignData.getAlignment((Faction)fac)).reversed()).findFirst();
            if (highestFactionWithPenalty.isPresent())
                return highestFactionWithPenalty.get();
        }
        return null;
    }

    public boolean isInvulnerable() {
        return true;
    }

    protected boolean isMovementNoisy() {
        return false;
    }

    public boolean isPushable() {
        return false;
    }

    protected void readAdditionalSaveData(CompoundNBT nbt) {}

    public boolean shouldDisplayConquestBonus(AlignmentDataModule alignData) {
        Faction currentViewedFaction = alignData.getCurrentViewedFaction();
        if (this.conquestBonus > 0.0F && alignData.isPledgedTo(currentViewedFaction))
            return true;
        return (this.conquestBonus < 0.0F && (currentViewedFaction == this.mainFaction || alignData.isPledgedTo(currentViewedFaction)));
    }

    public boolean shouldShowBonusText(boolean showAlign, boolean showConquest) {
        return (showAlign || (showConquest && !this.bonusSource.isKillByHiredUnit));
    }

    public void tick() {
        super.tick();
        this.particlePrevAge = this.particleAge++;
        if (this.particleAge >= this.particleMaxAge)
            removeAfterChangingDimensions();
    }

    public static AlignmentBonusEntity createBonusEntityForClientSpawn(World world, int entityId, AlignmentBonus bonusSource, Faction mainFaction, float prevMainAlignment, AlignmentBonusMap factionBonusMap, float conquestBonus, Vector3d pos) {
        if (!world.isClientSide)
            throw new IllegalArgumentException("Alignment bonus entities cannot be spawned on the server side!");
        AlignmentBonusEntity entity = new AlignmentBonusEntity((EntityType) LOTREntities.ALIGNMENT_BONUS.get(), world);
        entity.setId(entityId);
        entity.bonusSource = bonusSource;
        entity.mainFaction = mainFaction;
        entity.prevMainAlignment = prevMainAlignment;
        entity.factionBonusMap = factionBonusMap;
        entity.conquestBonus = conquestBonus;
        entity.setPos(pos.x, pos.y, pos.z);
        entity.calcMaxAge();
        return entity;
    }

    public static int getNextSafeEntityIdForBonusSpawn(ServerWorld world) {
        AlignmentBonusEntity entity = new AlignmentBonusEntity((EntityType)LOTREntities.ALIGNMENT_BONUS.get(), (World)world);
        return entity.getId();
    }
}
