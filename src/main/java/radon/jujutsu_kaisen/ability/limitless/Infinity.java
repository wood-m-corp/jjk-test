package radon.jujutsu_kaisen.ability.limitless;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;
import radon.jujutsu_kaisen.entity.projectile.base.JujutsuProjectile;
import radon.jujutsu_kaisen.entity.curse.KuchisakeOnnaEntity;
import radon.jujutsu_kaisen.entity.projectile.ThrownChainProjectile;
import radon.jujutsu_kaisen.item.JJKItems;
import radon.jujutsu_kaisen.util.HelperMethods;

import java.util.*;

public class Infinity extends Ability implements Ability.IToggled {
    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return true;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.TOGGLED;
    }

    @Override
    public void run(LivingEntity owner) {

    }

    @Override
    public float getCost(LivingEntity owner) {
        return 1.0F;
    }

    @Override
    public void onEnabled(LivingEntity owner) {

    }

    @Override
    public void onDisabled(LivingEntity owner) {

    }


    public static class FrozenProjectileData extends SavedData {
        public static final String IDENTIFIER = "frozen_projectile_data";

        private final Map<UUID, FrozenProjectileNBT> frozen;

        public FrozenProjectileData() {
            this.frozen = new HashMap<>();
        }

        public static FrozenProjectileData load(CompoundTag pCompoundTag) {
            FrozenProjectileData data = new FrozenProjectileData();
            ListTag frozenTag = pCompoundTag.getList("frozen", Tag.TAG_COMPOUND);

            for (Tag tag : frozenTag) {
                FrozenProjectileNBT nbt = new FrozenProjectileNBT((CompoundTag) tag);
                data.frozen.put(nbt.getTarget(), nbt);
            }
            return data;
        }

        @Override
        public @NotNull CompoundTag save(CompoundTag pCompoundTag) {
            ListTag frozenTag = new ListTag();
            frozenTag.addAll(this.frozen.values());
            pCompoundTag.put("frozen", frozenTag);
            return pCompoundTag;
        }

        public void add(LivingEntity source, Projectile target) {
            if (!this.frozen.containsKey(target.getUUID())) {
                this.frozen.put(target.getUUID(), new FrozenProjectileNBT(source, target));
                this.setDirty();
            }
        }

        public void tick(ServerLevel level) {
            Iterator<FrozenProjectileNBT> iter = this.frozen.values().iterator();

            while (iter.hasNext()) {
                FrozenProjectileNBT nbt = iter.next();

                Entity projectile = level.getEntity(nbt.getTarget());

                if (!(level.getEntity(nbt.getSource()) instanceof LivingEntity owner)) {
                    if (projectile != null) {
                        projectile.setDeltaMovement(nbt.getMovement());
                        projectile.setNoGravity(nbt.isNoGravity());
                    }
                    iter.remove();
                    this.setDirty();
                    continue;
                }

                if (projectile == null) {
                    iter.remove();
                    this.setDirty();
                } else {
                    if (JJKAbilities.hasToggled(owner, JJKAbilities.INFINITY.get()) && owner.distanceTo(projectile) < 2.5F) {
                        Vec3 original = nbt.getMovement();
                        projectile.setDeltaMovement(original.scale(Double.MIN_VALUE));
                        projectile.setNoGravity(true);
                    } else {
                        projectile.setDeltaMovement(nbt.getMovement());
                        projectile.setNoGravity(nbt.isNoGravity());
                        iter.remove();
                        this.setDirty();
                    }
                }
            }
        }

        private static class FrozenProjectileNBT extends CompoundTag {
            public FrozenProjectileNBT(LivingEntity source, Projectile target) {
                this.putUUID("source", source.getUUID());
                this.putUUID("target", target.getUUID());

                this.putBoolean("no_gravity", target.isNoGravity());

                Vec3 movement = target.getDeltaMovement();
                this.putDouble("movement_x", movement.x);
                this.putDouble("movement_y", movement.y);
                this.putDouble("movement_z", movement.z);
            }

            public FrozenProjectileNBT(CompoundTag nbt) {
                this.putUUID("source", nbt.getUUID("source"));
                this.putUUID("target", nbt.getUUID("target"));

                this.putBoolean("no_gravity", nbt.getBoolean("no_gravity"));

                this.putDouble("movement_x", nbt.getDouble("movement_x"));
                this.putDouble("movement_y", nbt.getDouble("movement_y"));
                this.putDouble("movement_z", nbt.getDouble("movement_z"));
            }

            public UUID getSource() {
                return this.getUUID("source");
            }

            public UUID getTarget() {
                return this.getUUID("target");
            }

            public boolean isNoGravity() {
                return this.getBoolean("no_gravity");
            }

            public Vec3 getMovement() {
                double x = this.getDouble("movement_x");
                double y = this.getDouble("movement_y");
                double z = this.getDouble("movement_z");
                return new Vec3(x, y, z);
            }
        }
    }

    private static boolean canBlock(LivingEntity owner, Projectile projectile) {
        if (projectile.getOwner() == owner) return false;

        if (projectile instanceof ThrownChainProjectile chain) {
            if (chain.getStack().is(JJKItems.INVERTED_SPEAR_OF_HEAVEN.get())) return false;
        }

        for (KuchisakeOnnaEntity curse : owner.level().getEntitiesOfClass(KuchisakeOnnaEntity.class, AABB.ofSize(owner.position(),
                KuchisakeOnnaEntity.RANGE, KuchisakeOnnaEntity.RANGE, KuchisakeOnnaEntity.RANGE))) {
            Optional<UUID> identifier = curse.getCurrent();
            if (identifier.isEmpty()) continue;
            if (identifier.get() == owner.getUUID() && projectile.getOwner() == curse) return false;
        }

        if (projectile instanceof JujutsuProjectile jujutsu) {
            return !jujutsu.isDomain();
        }
        return true;
    }

    @Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class InfinityForgeEvents {
        @SubscribeEvent
        public static void onLevelTick(TickEvent.LevelTickEvent event) {
            if (event.phase == TickEvent.Phase.START) return;

            if (event.level instanceof ServerLevel level) {
                FrozenProjectileData data = level.getDataStorage().computeIfAbsent(FrozenProjectileData::load, FrozenProjectileData::new,
                        FrozenProjectileData.IDENTIFIER);
                data.tick(level);
            }
        }

        @SubscribeEvent
        public static void onProjectileImpact(ProjectileImpactEvent event) {
            if (!(event.getRayTraceResult() instanceof EntityHitResult hit)) return;
            if (!(hit.getEntity() instanceof LivingEntity owner)) return;
            if (!(owner.level() instanceof ServerLevel level)) return;

            if (!JJKAbilities.hasToggled(owner, JJKAbilities.INFINITY.get())) return;

            FrozenProjectileData data = level.getDataStorage().computeIfAbsent(FrozenProjectileData::load, FrozenProjectileData::new,
                    FrozenProjectileData.IDENTIFIER);

            Projectile projectile = event.getProjectile();

            if (!Infinity.canBlock(owner, projectile)) return;

            data.add(owner, projectile);

            event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onLivingTick(LivingEvent.LivingTickEvent event) {
            LivingEntity target = event.getEntity();

            if (!(target.level() instanceof ServerLevel level)) return;

            FrozenProjectileData data = level.getDataStorage().computeIfAbsent(FrozenProjectileData::load, FrozenProjectileData::new,
                    FrozenProjectileData.IDENTIFIER);

            if (!JJKAbilities.hasToggled(target, JJKAbilities.INFINITY.get())) return;

            for (Projectile projectile : target.level().getEntitiesOfClass(Projectile.class, target.getBoundingBox().inflate(1.0D))) {
                if (!Infinity.canBlock(target, projectile)) continue;

                data.add(target, projectile);
            }
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onLivingAttack(LivingAttackEvent event) {
            LivingEntity target = event.getEntity();

            if (target.level().isClientSide) return;

            if (!JJKAbilities.hasToggled(target, JJKAbilities.INFINITY.get())) return;

            DamageSource source = event.getSource();

            if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;

            if (source.getDirectEntity() instanceof Projectile projectile && !canBlock(target, projectile)) return;

            if (source.getDirectEntity() instanceof DomainExpansionEntity) return;

            if (source.getEntity() == target) return;

            if (source.getEntity() instanceof LivingEntity living && HelperMethods.isMelee(source)) {
                if (JJKAbilities.hasToggled(living, JJKAbilities.DOMAIN_AMPLIFICATION.get())) {
                    return;
                }
            }

            // We don't want to play the sound in-case it's a stopped projectile
            if (!(source.getDirectEntity() instanceof Projectile)) {
                target.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.MASTER, 1.0F, 1.0F);
            }
            event.setCanceled(true);
        }
    }
}
