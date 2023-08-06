package club.someoneice.shaker;

import club.someoneice.shaker.gui.ContainerShaker;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ShakerItem extends Item {
    public ShakerItem() {
        super(new Properties().tab(CreativeModeTab.TAB_FOOD).stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        super.use(world, player, hand);
        var item = player.getItemInHand(hand);
        var tag = item.getOrCreateTag();
        if (player.isShiftKeyDown()) {
            if (!world.isClientSide) {
                checkRecipes(item, world);
                NetworkHooks.openGui((ServerPlayer) player, new SimpleMenuProvider((id, inv, user) -> new ContainerShaker(id, inv), new TextComponent("")));
            }
            tag.putInt("shake_count", 0);
            return InteractionResultHolder.sidedSuccess(item, world.isClientSide);
        } else {
            tag.putInt("shake_count", tag.getInt("shake_count") + 1);
            player.sendMessage(new TextComponent("Shaking times: " + tag.getInt("shake_count")), UUID.randomUUID());
            world.playSound(null, player.position().x, player.position().y, player.position().z, SoundEvents.HONEY_DRINK, SoundSource.PLAYERS, 0.5F, 2.6F + world.random.nextFloat() * 0.8F);
            return InteractionResultHolder.success(item);
        }
    }

    private void checkRecipes(ItemStack item, Level world) {
        var tag = item.getOrCreateTag();

        SimpleContainer container = new SimpleContainer(8);
        item.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(it -> {
            for (int i = 0; i < 8; i ++) container.addItem(it.getStackInSlot(i).copy());
        });

        Optional<RecipeShaker> match = world.getRecipeManager().getRecipeFor(RecipeShaker.Type.INSTANCE, container, world);
        match.ifPresent(it -> {
            if (tag.getInt("shake_count") > it.getShaking()) item.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(itm -> {
                for (int i = 0; i < 8; i ++)
                    itm.extractItem(i, 1, false);
                itm.insertItem(8, it.getResultItem().copy(), false);
            });
        });
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack item, CompoundTag nbt) {
        super.initCapabilities(item, nbt);
        return new CapabilityHandle();
    }

    @Override
    public CompoundTag getShareTag(ItemStack item) {
        var result = Objects.requireNonNullElse(super.getShareTag(item), new CompoundTag());
        item.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(it -> result.put("items", ((ItemStackHandler) it).serializeNBT()));
        return result;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        super.readShareTag(stack, nbt);
        if (nbt != null)
            stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(it -> ((ItemStackHandler) it).deserializeNBT(nbt.getCompound("items")));
    }

    public static class CapabilityHandle implements ICapabilitySerializable<CompoundTag> {
        private final LazyOptional<ItemStackHandler> handler;

        public CapabilityHandle() {
            handler = LazyOptional.of(() -> new ItemStackHandler(9) {
                @Override
                public int getSlotLimit(int slot) {
                    return 1;
                }
            });
        }

        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @org.jetbrains.annotations.Nullable Direction side) {
            return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? handler.cast() : LazyOptional.empty();
        }

        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
            return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? handler.cast() : LazyOptional.empty();
        }

        private ItemStackHandler getItemHandler() {
            return handler.orElseThrow(RuntimeException::new);
        }

        @Override
        public CompoundTag serializeNBT() {
            return getItemHandler().serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            getItemHandler().deserializeNBT(nbt);
        }

    }
}
