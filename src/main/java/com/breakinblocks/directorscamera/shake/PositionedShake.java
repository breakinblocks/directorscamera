package com.breakinblocks.directorscamera.shake;

import com.breakinblocks.directorscamera.curves.Easings;
import com.breakinblocks.directorscamera.curves.PiecewiseEasing;
import com.breakinblocks.directorscamera.util.MathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PositionedShake implements ScreenShake {
    private final ShakeData data;
    private final PiecewiseEasing envelope;
    private final double maxDistance;
    private Vec3 pos;

    public PositionedShake(ShakeData data, Vec3 pos, double maxDistance) {
        this.data = data;
        this.pos = pos;
        this.maxDistance = maxDistance;
        this.envelope = new PiecewiseEasing()
            .addArea(data.inTime(), Easings::easeIn)
            .addArea(data.stayTime(), Easings::one)
            .addArea(data.outTime(), Easings::reversedEaseOut);
    }

    public Vec3 getPos() {
        return pos;
    }

    public void setPos(Vec3 pos) {
        this.pos = pos;
    }

    public ShakeData getData() {
        return data;
    }

    @Override
    public void process(PoseStack poseStack, int time, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }
        Vec3 camPos = minecraft.gameRenderer.getMainCamera().getPosition();
        double dist = camPos.distanceTo(pos);
        if (dist > maxDistance) {
            return;
        }
        double pdist = 2 * dist / maxDistance;
        double distStrength = Math.exp(-pdist * pdist);
        Vec3 look = player.getLookAngle();
        Vec3 left = new Vec3(0, 1, 0).cross(look);
        Vec3 proj = MathUtil.projectOntoPlane(pos.subtract(camPos), look);
        Vec3 up = look.cross(left);
        double angle = MathUtil.angleBetween(up, proj);
        if (Double.isNaN(angle)) {
            angle = 0;
        }
        if (proj.dot(left) < 0) {
            angle = -angle;
        }
        int duration = data.duration();
        float t = Mth.clamp(time + partialTicks, 0, duration);
        float p = duration == 0 ? 1 : t / duration;
        double s = 2 * Math.PI * data.frequency() * p;
        Vector3f axis = new Vector3f((float) -Math.cos(angle), (float) -Math.sin(angle), 0.0F).normalize();
        float strength = envelope.applyFloat(t);
        double degrees = Math.sin(s + Math.PI) * data.amplitude() * strength * distStrength;
        if (Double.isNaN(degrees) || degrees == 0) {
            return;
        }
        poseStack.mulPose(new Quaternionf().rotateAxis((float) Math.toRadians(degrees), axis));
    }

    @Override
    public boolean hasEnded(int elapsedTime) {
        return elapsedTime > data.duration();
    }
}
