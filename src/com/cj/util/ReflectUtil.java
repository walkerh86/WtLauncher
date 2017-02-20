package com.cj.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.util.Log;

public class ReflectUtil{
	public static Object reflectGetFieldValue(Object obj, String fieldName){
		Object result = null;
		try{
			Class objClass = obj.getClass();
			Field field = objClass.getDeclaredField(fieldName);
			field.setAccessible(true);
			result = field.get(obj);
		}catch(Exception e){
			Log.i("hcj","reflectGetFieldValue,e="+e);
		}
		return result;
	}

	public static void reflectSetFieldValue(Object obj, String fieldName, Object value){
		try{
			Class objClass = obj.getClass();
			Field field = objClass.getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(obj,value);
		}catch(Exception e){
			Log.i("hcj","reflectSetFieldValue,e="+e);
		}
	}

	public static Object reflectGetParentFieldValue(Object obj, String fieldName){
		Object result = null;
		try{
			Class<?> objClass = obj.getClass();
			Class<?> parentClass = objClass.getSuperclass();
			Field field = parentClass.getDeclaredField(fieldName);
			field.setAccessible(true);
			result = field.get(obj);
		}catch(Exception e){
			Log.i("hcj","reflectGetParentFieldValue,e="+e);
		}
		return result;
	}

	public static Object reflectCallMethod(Object obj, String methodName, Class<?>[] paramsTypes, Object[] params){
		Object ret = null;
		try{
			Method method = obj.getClass().getDeclaredMethod(methodName, paramsTypes);
			Log.i("hcj","reflectCallMethod,method="+method);
			if(method != null){
				ret = method.invoke(obj,params);
			}
		}catch(Exception e){
			Log.i("hcj","reflectCallMethod,e="+e);
		}
		return ret;
	}
}
