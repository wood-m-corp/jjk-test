package radon.jujutsu_kaisen.capability.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.CurseGrade;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererGrade;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;
import radon.jujutsu_kaisen.entity.base.SummonEntity;

import java.util.List;
import java.util.function.Consumer;

public interface ISorcererData {
    void tick(LivingEntity owner);

    List<DomainExpansionEntity> getDomains(ServerLevel level);
    void onInsideDomain(DomainExpansionEntity domain);

    boolean hasToggled(Ability ability);

    @Nullable CursedTechnique getTechnique();
    void setTechnique(@Nullable CursedTechnique technique);

    SorcererGrade getGrade();
    void setGrade(SorcererGrade grade);

    boolean hasTrait(Trait trait);
    void addTrait(Trait trait);
    void addTraits(List<Trait> traits);
    void removeTrait(Trait trait);

    void setCurse(boolean curse);
    boolean isCurse();

    void exorcise(LivingEntity owner, CurseGrade grade);

    void toggle(LivingEntity owner, Ability ability);
    void clearToggled();

    void addCooldown(LivingEntity owner, Ability ability);
    int getRemainingCooldown(Ability ability);
    boolean isCooldownDone(Ability ability);

    void addDuration(LivingEntity owner, Ability ability);

    void setBurnout(int duration);
    int getBurnout();
    boolean hasBurnout();

    void resetCooldowns();
    void resetBurnout();

    float getEnergy();
    float getMaxEnergy();
    void useEnergy(float amount);
    void setEnergy(float energy);

    void onBlackFlash(LivingEntity owner);
    long getLastBlackFlashTime();
    boolean isInZone(LivingEntity owner);

    void delayTickEvent(Consumer<LivingEntity> task, int delay);

    void setCopied(@Nullable CursedTechnique technique);
    @Nullable CursedTechnique getCopied();

    void channel(LivingEntity owner, @Nullable Ability ability);
    boolean isChanneling(Ability ability);

    <T extends SummonEntity> void addSummon(T entity);
    <T extends SummonEntity> void unsummonByClass(ServerLevel level, Class<T> clazz);
    <T extends SummonEntity> boolean hasSummonOfClass(ServerLevel level, Class<T> clazz);
    <T extends SummonEntity> @Nullable T getSummonByClass(ServerLevel level, Class<T> clazz);

    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag nbt);
}
