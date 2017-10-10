package io.isharing.springddal.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.BASE64Encoder;

public class RouteUtils {
	
    private static final Logger log = LoggerFactory.getLogger(RouteUtils.class);
    
    /**
     * 默认编码
     */
    private final static String encode = "utf-8";
    
    /**
     * 最大资源数
     */
    private final static int resourceMax = 10000;

    /**
     * 获取hashCode
     *
     * @param routeValue
     * @return
     */
    public static int getHashCodeBase64(String routeValue) {
        int hashCode = 0;
        try {
            String pinBase64 = new BASE64Encoder().encode(routeValue.getBytes(encode));
            hashCode = Math.abs(pinBase64.hashCode());
        } catch (Exception e) {
            log.error("hashCode 失败", e);
        }
        return hashCode;
    }

    /**
     * 获取资源码
     *
     * @param routeValue
     * @return
     */
    public static int getResourceCode(String routeValue) {
        int hashCode = RouteUtils.getHashCodeBase64(routeValue);
        int resourceCode = hashCode % resourceMax;
        return resourceCode;
    }

    public static void main(String args[]) {
        String payid = "140331160123935469773";

        String resource = payid.substring(payid.length() - 4);

        int routeFieldInt = Integer.valueOf(resource);
        System.out.println("1# routeFieldInt="+routeFieldInt);
        routeFieldInt = getResourceCode(resource);
        System.out.println("2# routeFieldInt="+routeFieldInt);
        int mode = 6 * 200;
        int dbIndex = routeFieldInt % mode / 200;
        int tbIndex = routeFieldInt % 200;

        System.out.println(dbIndex + "-->" + tbIndex);
    }
}
