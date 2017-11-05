package com.zte.ums.an.commonsh.northbound.atomicimpl;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.zte.ums.an.commonsh.api.northbound.conf.subscriber.SubscriberConfAtomicI;
import com.zte.ums.an.commonsh.api.northbound.conf.subscriber.VpnConfAtomicI;
import com.zte.ums.an.commonsh.entity.northbound.AtomicResult;
import com.zte.ums.an.commonsh.northbound.tl1agent.cmcc.SnmpNodeAdaptor;
import com.zte.ums.an.commonsh.util.emf.fastping.FastSnmpPing;
import com.zte.ums.an.commonsh.util.tools.ListOp;
import com.zte.ums.an.commonsh.util.tools.ListOp.Matcher;
import com.zte.ums.api.common.snmpnode.ppu.entity.SnmpNode;
import com.zte.ums.n3common.api.ZXLogger;
import com.zte.ums.n3common.api.util.CommonUtil;
import com.zte.ums.n3common.api.util.TestDebug;
import com.zte.ums.uep.api.ServiceAccess;

/**
 * <p>�ļ�����: AtomicIGetter.java</p>
 * <p>�ļ�����: ԭ�ӽӿ�ʵ����ȡ��</p>
 * <p>��Ȩ����: ��Ȩ����(C)2001-2010</p>
 * <p>��    ˾: ����ͨѶ�ɷ����޹�˾</p>
 * <p>����ժҪ: </p>
 * <p>����˵��: </p>
 * <p>������ڣ�2008��1��14��</p>
 * <p>�޸ļ�¼1:</p>
 * <pre>
 *    �޸����ڣ�
 *    �� �� �ţ�
 *    �� �� �ˣ�
 *    �޸����ݣ�
 * </pre>
 * <p>�޸ļ�¼2��</p>
 * @version 1.0
 * @author  ��⻪
 */
public class AtomicIGetter
{
//****** �����: �������� *******************************************************************************/

    /** ���Դ�ӡ */
    private static Logger logger = ZXLogger.getLogger("AtomicIGetter");

    /** ������Ԫ���ͺ�ԭ�ӽӿ�ӳ���ϵ�Ĺ�ϣ��KeyΪ��Ԫ�����ַ�����ValueΪԭ�ӽӿڹ�ϣ�� */
    private static Hashtable hashNeTypeAndAtomicI = null; //new Hashtable();
    private static String atomic_xml_path;

    private static EnumerationFilter<Class> snmpOperIntfFilter = new EnumerationFilter<Class>();
    
    static
    {
        snmpOperIntfFilter.discard(SubscriberConfAtomicI.class);
        snmpOperIntfFilter.discard(VpnConfAtomicI.class);
    }

    //****** �����: �������� *******************************************************************************/

    public static void setPath(String path)
    {
        AtomicIGetter.atomic_xml_path = path;
    }

    /**
     * ������Ԫ��SnmpNode��ԭ�ӽӿ���������ȡ���ԭ�ӽӿڵ�ʵ�ַ���
     * @param snmpNode ��Ԫ�ڵ���Ϣ
     * @param atomicIName ԭ�ӽӿ�����
     * @return ԭ�ӽӿڵ�ʵ�ֵ�ʵ����
     */
    public synchronized static Object getAtomicImpl(SnmpNode snmpNode, String atomicIName)
    {
        return getAtomicImpl(snmpNode.getMoc(), atomicIName);
    }

    public synchronized static Object getAtomicImpl(String strNeType, String atomicIName)
    {
        try
        {
            if(hashNeTypeAndAtomicI == null)
            {
                readConfFile();
            }

            Object valueObj = hashNeTypeAndAtomicI.get(strNeType);
            if(valueObj == null)
            {
                logger.error(strNeType + " may not support this operation.");
                return null;
            }

            Hashtable hashAtomicIAndImplClassName = (Hashtable)valueObj;
            valueObj = hashAtomicIAndImplClassName.get(atomicIName);
            if(valueObj == null)
            {
                logger.error(strNeType + " may not support this operation," + atomicIName);
                return null;
            }
            return getAtomicIInstance(atomicIName, Class.forName((String)valueObj));
        }
        catch(Exception ex)
        {
            logger.error("getAtomicImpl exception: ", ex);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Object getAtomicIInstance(String intfShortName, Class intfImpl) 
                    throws InstantiationException, IllegalAccessException
    {
        Class atomicIntf = findImplementedInterface(intfImpl, intfShortName);
        Object instance = intfImpl.newInstance();
        
        return isSnmpOper(atomicIntf)? AtomicIProxyFactory.getProxyForLinkStatusCheck(atomicIntf, instance)
                                     : instance;
    }

//****** �����: �ڲ����� *******************************************************************************/

    private static boolean isSnmpOper(Class intf)
    {
        return snmpOperIntfFilter.filter(intf).size() != 0;
    }

    private static Class findImplementedInterface(Class clzz, String intfShortName)
    {
        if(clzz == null)
        {
            return null;
        }
        
        for(Class intf : clzz.getInterfaces())
        {
            if(intf.getName().endsWith(intfShortName))
            {
                return intf;
            }
        }
        return findImplementedInterface(clzz.getSuperclass(), intfShortName);
    }

    private static void readConfFile()
    {
        hashNeTypeAndAtomicI = new Hashtable();
        String filePath = "";
        if(!TestDebug.isDebug())
        {
            if(atomic_xml_path == null || atomic_xml_path.length() == 0)
            {
                filePath = ServiceAccess.getSystemSupportService().getDeployedPar("an-commonsh-northbound-emf").getBaseDir()
                + File.separator + "conf" + File.separator;
            }
            else
            {
                filePath = atomic_xml_path + File.separator + "conf" + File.separator;
            }
        }
        else
        {
            filePath = CommonUtil.getNetNumenHomeDir() 
            + "/ums-server/procs/ppus/an.ppu/an-commonsh.pmu/an-commonsh-northbound-emf.par/conf/";
        }

        try
        {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(new File(filePath + "an-commonsh-atomic.xml"));
            Element rootE = doc.getRootElement();
            Element neTypeListE = rootE.getChild("netype-list");

            List listNeType = neTypeListE.getChildren("netype");
            Iterator itListNeType = listNeType.iterator();
            while(itListNeType.hasNext())
            {
                Element neTypeE = (Element)itListNeType.next();
                String strNeType = neTypeE.getAttributeValue("type");
                Hashtable hashAtomicIAndImplClassName = new Hashtable();
                List listAtomicI = neTypeE.getChildren("ao");
                Iterator itListAtomicI = listAtomicI.iterator();
                while(itListAtomicI.hasNext())
                {
                    Element atomicIE = (Element)itListAtomicI.next();
                    String atomicIName = atomicIE.getAttributeValue("name");
                    String implClassName = atomicIE.getAttributeValue("proc");
                    hashAtomicIAndImplClassName.put(atomicIName, implClassName);
                }
                String[] arrayStrNeType = strNeType.split(",");
                for(int i = 0; i < arrayStrNeType.length; i++)
                {
                    hashNeTypeAndAtomicI.put(arrayStrNeType[i].trim(), hashAtomicIAndImplClassName);
                }
            }
        }
        catch(Exception ex)
        {
            logger.error("readConfFile exception: ", ex);
        }
    }
}

/**
 * <li>�ļ�����: AtomicIProxyFactory</li>r
 */

class AtomicIProxyFactory
{
    @SuppressWarnings("unchecked")
    public static<T> T getProxyForLinkStatusCheck(Class<T> intf, final T instance)
    {
        return (T)Proxy.newProxyInstance(instance.getClass().getClassLoader(), 
                                         new Class[]{intf}, 
                                         new InvocationHandler(){
                                            @Override
                                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                                            {
                                                if(!checkNeLinkStatus(method, args))
                                                {
                                                    AtomicResult res = new AtomicResult();
                                                    res.errorCode = AtomicIConst.ERROR_CODE_DEVICE_NO_RESPONSE;
                                                    res.atomicErrorCode = AtomicIConst.ERROR_CODE_DEVICE_NO_RESPONSE;
                                                    res.additionalInfo = "No SNMP response for NE link anomaly";
                                                    return res;
                                                }
                                                return method.invoke(instance, args);
                                            }});
    }

    private static boolean checkNeLinkStatus(Method method, Object[] args)
    {
        SnmpNode ne = findNe(args);
        if(ne == null)
        {
            return true;
        }
        return isLinkUp(ne);
    }
    
    private static boolean isLinkUp(SnmpNode ne)
    {
        return new FastSnmpPing().ping(ne);
    }
    
    private static SnmpNode findNe(Object[] args)
    {
        for(Object arg : args)
        {
            if(arg instanceof SnmpNode)
            {
                if(arg.getClass() == SnmpNodeAdaptor.class)
                {
                    return ((SnmpNodeAdaptor)arg).getSnmpNode();
                }
                return (SnmpNode)arg;
            }
        }
        return null;
    }
}

/**
 * <li>�ļ�����: EnumerationFilter</li>
 * @param <T> declares type of elements.
 */

class EnumerationFilter<T>
{
    private List<T> discards = new ArrayList<T>();

    public List<T> filter(T...elements)
    {
        return ListOp.filter((List<T>)Arrays.asList(elements), notInDiscards());
    }

    private Matcher<T> notInDiscards()
    {
        return new Matcher<T>(){
            @Override
            public boolean match(T element)
            {
                return !discards.contains(element);
            }};
    }
    
    public void discard(T...elements)
    {
        for(T m : elements)
        {
            discards.add(m);
        }
    }
}