package com.zte.ums.an.commonsh.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * <p>文件名称: </p>
 * <p>文件描述: </p>
 * <p>版权所有: 版权所有(C)2001-2015</p>
 * <p>公    司: 中兴通讯股份有限公司</p>
 * <p>内容摘要: </p>
 * <p>其他说明: </p>
 * <p>完成日期：2013年6月24日</p>
 * <p>修改记录1:</p>
 * <pre>
 *    修改日期：
 *    版 本 号：
 *    修 改 人：
 *    修改内容：
 * </pre>
 * <p>修改记录2：</p>
 * @version 1.0
 * @author  ChenDuoduo_10087118
 */
public class ToStringUtil
{
    private ToStringUtil()
    {
    }
    
    public static String toString(Object info)
    {
        Map<String, String> name2Valule = extractNameAndValue(info);
        
        return format(name2Valule);
    }

    private static Map<String, String> extractNameAndValue(Object info)
    {
        Map<String, String> name2Valule = new TreeMap<String, String>();
        
        Class<? extends Object> tmpClass = info.getClass();
        
        while(!isClassObject(tmpClass))
        {
            Field[] allFields = tmpClass.getDeclaredFields();
            name2Valule.putAll(extractNameAndValueFromFields(info, allFields));
            
            tmpClass = tmpClass.getSuperclass();
        }
        
        return name2Valule;
    }

    private static String format(Map<String, String> name2Valule)
    {
        if(name2Valule.size() == 0)
        {
            return "";
        }
        
        final int leftSideCharCount = getMaximumNameLength(name2Valule);
        
        StringBuffer buf = new StringBuffer();
        
        Iterator<Entry<String, String>> it = name2Valule.entrySet().iterator();
        while(it.hasNext())
        {
            Entry<String, String> item = it.next();
            buf.append(item.getKey()).append(getSpaces(leftSideCharCount - item.getKey().length())).append("==  ").append(item.getValue())
                            .append("\n");
        }
        
        return buf.delete(buf.length() - 1, buf.length()).toString();
    }

    private static String getSpaces(int count)
    {
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < count; i++)
        {
            buf.append(" ");
        }
        return buf.toString();
    }

    private static int getMaximumNameLength(Map<String, String> name2Valule)
    {
        int maxCount = 0;
        Iterator<Entry<String, String>> it = name2Valule.entrySet().iterator();
        while(it.hasNext())
        {
            int tmpCount = it.next().getKey().length();
            maxCount = tmpCount > maxCount ? tmpCount : maxCount;
        }
        
        return maxCount + 2;
    }

    private static boolean isClassObject(Class<? extends Object> tmpClass)
    {
        return tmpClass.getName().equals("java.lang.Object");
    }

    private static Map<String, String> extractNameAndValueFromFields(Object info, Field[] allFields)
    {
        Map<String, String> name2Value = new LinkedHashMap<String, String>();
        for(Field field : allFields)
        {
            field.setAccessible(true);
            if(!Modifier.isStatic(field.getModifiers()))
            {
                name2Value.put(field.getName(), getValue(info, field));
            }
        }
        return name2Value;
    }

    private static String getValue(Object info, Field field) 
    {
        try
        {
            if(isPrimitive(field))//基本数据类型
            {
                return String.valueOf(field.get(info));
            }
            else if(isArray(field))//数组
            {
                return getValueForArray(info, field);
            }
            else//对象等
            {
                return getValueForObject(info, field);
            }
        }
        catch(Exception e)
        {
            return "N/A (" + e.getMessage() + ")";
        }
    }

    private static String getValueForObject(Object info, Field field) throws IllegalAccessException
    {
        Object obj = field.get(info);
        if(obj == null)
        {
            return "null";
        }

        return "[" + (obj instanceof Iterable ? toStringForIterable((Iterable<?>)obj) : String.valueOf(obj)) + "]";
    }

    private static String toStringForIterable(Iterable<?> iterable)
    {
        Iterator<?> it = iterable.iterator();
        StringBuffer buf = new StringBuffer();
        while(it.hasNext())
        {
            buf.append("[").append(it.next()).append("]").append(", ");
        }
        
        if(buf.length() == 0)
        {
            return "";
        }
        else
        {
            return buf.delete(buf.length() - 2, buf.length()).toString();
        }
    }

    private static String getValueForArray(Object info, Field field) throws IllegalAccessException
    {
        if(isOneDimensionArray(field))
        {
            return toString4OneDimensionArray(field.get(info));
        }
        
        return Arrays.deepToString((Object[])field.get(info));
    }

    private static String toString4OneDimensionArray(Object obj)
    {
        if(obj == null)
        {
            return "null";
        }
        
        final int length = Array.getLength(obj);
        if(length == 0)
        {
            return "[]";
        }
        
        StringBuilder buff = new StringBuilder();
        buff.append('[');
        
        for (int i = 0; i < length - 1; i++)
        {
            buff.append(Array.get(obj, i));
            buff.append(", ");
        }
        
        return buff.append(Array.get(obj, length - 1)).append(']').toString();
    }

    private static boolean isOneDimensionArray(Field field)
    {
        return field.getType().getName().charAt(0) == '[' && field.getType().getName().charAt(1) != '[';
    }

    private static boolean isArray(Field field)
    {
        return field.getType().getName().startsWith("[");
    }
    
    private static boolean isPrimitive(Field field)
    {
        return field.getType().isPrimitive() ||  field.getType().getName().equals("java.lang.String");
    }
}
