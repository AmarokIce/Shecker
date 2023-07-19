package club.someoneice.shaker;

import club.someoneice.shaker.gui.ContainerShaker;
import club.someoneice.shaker.gui.GUIShaker;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(ShakerMain.MODID)
public class ShakerMain {
    public static final String MODID = "shaker";

    public static final DeferredRegister<Item> ItemList = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Item> SHAKER = ItemList.register("shaker", ShakerItem::new);

    public static final DeferredRegister<MenuType<?>> GuiList = DeferredRegister.create(ForgeRegistries.CONTAINERS, ShakerMain.MODID);

    public static final RegistryObject<MenuType<ContainerShaker>> SHAKER_GUI = GuiList.register("shaker_gui", () -> IForgeMenuType.create(ContainerShaker::new));

    public static final DeferredRegister<RecipeSerializer<?>> RecipesList = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
    public static final RegistryObject<RecipeSerializer<RecipeShaker>> SHAKER_RECIPE = RecipesList.register(RecipeShaker.NAME, () -> RecipeShaker.Serializer.INSTANCE);


    public ShakerMain() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ItemList.register(bus);
        GuiList.register(bus);
        RecipesList.register(bus);
        bus.addListener(this::clientSetup);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        MenuScreens.register(SHAKER_GUI.get(), GUIShaker::new);
    }
}
