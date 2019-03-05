package org.vivecraft.gameplay.trackers;

import java.util.Random;

import org.vivecraft.gameplay.OpenVRPlayer;
import org.vivecraft.gameplay.trackers.Tracker.EntryPoint;
import org.vivecraft.provider.MCOpenVR;
import org.vivecraft.utils.InputSimulator;
import org.vivecraft.utils.Quaternion;
import org.vivecraft.utils.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.item.ItemTrident;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Created by Hendrik on 02-Aug-16.
 */
public class ShieldTracker extends Tracker{
	
	public boolean isClick;
	public boolean isAlready;
	public boolean isMain;
	boolean shieldUp;
	long shieldStart;
	int shieldtime=250;
	
	
	public ShieldTracker(Minecraft mc) {
		super(mc);
	}
	
	public boolean isActive(EntityPlayerSP p){
		if(Minecraft.getMinecraft().vrSettings.seated)
			return false;
		if(p == null) return false;
		if(p.isDead) return false;
		if(p.isPlayerSleeping()) return false;
		if(p.getHeldItemMainhand() != null){
			EnumAction action=p.getHeldItemMainhand().getUseAction();
			if(action == EnumAction.BLOCK) return true;
		}
		if(p.getHeldItemOffhand() != null){
			EnumAction action=p.getHeldItemOffhand().getUseAction();
			if(action == EnumAction.BLOCK) return true;
		}
		return false;
	}

	@Override
	public EntryPoint getEntryPoint() {
		return EntryPoint.SPECIAL_ITEMS;
	}
	public static boolean isShield(ItemStack itemStack) {
		if( itemStack == ItemStack.EMPTY) return false;
		if(Minecraft.getMinecraft().vrSettings.bowMode == 0) return false;
		else if(Minecraft.getMinecraft().vrSettings.bowMode == 1) return itemStack.getItem() == Items.SHIELD; 
		else return itemStack.getItem().getUseAction(itemStack) == EnumAction.BLOCK;			 
	}
	public static boolean isHoldingShield(EntityLivingBase e, EnumHand hand) {
		if(Minecraft.getMinecraft().vrSettings.seated) return false;
		return isShield(e.getHeldItem(hand));
	}

private Random r = new Random();

	public void doProcess(EntityPlayerSP player){

		OpenVRPlayer provider = mc.vrPlayer;
		
		Vec3d hmdPos=provider.vrdata_room_pre.hmd.getPosition();
		for(int c=0;c<2;c++){
			
			boolean main = this.isHoldingShield(player, EnumHand.MAIN_HAND);
			boolean off = this.isHoldingShield(player, EnumHand.OFF_HAND);
			EnumHand hand = main ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
			double handMul = main ? 1 : -1;
			Vec3d controllerPos = provider.vrdata_room_pre.getController(c).getPosition();//.add(provider.getCustomControllerVector(c, new Vec3(0, 0, -0.1)));
			Vec3d controllerDir = provider.vrdata_room_pre.getController(c).getDirection();
			Vec3d hmddir = provider.vrdata_room_pre.hmd.getDirection();
			Vec3d hmd90 = hmddir.rotateYaw(90);
			Vec3d side = new Vec3d(0, 0, 1*handMul);
			Vec3d forward = new Vec3d(1, 0, 0);
			Vec3d delta = hmdPos.subtract(controllerPos);
			double dotDelta = delta.dotProduct(hmddir);
			double dotDelta90 = delta.dotProduct(hmd90);
			
			boolean zone = ((hmdPos.y - controllerPos.y) < 0.25) ; //controller above half hmd
			boolean zone2 = ((hmdPos.x - controllerPos.x)*handMul > 0.1);
			boolean zone3 = (dotDelta90*handMul<0);
			boolean zone4 = (dotDelta<0);
			int check=0;
			
			if (c==1&&off) check = 2;
			if (c==0&&main) check = 1;
			if(c==1&&isMain) return;
			Vector3 vec3 = new Vector3(hmddir); 
			
			if(zone&&zone3&&zone4&&check >0 ){
				isClick = true;
			}else {
				isClick = false;
			}
			
			if(isClick==true&&isAlready==true&&check == 0) {
				InputSimulator.releaseMouse(1);
				isMain = false;
				isAlready = false;
				isClick = false;
				return;
			}
			
			if(check == 1) {
				if(isClick == true&&isAlready== false) {
					shieldStart = Util.milliTime();
					shieldUp = false;
					InputSimulator.pressMouse(1);
					isMain = true;
					isAlready = true;
				}else if(isClick==false && isAlready == true) {
					isMain = false;
					InputSimulator.releaseMouse(1);
					isAlready = false;
				}
				if(Util.milliTime()-shieldStart>shieldtime&&shieldUp==false) {
					MCOpenVR.triggerHapticPulse(c, 700 );
					shieldUp = true;
				}
			}else if(check==2) {
//				boolean checkMainEnum = false;
//				System.out.println("Pos : " + (hmdPos.y - controllerPos.y));
//				System.out.println("dotDelta : " +dotDelta );
//				if(	mc.playerController.processRightClick(player, player.world,c==0?EnumHand.MAIN_HAND:EnumHand.OFF_HAND)==EnumActionResult.SUCCESS){
//					
//				}
				ItemStack is = player.getHeldItem(EnumHand.MAIN_HAND);
	        	Item item = null;
				if(is!=null )item = is.getItem();

	            boolean tool = false;
	            boolean sword = false;

	            if(item instanceof ItemSword){
	            	sword = true; 	
	            }else if (item instanceof ItemTool){
	            	tool = true;
	            }
				if(isClick == true&&isAlready== false&&(sword==true||tool==true)) {
					shieldStart = Util.milliTime();
					shieldUp = false;
					InputSimulator.pressMouse(1);
//					player.setItemInUseClient(player.getHeldItem(EnumHand.OFF_HAND));
//					mc.playerController.processRightClick(player, player.world, hand);
					isAlready = true;
				}else if(isClick==false && isAlready == true) {
					InputSimulator.releaseMouse(1);
//					player.setItemInUseClient(ItemStack.EMPTY);
					isAlready = false;
				}
				if(Util.milliTime()-shieldStart>shieldtime&&shieldUp==false) {
					MCOpenVR.triggerHapticPulse(c, 700 );
					shieldUp = true;
				}
//				 mc.playerController.processRightClickBlock(mc.player, mc.world, blockpos, mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec, enumhand)
			}
//			
//			if (!mc.playerController.getIsHittingBlock()){
//	            if (!mc.player.isRowingBoat()) {
//	                for (EnumHand enumhand : EnumHand.values()){
//	                    ItemStack itemstack = mc.player.getHeldItem(enumhand);
//	                    if (mc.objectMouseOver != null){
//	                        switch (mc.objectMouseOver.type){
//	                            case ENTITY:
//	                                if (mc.playerController.interactWithEntity(mc.player, mc.objectMouseOver.entity, mc.objectMouseOver, enumhand) == EnumActionResult.SUCCESS) {
//	                                    return;
//	                                }
//
//	                                if (mc.playerController.interactWithEntity(mc.player, mc.objectMouseOver.entity, enumhand) == EnumActionResult.SUCCESS){
//	                                    return;
//	                                }
//	                                break;
//
//	                            case BLOCK:
//	                                BlockPos blockpos = mc.objectMouseOver.getBlockPos();
//
//	                                if (!mc.world.getBlockState(blockpos).isAir()) {
//	                                    int i = itemstack.getCount();
//	                                    EnumActionResult enumactionresult = mc.playerController.processRightClickBlock(mc.player, mc.world, blockpos, mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec, enumhand);
//
//	                                    if (enumactionresult == EnumActionResult.SUCCESS){
//	                                    	mc.player.swingArm(enumhand);
//
//	                                        if (!itemstack.isEmpty() && (itemstack.getCount() != i || mc.playerController.isInCreativeMode())){
//	                                        	mc.entityRenderer.itemRenderer.resetEquippedProgress(enumhand);
//	                                        }
//
//	                                        return;
//	                                    }
//
//	                                    if (enumactionresult == EnumActionResult.FAIL) {
//	                                        return;
//	                                    }
//	                                }
//	                        }
//	                    }
//
//	                    if (!itemstack.isEmpty() && mc.playerController.processRightClick(mc.player, mc.world, enumhand) == EnumActionResult.SUCCESS) {
//	                    	mc.entityRenderer.itemRenderer.resetEquippedProgress(enumhand);
//	                        return;
//	                    }
//	                }
//	            }
//	        }

		}
	}
}
