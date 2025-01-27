package radon.jujutsu_kaisen.ability.disaster_tides;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.entity.effect.WaterTorrentEntity;
import radon.jujutsu_kaisen.entity.projectile.EelGrappleProjectile;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;


public class WaterTorrent extends Ability {
    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return HelperMethods.RANDOM.nextInt(3) == 0 && target != null && owner.hasLineOfSight(target) && owner.distanceTo(target) <= WaterTorrentEntity.RANGE;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.INSTANT;
    }

    @Override
    public void run(LivingEntity owner) {
        owner.swing(InteractionHand.MAIN_HAND);

        WaterTorrentEntity torrent = new WaterTorrentEntity(owner, this.getPower(owner));
        owner.level().addFreshEntity(torrent);
    }

    @Override
    public int getCooldown() {
        return 7 * 20;
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 75.0F;
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.MELEE;
    }

    @Override
    public Classification getClassification() {
        return Classification.WATER;
    }
}
