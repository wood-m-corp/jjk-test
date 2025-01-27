package radon.jujutsu_kaisen.entity;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import radon.jujutsu_kaisen.VeilHandler;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.client.particle.ParticleColors;
import radon.jujutsu_kaisen.client.particle.VaporParticle;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;
import radon.jujutsu_kaisen.util.HelperMethods;

import javax.annotation.Nullable;
import java.util.UUID;

public class SimpleDomainEntity extends Entity {
    private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(SimpleDomainEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_MAX_HEALTH = SynchedEntityData.defineId(SimpleDomainEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_HEALTH = SynchedEntityData.defineId(SimpleDomainEntity.class, EntityDataSerializers.FLOAT);

    private static final float STRENGTH = 100.0F;
    private static final double X_STEP = 0.025D;
    public static final float RADIUS = 2.0F;
    private static final float MAX_RADIUS = 4.0F;
    private static final float DAMAGE = 3.0F;
    private boolean invuln = false;
    private boolean domainInvuln = false;

    @Nullable
    private UUID ownerUUID;
    @Nullable
    private LivingEntity cachedOwner;

    protected SimpleDomainEntity(EntityType<?> pType, Level pLevel) {
        super(pType, pLevel);
    }

    public SimpleDomainEntity(LivingEntity owner) {
        super(JJKEntities.SIMPLE_DOMAIN.get(), owner.level());

        this.setOwner(owner);

        this.setPos(owner.position());

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        this.setRadius(Math.min(MAX_RADIUS, RADIUS * cap.getAbilityPower()));
        this.setMaxHealth(STRENGTH * cap.getAbilityPower());
        this.setHealth(this.entityData.get(DATA_MAX_HEALTH));
    }

    public float getRadius() {
        return this.entityData.get(DATA_RADIUS);
    }

    private void setRadius(float radius) {
        this.entityData.set(DATA_RADIUS, radius);
    }

    private float getMaxHealth() {
        return this.entityData.get(DATA_MAX_HEALTH);
    }

    private void setMaxHealth(float maxHealth) {
        this.entityData.set(DATA_MAX_HEALTH, maxHealth);
    }

    private float getHealth() {
        return this.entityData.get(DATA_HEALTH);
    }

    private void setHealth(float health) {
        this.entityData.set(DATA_HEALTH, Mth.clamp(health, 0.0F, this.getMaxHealth()));
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pPose) {
        float radius = (float) (this.getRadius() * 2.0D);
        return EntityDimensions.fixed(radius, radius);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_RADIUS, 0.0F);
        this.entityData.define(DATA_MAX_HEALTH, 0.0F);
        this.entityData.define(DATA_HEALTH, 0.0F);
    }

    @Override
    public @NotNull Vec3 getDeltaMovement() {
        return Vec3.ZERO;
    }

    //@Override
    public boolean hurt(@NotNull DamageSource pSource, float pAmount, boolean isDomainAttack) {
        if (!isDomainAttack && invuln) return false;
        if (isDomainAttack && domainInvuln) return false;
        if ((pSource.getEntity() instanceof LivingEntity attacker)) {
            if (attacker == this.getOwner()) {
                return false;
            }
        }
        ISorcererData cap = this.getOwner().getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        if (isDomainAttack) {
            this.domainInvuln = true;
            cap.delayTickEvent(() -> {
                this.domainInvuln = false;
            }, 10);
        }
        if (!isDomainAttack) {
            this.invuln = true;
            cap.delayTickEvent(() -> {
                this.invuln = false;
            }, 5);
        }
        this.setHealth(this.getHealth() - pAmount);
        return true;
    }

    @Override
    public void tick() {
        if (this.getHealth() == 0.0F) {
            this.discard();
            return;
        }

        this.refreshDimensions();

        LivingEntity owner = this.getOwner();

        if (!this.level().isClientSide && (owner == null || owner.isRemoved() || !owner.isAlive() || !JJKAbilities.hasToggled(owner, JJKAbilities.SIMPLE_DOMAIN.get()))) {
            this.discard();
        } else if (owner != null) {
            super.tick();

            this.setPos(owner.position());

            if (this.level() instanceof ServerLevel level) {
                ISorcererData ownerCap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

                for (DomainExpansionEntity domain : VeilHandler.getDomains(level, owner.blockPosition())) {
                    if (domain.checkSureHitEffect()) {
                        LivingEntity target = domain.getOwner();
                        float baseDMG = DAMAGE;
                        if (domain.ability == JJKAbilities.UNLIMITED_VOID.get() || domain.ability == JJKAbilities.SELF_EMBODIMENT_OF_PERFECTION.get()) {
                            baseDMG*=0.5F;
                        } 
                        if (domain.ability == JJKAbilities.MALEVOLENT_SHRINE.get()) {
                            baseDMG*=7.0F;
                        } 
                        if (target != null) {
                            ISorcererData targetCap = target.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
                            this.hurt(JJKDamageSources.indirectJujutsuAttack(domain, target, null), baseDMG * (1.0F + Math.max(0.0F, targetCap.getAbilityPower() - ownerCap.getAbilityPower())),true);
                        }
                    }
                }
            }

            float factor = (this.getHealth() / this.getMaxHealth()) * 2.0F;

            ParticleOptions particle = new VaporParticle.VaporParticleOptions(ParticleColors.SIMPLE_DOMAIN, (float) (this.getRadius() * 0.25D),
                    1.0F, true, 1);

            for (double phi = 0.0D; phi < Math.PI * factor; phi += X_STEP) {
                double x = this.getX() + this.getRadius() * Math.cos(phi);
                double y = this.getY();
                double z = this.getZ() + this.getRadius() * Math.sin(phi);
                this.level().addParticle(particle, x, y, z, 0.0D, HelperMethods.RANDOM.nextDouble(), 0.0D);
            }
        }
    }

    public void setOwner(@Nullable LivingEntity pOwner) {
        if (pOwner != null) {
            this.ownerUUID = pOwner.getUUID();
            this.cachedOwner = pOwner;
        }
    }

    @Nullable
    public LivingEntity getOwner() {
        if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
            return this.cachedOwner;
        } else if (this.ownerUUID != null && this.level() instanceof ServerLevel) {
            this.cachedOwner = (LivingEntity) ((ServerLevel) this.level()).getEntity(this.ownerUUID);
            return this.cachedOwner;
        } else {
            return null;
        }
    }

    @Override
    public boolean isInWall() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        if (this.ownerUUID != null) {
            pCompound.putUUID("owner", this.ownerUUID);
        }
        pCompound.putFloat("radius", this.getRadius());
        pCompound.putFloat("max_health", this.getMaxHealth());
        pCompound.putFloat("health", this.getHealth());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        if (pCompound.hasUUID("owner")) {
            this.ownerUUID = pCompound.getUUID("owner");
        }
        this.setRadius(pCompound.getFloat("radius"));
        this.setMaxHealth(pCompound.getFloat("max_health"));
        this.setHealth(pCompound.getFloat("health"));
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        Entity entity = this.getOwner();
        return new ClientboundAddEntityPacket(this, entity == null ? 0 : entity.getId());
    }

    @Override
    public void recreateFromPacket(@NotNull ClientboundAddEntityPacket pPacket) {
        super.recreateFromPacket(pPacket);

        LivingEntity owner = (LivingEntity) this.level().getEntity(pPacket.getData());

        if (owner != null) {
            this.setOwner(owner);
        }
    }
}
