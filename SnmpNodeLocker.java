package com.zte.ums.an.commonsh.localstore.framework.common;
import java.util.HashSet;
import java.util.Set;

import com.zte.ums.api.common.snmpnode.ppu.entity.SnmpNode;

/**
    * <ul><li>文件描述: /li>
    * <li>    完成日期: 2015.7.3</li></ul>
    * @author ChenDuoduo_10087118
    */
public class SnmpNodeLocker
{
    private Set<String> lockedNEs = new HashSet<String>();
    private static SnmpNodeLocker locker = new SnmpNodeLocker();
    
    private SnmpNodeLocker()
    {
    }
    
    public static SnmpNodeLocker getInstance()
    {
        return locker;
    }

    public void getLock(SnmpNode snmpNode)
    {
        waitForRelease(snmpNode);
        lock(snmpNode);
    }

    public synchronized void unlock(SnmpNode snmpNode)
    {
        lockedNEs.remove(snmpNode.getIpAddress());
        this.notifyAll();
    }

    private void lock(SnmpNode snmpNode)
    {
        lockedNEs.add(snmpNode.getIpAddress());
    }

    private boolean isLocked(SnmpNode snmpNode)
    {
        return lockedNEs.contains(snmpNode.getIpAddress());
    }

    private void waitForRelease(SnmpNode snmpNode)
    {
        while(isLocked(snmpNode))
        {
            synchronized(this)
            {
                try
                {
                    this.wait();
                }
                catch(InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

        }
    }
}