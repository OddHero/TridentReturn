package art.rehra.tridentreturn;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("tridentreturn")
public class TridentReturn
{
    // Directly reference a log4j logger.
    //private static final Logger LOGGER = LogManager.getLogger();

    public TridentReturn() {
        // Register the setup method for modloading
        //FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        //FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        //FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        //FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

    }

    //private void setup(final FMLCommonSetupEvent event)
    //{
    //    // some preinit code
    //    LOGGER.info("HELLO FROM PREINIT");
    //    LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    //}

    //private void doClientStuff(final FMLClientSetupEvent event) {
    //    // do something that can only be done on the client
    //    LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
    //}

    //private void enqueueIMC(final InterModEnqueueEvent event)
    //{
    //    // some example code to dispatch IMC to another mod
    //    InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    //}

    //private void processIMC(final InterModProcessEvent event)
    //{
    //    // some example code to receive and process InterModComms from other mods
    //    LOGGER.info("Got IMC {}", event.getIMCStream().
    //            map(m->m.getMessageSupplier().get()).
    //            collect(Collectors.toList()));
    //}

    @SubscribeEvent
    public void onTridentThrow(PlayerEvent.StartTracking event) {
        if(event.getTarget() instanceof TridentEntity){
            TridentEntity trident = (TridentEntity) event.getTarget();
            PlayerEntity player = (PlayerEntity) event.getPlayer();
            ItemStack currentItemStack = player.inventory.getCurrentItem();
            Item currentItem = currentItemStack.getItem();
            ResourceLocation location = new ResourceLocation("minecraft:trident");
            CompoundNBT nbt = new CompoundNBT();
            trident.writeAdditional(nbt);
            CompoundNBT triNBT = nbt.getCompound("Trident").getCompound("tag");
            if(triNBT.getInt("returnSlot")!=0)return;
            int returnSlot = -1;
            if(currentItem.getRegistryName().equals(location)){
                returnSlot = player.inventory.currentItem+1;
            }else if(player.inventory.offHandInventory.get(0).getItem().getRegistryName().equals(location)){
                returnSlot = -1;
            }
            triNBT.putInt("returnSlot",returnSlot);
            trident.readAdditional(nbt);
        }
    }

    @SubscribeEvent
    public void onTridentReturn(PlayerEvent.StopTracking event) {
        if(event.getTarget() instanceof TridentEntity){
            PlayerEntity player = event.getPlayer();
            int returnedSlot,returnSlot;
            NonNullList<ItemStack> mainInventory = player.inventory.mainInventory;
            for (int i = 0, mainInventorySize = mainInventory.size(); i < mainInventorySize; i++) {
                ItemStack stack = mainInventory.get(i);
                if (stack==null) continue;
                CompoundNBT tag = stack.getTag();
                if(tag == null)continue;
                returnSlot = tag.getInt("returnSlot");
                tag.remove("returnSlot");
                returnSlot = returnSlot>0?returnSlot-1:returnSlot;
                returnedSlot = i;
                if (returnSlot != 0) {
                    if(returnSlot == -1){
                        if(player.inventory.offHandInventory.get(0).isEmpty()){
                            player.inventory.offHandInventory.set(0,player.inventory.mainInventory.get(returnedSlot));
                            player.inventory.mainInventory.set(returnedSlot,ItemStack.EMPTY);
                        }
                    }else if(returnSlot>=0 && player.inventory.mainInventory.get(returnSlot).isEmpty()){
                        player.inventory.mainInventory.set(returnSlot,player.inventory.mainInventory.get(returnedSlot));
                        player.inventory.mainInventory.set(returnedSlot,ItemStack.EMPTY);
                    }
                }
            }
        }
    }

    //// You can use SubscribeEvent and let the Event Bus discover methods to call
    //@SubscribeEvent
    //public void onServerStarting(FMLServerStartingEvent event) {
    //    // do something when the server starts
    //    LOGGER.info("HELLO from server starting");
    //}

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    //@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    //public static class RegistryEvents {
    //    @SubscribeEvent
    //    public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
    //        // register a new block here
    //        LOGGER.info("HELLO from Register Block");
    //    }
    //}
}
