package org.vivecraft;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.monster.EnderMan;

public class Reflector {
	//last checked 1.17.1
	public static Field Entity_Data_Pose= getPrivateField("ad", Entity.class);
	public static Field Entity_eyesHeight = getPrivateField("aX", Entity.class);
	public static Field SynchedEntityData_itemsById = getPrivateField("f", SynchedEntityData.class);
	public static Field availableGoals = getPrivateField("d", GoalSelector.class);
	public static Field aboveGroundCount = getPrivateField("C", ServerGamePacketListenerImpl.class);
	
	public static Method Entity_teleport= getPrivateMethod("t", EnderMan.class);
	public static Method Entity_teleportTowards = getPrivateMethod("a", EnderMan.class, Entity.class);
	
	public static Object getFieldValue(Field field, Object object) {
		try
		{
			return field.get(object);
		}catch(IllegalAccessException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	public static void setFieldValue(Field field, Object object, Object value) {
		try
		{
			 field.set(object, value);
		}catch(IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}
	
	private static Field getPrivateField(String fieldName, Class clazz)
	{
		Field field = null;
		try
		{
			field = clazz.getDeclaredField(fieldName);
			field.setAccessible(true);
		}
		catch(NoSuchFieldException e)
		{
			e.printStackTrace();
		}
		return field;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Method getPrivateMethod(String methodName, Class clazz, Class... param)
	{
		Method m = null;
		try
		{
			if(param == null) {
				m = clazz.getDeclaredMethod(methodName);
			} else {
				m = clazz.getDeclaredMethod(methodName, param);
			}
			m.setAccessible(true);
		}
		catch(NoSuchMethodException e)
		{
			e.printStackTrace();
		}
		return m;
	}
	
	public static Object invoke(Method m, Object object, Object... param) {
		try {
			if(param == null) 
				return  m.invoke(object);
			else
				return  m.invoke(object, param);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
}
