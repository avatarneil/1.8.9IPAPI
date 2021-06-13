package com.nur.ipapi;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class IPHandler {
    public final static Map<InetAddress, IPInfo> ipCache = new HashMap<InetAddress, IPInfo>();

    public static boolean isCached(InetAddress ip) {
        return ipCache.containsKey(ip);
    }

    public static boolean isCached(String ip) {
        try {
            return isCached(Inet4Address.getByName(ip));
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static IPInfo getCached(InetAddress ip) {
        return ipCache.get(ip);
    }

    public static IPInfo getCached(String ip) {
        try {
            return ipCache.get(Inet4Address.getByName(ip));
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void cache(InetAddress ip, IPInfo result) {
        ipCache.put(ip, result);
    }

    public static void cache(String ip, IPInfo result) {
        try {
            cache(Inet4Address.getByName(ip), result);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
