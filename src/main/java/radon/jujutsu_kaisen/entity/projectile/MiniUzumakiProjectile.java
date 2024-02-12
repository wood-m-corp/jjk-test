package radon.jujutsu_kaisen.entity.projectile;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererGrade;
import radon.jujutsu_kaisen.entity.JJKEntities;
import radon.jujutsu_kaisen.entity.effect.base.BeamEntity;
import radon.jujutsu_kaisen.entity.curse.base.CursedSpirit;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;
import radon.jujutsu_kaisen.util.SorcererUtil;

public class MiniUzumakiProjectile extends BeamEntity {
    public static final double RANGE = 24.0D;
    private static final float MAX_POWER = 10.0F;
    public MiniUzumakiProjectile(EntityType<? extends Projectile> pType, Level pLevel) {
        super(pType, pLevel);

        this.noCulling = true;
    }

    public MiniUzumakiProjectile(LivingEntity owner, float power) {
        this(JJKEntities.MINI_UZUMAKI.get(), owner.level());

        this.setOwner(owner);
        this.setPower(power);

        ISorcererData ownerCap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        Entity weakest = null;

        for (Entity current : ownerCap.getSummons()) {
            if (!(current instanceof CursedSpirit)) continue;

            if (weakest == null) {
                weakest = current;
                continue;
            }

            ISorcererData weakestCap = weakest.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
            ISorcererData currentCap = current.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

            if (SorcererUtil.getGrade(currentCap.getExperience()).ordinal() < SorcererUtil.getGrade(weakestCap.getExperience()).ordinal()) weakest = current;
        }

        if (weakest != null) {
            ISorcererData weakestCap = weakest.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
            float addPower = SorcererUtil.getPower(weakestCap.getExperience());
            if (addPower < 2.90F) {
                addPower/=2.5F;
            }
            if (addPower > 3.85F) {
                addPower*=1.75F;
            }
            addPower/=1.5F;
            this.setPower(Math.min(MAX_POWER,addPower + power) );
            
            if (SorcererUtil.getGrade(weakestCap.getExperience()).ordinal() >= SorcererGrade.SEMI_GRADE_1.ordinal() && weakestCap.getTechnique() != null) {
                ownerCap.absorb(weakestCap.getTechnique());

                if (owner instanceof ServerPlayer player) {
                    PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(ownerCap.serializeNBT()), player);
                }
            }
            weakest.discard();
        }
    }

    @Override
    public int getFrames() {
        return 3;
    }

    @Override
    public float getScale() {
        return 1.0F;
    }

    @Override
    protected double getRange() {
        return RANGE;
    }

    @Override
    protected float getDamage() {
        return 11.25F;
    }

    @Override
    protected int getDuration() {
        return 20;
    }

    @Override
    public int getCharge() {
        return 10;
    }

    @Override
    protected @Nullable Ability getSource() {
        return JJKAbilities.MINI_UZUMAKI.get();
    }
}
