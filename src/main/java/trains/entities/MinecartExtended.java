package trains.entities;


import com.mojang.authlib.GameProfile;
import mods.railcraft.api.carts.IMinecart;
import mods.railcraft.api.carts.IRoutableCart;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidTank;

import java.util.UUID;

public class MinecartExtended extends EntityMinecart implements IMinecart, IRoutableCart, IInventory {

    //Main Values
    public int[] colors; //allows certain parts of certain trains to be recolored
    public String name;
    public boolean isLocked = false; //mostly used to lock other players from using/accessing parts of the cart/train
    public boolean brake = false; //bool for the train/rollingstock's break.
    public boolean lamp = false; //controls the headlight/lamp
    public int[] previousLampPosition = new int[]{0,0,0}; //this is the position of the light previously, only two lights per train will ever exist at one time.
    public float maxSpeed; // the max speed
    public int GUIID = 0; //id for the GUI
    public UUID owner = null;  //universal, get train owner
    private int minecartNumber = 0; //used to identify the minecart number so it doesn't interfere with other mods or the base game minecarts,

    //inventory
    public ItemStack[] inventory = new ItemStack[]{};//Inventory, every train will have this to some extent or another,
    public ItemStack[] slots = new ItemStack[]{};//Inventory, every train will have this to some extent or another,
    public FluidTank[] tank = new FluidTank[]{};//depending on the train this is either used for diesel, steam, or redstone flux
    public int rows =0; //defines the inventory width
    public int columns =0;//defines inventory height

    //train values
    public float[] acceleration; //the first 3 values are a point curve, representing 0-35%, 35-70% and >70% to modify how acceleration is handled at each point. //the 4th value defines how much the weight hauled effects acceleration.
    public int trainType=0;//list of train types 0 is null, 1 is steam, 2 is diesel, 3 is electric
    public boolean isRunning = false;// if the train is running/using fuel
    private int ticks = 0; //tick count.

    //rollingstock values
    public Item[] storageFilter = new Item[]{};//item set to use for filters, storage only accepts items in the filter
    public Material[] storageMaterialFilter = new Material[]{};//same as item filter but works for materials
    public boolean canBeRidden;

    //railcraft variables
    public String destination = "";  //railcraft destination
    public boolean isLoco = false;  //if this can accept destination tickets, aka is a locomotive


    /*/
    Functions
    /*/

    //default constructor for registering entity
    public MinecartExtended(World world) {
        super(world);
    }
    //default constructor we actually use
    public MinecartExtended(UUID owner, World world, double xPos, double yPos, double zPos, float maxSpeed, float[] acceleration,
                            Item[] storageItemFilter /*/ empty array for no filter /*/ , Material[] storageMaterialFilter /*/ empty array for no filter /*/ ,
                            int type /*1-steam, 2-diesel, 3-electric, 4-hydrogen, 5-nuclear, 0-RollingStock*/,
                            FluidTank[] tank /*/ empty array for no tanks, - steam and nuclear take two tanks. - all other trains take one tank - all tanks besides diesel should use FluidRegistry.WATER /*/,
                            int inventoryrows, int inventoryColumns /*/ the inventory is rows(x) * columns(y)/*/,
                            int GUIid, int minecartNumber, boolean canBeRidden) {
        super(world,xPos, yPos, zPos);
        isLoco = true;
        this.maxSpeed = maxSpeed;
        this.acceleration = acceleration;
        this.owner = owner;
        this.minecartNumber = minecartNumber;
        this.storageMaterialFilter = storageMaterialFilter;
        this.canBeRidden = canBeRidden;
        this.tank = tank;
        trainType = type;
        inventory = new ItemStack[inventoryrows * inventoryColumns];
        GUIID = GUIid;
        storageFilter = storageItemFilter;
        rows = inventoryrows;
        columns = inventoryColumns;

    }


    /*/
    Inventory stuff, most of this is self-explanatory.
    /*/
    @Override
    public String getInventoryName() {
        return name;
    }
    @Override
    public void openInventory() {}
    @Override
    public void closeInventory() {}
    @Override
    public void markDirty() {}

    @Override
    public ItemStack getStackInSlot(int slot) {
        //be sure the slot exists before trying to return anything,
        if (slot>=0 && slot< inventory.length) {
            return inventory[slot];
        } else if(slot == -1){
            return slots[0];
        } else if (slot == -2) {
            return slots[1];
        } else {
            return null;
        }
    }
    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        //we return null no matter what, but we want to make sure the slot is properly set as well.
        if (slot>=0 && slot< inventory.length) {
            inventory[slot] = null;
        }
        return null;
    }
    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        //be sure the slot exists before trying to return anything,
        if (slot>=0 && slot< inventory.length) {
            //if subtraction makes slot empty/null then set it to null and return null, otherwise return the stack.
            if (inventory[slot].stackSize <= amount ^ inventory[slot].stackSize <= 0) {
                inventory[slot] = null;
                return null;
            } else {
                return inventory[slot].splitStack(amount);
            }
        } else if (slot <0) {
        //manage crafter slots for trains
            switch (slot){
                case -1:{
                    if (slots[0].stackSize <= amount ^ slots[0].stackSize <= 0) {
                        slots[0] = null;
                        return null;
                    } else {
                        return inventory[slot].splitStack(amount);
                    }
                }
                case -2:{
                    if (slots[1].stackSize <= amount ^ slots[1].stackSize <= 0) {
                        slots[1] = null;
                        return null;
                    } else {
                        return slots[1].splitStack(amount);
                    }
                }
                default:{return null;}
            }

        } else {
            return null;
        }
    }
    @Override
    public void setInventorySlotContents(int slot, ItemStack itemstack) {
        //be sure item stack isn't null, then add the itemstack, and be sure the slot doesn't go over the limit.
        if (itemstack != null && slot >=0 && slot<inventory.length) {
            if (itemstack.stackSize >= getInventoryStackLimit()) {
                itemstack.stackSize = getInventoryStackLimit();
            }
        } else if (itemstack != null && slot == -1){
            slots[0] = itemstack;
            if (itemstack.stackSize >= getInventoryStackLimit()) {
                itemstack.stackSize = getInventoryStackLimit();
            }
        } else if (itemstack != null && slot == -2){
            slots[1] = itemstack;
            if (itemstack.stackSize >= getInventoryStackLimit()) {
                itemstack.stackSize = getInventoryStackLimit();
            }
        }
        inventory[slot] = itemstack;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    //return if the item can be placed in the slot, for this slot it's just a check if the slot exists, but other things may have slots for specific items, this filters that.
    @Override
    public boolean isItemValidForSlot(int slot, ItemStack item){
        return (slot>=0 && slot< inventory.length);
    }

    //return the number of inventory slots
    @Override
    public int getSizeInventory(){
        if(inventory != null){
            return inventory.length;
        } else{
            return 0;
        }
    }

    //return if the train can be used by the player, if it's locked, only the owner can use it.
    @Override
    public boolean isUseableByPlayer(EntityPlayer player){
        if (isLocked){
            if(owner.equals(player.getUniqueID())){
                return true;
            } else {
                return false;
            }
        } else{
            return true;
        }
    }

    /*/
    Core Minecart Overrides
    /*/
    //technically this is a normal minecart, so return the value for that, which isn't in the base game or another mod.
    @Override
    public int getMinecartType() {
        return minecartNumber;
    }
    //cart management stuff
    @Override
    public boolean isPoweredCart() {
        return true;
    }
    @Override
    public boolean canBeRidden() {
        return canBeRidden;
    }
    @Override
    public boolean canBePushed() {
        return true;
    }//TODO this should be false later when it can move on its own.
    @Override
    public boolean canRiderInteract()
    {
        return true;
    }


    /*/
    Function that runs every tick.
    /*/
    @Override
    public void onUpdate() {
        super.onUpdate();
        ticks++;
        //create a manager for the ticks, that way we can do something different each tick to lessen the performance hit.
        switch (ticks){
            case 5:{
                //manage fuels, no point in doing a check if this should be run or not, due to the overall efficiency of switch statements.
                switch (trainType) {
                    //steam
                    case 1: {

                        break;
                    }
                    //diesel
                    case 2: {

                        break;
                    }
                    //electric
                    case 3: {

                        break;
                    }
                    //hydrogen
                    case 4: {

                        break;
                    }
                    //nuclear
                    case 5: {

                        break;
                    }
                    //case for it not being a train
                    default:{break;}
                }
                break;
            }
            case 10:{
                /*/ for managing the lamp, will need to implement it better later. Maybe do a client side only to change lighting of individual blocks?
                if(previousLampPosition != new int[]{MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ+2)}){
                    if(previousLampPosition != new int[]{0,0,0}) {
                        worldObj.setBlockToAir(previousLampPosition[0], previousLampPosition[1], previousLampPosition[2]);
                    }
                    previousLampPosition=new int[]{MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ+2)};

                    if (lamp && worldObj.isAirBlock(previousLampPosition[0], previousLampPosition[1], previousLampPosition[2])) {
                        worldObj.setBlock(previousLampPosition[0], previousLampPosition[1], previousLampPosition[2], new BlockLight());
                        System.out.println("created lamp child");
                    }
                }
                /*/
                break;
            }
            //other cases
            default:{
                //if the tick count is higher than the values used, reset it so it can count up again.
                if (ticks>10){
                ticks = 0;
                }
                break;
            }

        }




    }

    /*/
    NBT
    /*/
    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        //colors = tag.getIntArray("extended.colors");
        isLocked = tag.getBoolean("extended.isLocked");
        brake = tag.getBoolean("extended.brake");
        lamp = tag.getBoolean("extended.lamp");
        //previousLampPosition = tag.getIntArray("extended.previousLamp");
        owner = new UUID(tag.getLong("extended.ownerM"),tag.getLong("extended.ownerL"));
        isRunning = tag.getBoolean("extended.isRunning");
        ticks = tag.getInteger("extended.ticks");
        destination = tag.getString("extended.destination");
        //read through the itemstacks
        NBTTagList taglist = tag.getTagList("Items", 10);
        for (int i = 0; i < taglist.tagCount(); i++) {
            NBTTagCompound nbttagcompound1 = taglist.getCompoundTagAt(i);
            byte b0 = nbttagcompound1.getByte("Slot");

            if (b0 >= 0 && b0 < inventory.length) {
                inventory[b0] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
            }
        }

        for (int t=0; t<tank.length; t++){
            tank[t].readFromNBT(tag);
        }
        //items with static-esk values that shouldn't need NBT,
        //name, maxSpeed, GUIID, minecartNumber, trainType, acceleration, filters, canBeRidden, isLoco.
    }
    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        //tag.setIntArray("extended.colors", colors);
        tag.setBoolean("extended.isLocked", isLocked);
        tag.setBoolean("extended.brake", brake);
        tag.setBoolean("extended.lamp", lamp);
        //tag.setIntArray("extended.previousLamp", previousLampPosition);
        tag.setLong("extended.ownerM", owner.getMostSignificantBits());
        tag.setLong("extended.ownerL", owner.getLeastSignificantBits());
        tag.setBoolean("extended.isRunning",isRunning);
        tag.setInteger("extended.ticks", ticks);
        tag.setString("extended.destination",destination);
        //write the itemset to a tag list before adding it
        NBTTagList nbttaglist = new NBTTagList();
        for (int i = 0; i < inventory.length; ++i) {
            if (inventory[i] != null) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte)i);
                inventory[i].writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }
        tag.setTag("Items", nbttaglist);

        for (int t=0; t<tank.length; t++){
            tank[t].writeToNBT(tag);
        }

    }



    /*/
    Railcraft support
    /*/
    @Override
    public String getDestination() {
        return destination;
    }
    @Override
    public boolean doesCartMatchFilter(ItemStack stack, EntityMinecart cart) {
        if (stack == null || cart == null) { return false; }
        ItemStack cartItem = cart.getCartItem();
        return cartItem != null && stack.isItemEqual(cartItem);
    }
    //Only locomotives can receive a destination from a track.
    @Override
    public boolean setDestination(ItemStack ticket) {
        return isLoco;
    }

    //used by railcraft, this is needed but we'll obsolete this with our own methods because this is just poor.
    @Override
    public GameProfile getOwner(){return null;}

    //methods for getting/setting actual owner
    public void setOwner(UUID player){owner = player;}
    public UUID getOwnerUUID(){return owner;}

}
