package com.breakinblocks.directorscamera.cutscene;

import com.breakinblocks.directorscamera.anchor.AnchorIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface FrameSource {
    Optional<CutsceneFrame> resolve(ServerPlayer player);

    String describe();

    static FrameSource anchor(String anchorId, double maxDistance) {
        return new Anchor(anchorId, maxDistance);
    }

    static FrameSource fixed(CutsceneFrame frame) {
        return new Fixed(frame);
    }

    static FrameSource player() {
        return new PlayerPose(false);
    }

    static FrameSource playerEyes() {
        return new PlayerPose(true);
    }

    static FrameSource structure(@Nullable Identifier structureId) {
        return new Structure(structureId);
    }

    record Anchor(String anchorId, double maxDistance) implements FrameSource {
        @Override
        public Optional<CutsceneFrame> resolve(ServerPlayer player) {
            return AnchorIndex.nearest(player.level(), anchorId, player.position(), maxDistance);
        }

        @Override
        public String describe() {
            return "anchor " + anchorId;
        }
    }

    record Fixed(CutsceneFrame frame) implements FrameSource {
        @Override
        public Optional<CutsceneFrame> resolve(ServerPlayer player) {
            return Optional.of(frame);
        }

        @Override
        public String describe() {
            return "fixed " + frame.origin() + " yaw " + frame.yaw();
        }
    }

    record PlayerPose(boolean eyes) implements FrameSource {
        @Override
        public Optional<CutsceneFrame> resolve(ServerPlayer player) {
            Vec3 origin = eyes ? player.getEyePosition() : player.position();
            return Optional.of(new CutsceneFrame(origin, player.getYRot()));
        }

        @Override
        public String describe() {
            return eyes ? "player eyes" : "player";
        }
    }

    record Structure(@Nullable Identifier structureId) implements FrameSource {
        @Override
        public Optional<CutsceneFrame> resolve(ServerPlayer player) {
            BlockPos pos = player.blockPosition();
            for (StructureStart start : player.level().structureManager().startsForStructure(ChunkPos.containing(pos), s -> true)) {
                if (!start.getBoundingBox().isInside(pos)) {
                    continue;
                }
                if (structureId != null) {
                    Identifier id = player.level().registryAccess().lookupOrThrow(Registries.STRUCTURE).getKey(start.getStructure());
                    if (!structureId.equals(id)) {
                        continue;
                    }
                }
                return Optional.of(frameOf(start));
            }
            return Optional.empty();
        }

        private static CutsceneFrame frameOf(StructureStart start) {
            BoundingBox box = start.getBoundingBox();
            Vec3 origin = new Vec3(box.minX(), box.minY(), box.minZ());
            float yaw = 0.0F;
            for (StructurePiece piece : start.getPieces()) {
                if (piece instanceof TemplateStructurePiece template) {
                    StructurePlaceSettings settings = template.placeSettings();
                    origin = Vec3.atLowerCornerOf(template.templatePosition());
                    yaw = switch (settings.getRotation()) {
                        case NONE -> 0.0F;
                        case CLOCKWISE_90 -> 90.0F;
                        case CLOCKWISE_180 -> 180.0F;
                        case COUNTERCLOCKWISE_90 -> -90.0F;
                    };
                    break;
                }
            }
            return new CutsceneFrame(origin, yaw);
        }

        @Override
        public String describe() {
            return structureId == null ? "structure" : "structure " + structureId;
        }
    }
}
