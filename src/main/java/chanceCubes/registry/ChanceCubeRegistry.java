package chanceCubes.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import chanceCubes.CCubesCore;
import chanceCubes.items.CCubesItems;
import chanceCubes.items.ItemChancePendant;
import chanceCubes.rewards.BasicReward;
import chanceCubes.rewards.IChanceCubeReward;
import chanceCubes.rewards.type.EntityRewardType;
import chanceCubes.rewards.type.ExperienceRewardType;
import chanceCubes.rewards.type.ItemRewardType;
import chanceCubes.rewards.type.MessageRewardType;
import chanceCubes.rewards.type.PotionRewardType;
import chanceCubes.rewards.type.PotionRewardType.PotionType;

public class ChanceCubeRegistry
{

	private static List<IChanceCubeReward> rewards = new ArrayList<IChanceCubeReward>();
	
	private int range = 75;

	/**
	 * loads the default rewards of the Chance Cube
	 */
	public void loadDefaultRewards()
	{
		this.registerReward(new BasicReward(CCubesCore.MODID+":RedstoneDiamond", -75, new ItemRewardType(new ItemStack(Items.redstone), new ItemStack(Items.diamond))));
		this.registerReward(new BasicReward(CCubesCore.MODID+":Creeper", 0, new EntityRewardType("Creeper")));
		this.registerReward(new BasicReward(CCubesCore.MODID+":RedstoneZombie", 100, new ItemRewardType(new ItemStack(Items.redstone)), new EntityRewardType("Zombie")));
		this.registerReward(new BasicReward(CCubesCore.MODID+":EXP", 25, new ExperienceRewardType(100)));
		this.registerReward(new BasicReward(CCubesCore.MODID+":Potions", 0, new PotionRewardType(PotionType.POISON_II)));
		this.registerReward(new BasicReward(CCubesCore.MODID+":ChatMessage", 0, new MessageRewardType("You have escaped the wrath of the Chance Cubes........."), new MessageRewardType("For now......")));
	}

	/**
	 * Registers the given reward as a possible outcome
	 * @param reward to register
	 */
	public void registerReward(IChanceCubeReward reward)
	{
		rewards.add(reward);
	}

	/**
	 * Unregisters a reward with the given name
	 * @param name of the reward to remove
	 * @return true is a reward was successfully removed, false if a reward was not removed
	 */
	public boolean unregisterReward(String name)
	{
		for(int i = 0; i < rewards.size(); i++)
		{
			IChanceCubeReward reward = rewards.get(i);
			if(reward.getName().equalsIgnoreCase(name))
			{
				rewards.remove(reward);
				return true;
			}
		}
		return false;
	}

	/**
	 * Triggers a random reward in the given world at the given location
	 * @param World
	 * @param x
	 * @param y
	 * @param z
	 */
	public void triggerRandomReward(World world, int x, int y, int z, EntityPlayer player, int luck)
	{
		//Luck from pendant
		if (player != null && player.inventory.hasItem(CCubesItems.chancePendant))
		{
			for (int i = 0; i < player.inventory.mainInventory.length; i++)
			{
				ItemStack stack = player.inventory.mainInventory[i];
				if (stack != null && stack.getItem().equals(CCubesItems.chancePendant))
				{
					ItemChancePendant pendant = (ItemChancePendant) stack.getItem();
					
					if (stack.getItemDamage() == 0)
					{
						int lapisCounter = 0;
						for (int slotCounter = 0; slotCounter < player.inventory.mainInventory.length; slotCounter++)
						{
							ItemStack lapisStack = player.inventory.mainInventory[slotCounter];
							if (lapisStack != null && lapisStack.getItem().equals(Items.dye) && lapisStack.getItemDamage() == 4) //Lapis!
							{
								lapisCounter += lapisStack.stackSize;
								player.inventory.setInventorySlotContents(slotCounter, null); //Annnnnnnd it's gone.
							}
							else if (lapisStack != null && lapisStack.getItem().equals(Item.getItemFromBlock(Blocks.lapis_block)))
							{
								lapisCounter += 9*lapisStack.stackSize;
								player.inventory.setInventorySlotContents(slotCounter, null);
							}
						}
						
						luck = luck + lapisCounter> (100+this.range) ? 100+this.range : luck + lapisCounter; //Don't overflow on topside.
					}
					
					if (stack.getItemDamage() == 1) //Not yet implemented
					{
						luck += pendant.getLuck(stack); //Stores lapis internally
						pendant.removeAllLuck(stack);
						pendant.damage(stack);
					}
						
				}
					
			}

		}
		int lowerIndex = 0;
		int upperIndex = rewards.size();
		int lowerRange = luck - this.range < -100 ? -100: luck - this.range;
		int upperRange = luck + this.range > 100 ? 100: luck + this.range;
		
		for(int i = 0; i < rewards.size(); i++)
		{
			if(rewards.get(i).getLuckValue() >= lowerRange)
			{
				lowerIndex = i;
				break;
			}
		}
		for(int i = rewards.size()-1; i >= 0; i--)
		{
			if(rewards.get(i).getLuckValue() <= upperRange)
			{
				upperIndex = i;
				break;
			}
		}
		
		int pick = world.rand.nextInt(upperIndex-lowerIndex + 1) + lowerIndex;
		rewards.get(pick).trigger(world, x, y, z, player);
    }

    public void processRewards()
    {
        Collections.sort(rewards, new Comparator<IChanceCubeReward>()
        {
            public int compare(IChanceCubeReward o1, IChanceCubeReward o2)
            {
                return o1.getLuckValue() - o2.getLuckValue();
            };
        });
    }

	public void setRange(int r)
	{
		this.range = r;
	}
}
