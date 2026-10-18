package dimaag;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Modules {
    public static boolean autoCrystal, autoAnchor, nightVision, aimAssist;
    private static boolean nvWasOn = false;
    private static int cd = 0;

    public static void tick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        nightVisionTick(p);
        if (mc.currentScreen != null) return;
        if (cd > 0) cd--;
        if (aimAssist) aimAssist(mc, p);
        if (cd == 0 && autoCrystal) autoCrystal(mc, p);
        if (cd == 0 && autoAnchor) autoAnchor(mc, p);
    }

    // ---------- helpers ----------
    private static PlayerEntity target(MinecraftClient mc, double range) {
        PlayerEntity best = null;
        double bd = range * range;
        for (PlayerEntity e : mc.world.getPlayers()) {
            if (e == mc.player || e.isSpectator() || !e.isAlive()) continue;
            double d = e.squaredDistanceTo(mc.player);
            if (d < bd) { bd = d; best = e; }
        }
        return best;
    }

    private static int findHotbar(ClientPlayerEntity p, Item item) {
        for (int i = 0; i < 9; i++) if (p.getInventory().getStack(i).isOf(item)) return i;
        return -1;
    }

    private static int findHotbarNot(ClientPlayerEntity p, Item item) {
        for (int i = 0; i < 9; i++) if (!p.getInventory().getStack(i).isOf(item)) return i;
        return -1;
    }

    private static void useBlock(MinecraftClient mc, ClientPlayerEntity p, BlockPos pos, int slot) {
        p.getInventory().selectedSlot = slot;
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos).add(0, 0.5, 0), Direction.UP, pos, false);
        mc.interactionManager.interactBlock(p, Hand.MAIN_HAND, hit);
        p.swingHand(Hand.MAIN_HAND);
    }

    // ---------- Night Vision ----------
    private static void nightVisionTick(ClientPlayerEntity p) {
        if (nightVision) {
            StatusEffectInstance cur = p.getStatusEffect(StatusEffects.NIGHT_VISION);
            if (cur == null || cur.getDuration() < 600)
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1000, 0, false, false, false));
            nvWasOn = true;
        } else if (nvWasOn) {
            p.removeStatusEffect(StatusEffects.NIGHT_VISION);
            nvWasOn = false;
        }
    }

    // ---------- Aim Assist (smooth, sirf aas-paas ke player pe) ----------
    private static void aimAssist(MinecraftClient mc, ClientPlayerEntity p) {
        PlayerEntity t = target(mc, 6);
        if (t == null) return;
        Vec3d eye = p.getEyePos();
        Vec3d aim = t.getPos().add(0, t.getHeight() * 0.7, 0);
        double dx = aim.x - eye.x, dy = aim.y - eye.y, dz = aim.z - eye.z;
        float ty = (float) (MathHelper.atan2(dz, dx) * 180.0 / Math.PI) - 90f;
        float tp = (float) -(MathHelper.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * 180.0 / Math.PI);
        float dyaw = MathHelper.wrapDegrees(ty - p.getYaw());
        if (Math.abs(dyaw) > 70) return; // assist hai, snap nahi
        p.setYaw(p.getYaw() + dyaw * 0.12f);
        p.setPitch(MathHelper.clamp(p.getPitch() + (tp - p.getPitch()) * 0.12f, -90f, 90f));
    }

    // ---------- Auto Crystal ----------
    private static void autoCrystal(MinecraftClient mc, ClientPlayerEntity p) {
        PlayerEntity t = target(mc, 8);
        if (t == null) return;

        // 1) crystal tod do
        EndCrystalEntity best = null;
        double bd = Double.MAX_VALUE;
        for (EndCrystalEntity c : mc.world.getEntitiesByClass(EndCrystalEntity.class, p.getBoundingBox().expand(5), e -> true)) {
            if (p.squaredDistanceTo(c) > 20.25) continue;
            double dt = c.squaredDistanceTo(t);
            if (dt > 36) continue;
            if (dt < bd) { bd = dt; best = c; }
        }
        if (best != null) {
            mc.interactionManager.attackEntity(p, best);
            p.swingHand(Hand.MAIN_HAND);
            cd = 2;
            return;
        }

        // 2) crystal laga do (obsidian / bedrock pe)
        int slot = findHotbar(p, Items.END_CRYSTAL);
        if (slot < 0) return;
        BlockPos pp = p.getBlockPos();
        Vec3d eye = p.getEyePos();
        BlockPos base = null;
        double bestD = Double.MAX_VALUE;
        for (int x = -4; x <= 4; x++) for (int y = -3; y <= 3; y++) for (int z = -4; z <= 4; z++) {
            BlockPos pos = pp.add(x, y, z);
            BlockState st = mc.world.getBlockState(pos);
            if (!(st.isOf(Blocks.OBSIDIAN) || st.isOf(Blocks.BEDROCK))) continue;
            Vec3d center = Vec3d.ofCenter(pos);
            if (center.distanceTo(eye) > 4.5) continue;
            if (!mc.world.getBlockState(pos.up()).isAir() || !mc.world.getBlockState(pos.up(2)).isAir()) continue;
            Box box = new Box(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getY() + 3, pos.getZ() + 1);
            if (!mc.world.getOtherEntities(null, box).isEmpty()) continue;
            double dTarget = center.squaredDistanceTo(t.getPos());
            double dSelf = center.squaredDistanceTo(p.getPos());
            if (dTarget > 36 || dTarget >= dSelf) continue; // target ke paas, apne se door
            if (dTarget < bestD) { bestD = dTarget; base = pos; }
        }
        if (base != null) {
            useBlock(mc, p, base, slot);
            cd = 2;
        }
    }

    // ---------- Auto Anchor ----------
    // Anchor lagate hi glowstone se bharta hai, phir blast karta hai.
    // (Overworld / End me blast hota hai; Nether me anchor blast nahi hota.)
    private static void autoAnchor(MinecraftClient mc, ClientPlayerEntity p) {
        PlayerEntity t = target(mc, 8);
        if (t == null) return;
        BlockPos pp = p.getBlockPos();
        Vec3d eye = p.getEyePos();
        BlockPos best = null;
        double bd = Double.MAX_VALUE;
        for (int x = -4; x <= 4; x++) for (int y = -4; y <= 4; y++) for (int z = -4; z <= 4; z++) {
            BlockPos pos = pp.add(x, y, z);
            if (!mc.world.getBlockState(pos).isOf(Blocks.RESPAWN_ANCHOR)) continue;
            Vec3d center = Vec3d.ofCenter(pos);
            if (center.distanceTo(eye) > 4.5) continue;
            double dt = center.squaredDistanceTo(t.getPos());
            if (dt > 25) continue;
            if (dt < bd) { bd = dt; best = pos; }
        }
        if (best == null) return;
        int charges = mc.world.getBlockState(best).get(RespawnAnchorBlock.CHARGES);
        if (charges == 0) {
            int g = findHotbar(p, Items.GLOWSTONE);
            if (g < 0) return;
            useBlock(mc, p, best, g);
            cd = 2;
        } else {
            int s = findHotbarNot(p, Items.GLOWSTONE);
            if (s < 0) return;
            useBlock(mc, p, best, s);
            cd = 4;
        }
    }
}
