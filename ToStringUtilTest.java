package com.zte.ums.an.commonsh.util;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * <p>文件名称: </p>
 * <p>文件描述: </p>
 * <p>版权所有: 版权所有(C)2001-2015</p>
 * <p>公    司: 中兴通讯股份有限公司</p>
 * <p>内容摘要: </p>
 * <p>其他说明: </p>
 * <p>完成日期：2013年6月14日</p>
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
public class ToStringUtilTest extends TestCase
{
    public void testEmptyInfo()
    {
        assertEquals("", new EmptyInfo().toString());
    }
    
    public void testSingleInfo()
    {
        assertEquals("location  ==  Shanghai", new SingleInfo().toString());
    }
    
    public void testChildInfoWithInitData()
    {    
        String expectedStr = "baseIntValue      ==  100\n" 
                           + "booleanPorts      ==  null\n"
                           + "booleanValue      ==  false\n"
                           + "bytePorts         ==  null\n"
                           + "charPorts         ==  null\n"
                           + "doublePorts       ==  null\n"
                           + "floatPorts        ==  null\n"
                           + "floatTables       ==  null\n"
                           + "infoNew           ==  [location  ==  Shanghai]\n"
                           + "infoNull          ==  null\n"
                           + "intPorts          ==  null\n"
                           + "intTables         ==  null\n" 
                           + "intValue          ==  0\n" 
                           + "integerList       ==  null\n" 
                           + "integerListEmpty  ==  []\n" 
                           + "longPorts         ==  null\n" 
                           + "ports             ==  null\n" 
                           + "shortPorts        ==  null\n" 
                           + "singleInfoList    ==  null\n" 
                           + "singleInfoTables  ==  null\n" 
                           + "singleInfos       ==  null\n" 
                           + "strNull           ==  null\n" 
                           + "strUnInitialized  ==  null\n"
                           + "tables            ==  null";
                           
        
        assertEquals(expectedStr, new ChildInfo().toString());
    }
    
    public void testChildInfo()
    {    
        ChildInfo info = assembelInfo();
        
        String expectedStr = "baseIntValue      ==  100\n" 
                           + "booleanPorts      ==  [true, false]\n"
                           + "booleanValue      ==  false\n"
                           + "bytePorts         ==  [123, 2]\n"
                           + "charPorts         ==  [a, b]\n"
                           + "doublePorts       ==  [5.55, 8.88]\n"
                           + "floatPorts        ==  [1.3, 6.7]\n"
                           + "floatTables       ==  [[1.0, 2.0], [3.0, 4.0]]\n"
                           + "infoNew           ==  [location  ==  Shanghai]\n"
                           + "infoNull          ==  null\n"
                           + "intPorts          ==  [1, 3, 10]\n"
                           + "intTables         ==  [[1, 2], [3, 4]]\n"
                           + "intValue          ==  222\n" 
                           + "integerList       ==  [[4090], [4091]]\n" 
                           + "integerListEmpty  ==  []\n" 
                           + "longPorts         ==  [111, 222]\n" 
                           + "ports             ==  [aaa, bbb]\n" 
                           + "shortPorts        ==  [3, 4]\n" 
                           + "singleInfoList    ==  [[location  ==  Shanghai], [location  ==  Shanghai]]\n" 
                           + "singleInfoTables  ==  [[location  ==  Shanghai, location  ==  Shanghai], [location  ==  Shanghai]]\n" 
                           + "singleInfos       ==  [location  ==  Shanghai, location  ==  Shanghai]\n" 
                           + "strNull           ==  myString\n" 
                           + "strUnInitialized  ==  null\n"
                           + "tables            ==  [[aaa, bbb], [ccc, ddd]]";
        
        assertEquals(expectedStr, info.toString());
    }

    private ChildInfo assembelInfo()
    {
        ChildInfo info = new ChildInfo();
        
        info.booleanPorts = new boolean[] {true, false};
        info.bytePorts = new byte[] {123, 2};
        info.charPorts = new char[] {'a', 'b'};
        info.doublePorts = new double[] {5.55, 8.88};
        info.floatPorts = new float[] {1.3f, 6.7f};
        info.floatTables = new float[][] {{1.0f, 2.0f}, {3.0f, 4.0f}};
        info.intPorts = new int[] {1, 3, 10};
        info.intTables = new int[][] {{1, 2}, {3, 4}};
        info.intValue = 222;
        info.longPorts = new long[] {111, 222};
        info.ports = new String[] {"aaa", "bbb"};
        info.shortPorts = new short[] {3, 4};
        info.strNull = "myString";
        info.tables = new String[][] {{"aaa", "bbb"}, {"ccc", "ddd"}};
        info.singleInfos = new SingleInfo[] {new SingleInfo(), new SingleInfo()};
        info.singleInfoTables = new SingleInfo[][] {{new SingleInfo(), new SingleInfo()}, {new SingleInfo()}};
        
        List<SingleInfo> list = new ArrayList<SingleInfo>();
        list.add(new SingleInfo());
        list.add(new SingleInfo());
        info.singleInfoList = list;
        
        List<Integer> integerList = new ArrayList<Integer>();
        integerList.add(4090);
        integerList.add(4091);
        info.setIntegerList(integerList);
        return info;
    }
}
