package com.breakinblocks.directorscut.anchor;

import com.breakinblocks.directorscut.item.DirectorsCameraItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class AnchorBlock extends BaseEntityBlock {
    public static final MapCodec<AnchorBlock> CODEC = simpleCodec(AnchorBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public AnchorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AnchorBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return canSee(context) ? Shapes.block() : Shapes.empty();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide || !(level.getBlockEntity(pos) instanceof AnchorBlockEntity anchor)) {
            return InteractionResult.SUCCESS;
        }
        player.sendSystemMessage(describe(anchor));
        return InteractionResult.CONSUME;
    }

    public static Component describe(AnchorBlockEntity anchor) {
        String id = anchor.getAnchorId().isEmpty() ? "(unset)" : anchor.getAnchorId();
        Component base = Component.translatable("directorscut.anchor.info", id, anchor.getFacing().getName()).withStyle(ChatFormatting.AQUA);
        if (!anchor.hasTrigger()) {
            return base;
        }
        return base.copy().append("\n").append(Component.translatable("directorscut.anchor.trigger_info", anchor.getCutsceneId(), anchor.getTriggerRadius(), anchor.isTriggerOnce(), anchor.getCooldownTicks()).withStyle(ChatFormatting.GRAY));
    }

    public static boolean canSee(CollisionContext context) {
        if (context instanceof EntityCollisionContext entityContext && entityContext.getEntity() instanceof Player player) {
            return canSee(player);
        }
        return false;
    }

    public static boolean canSee(Player player) {
        return player.isCreative() || DirectorsCameraItem.heldCamera(player) != null;
    }
}
