package com.chongba.utils;

import com.liuche.common.recharge.RechargeRequest;

/**
 * 测试序列化
 */
public class SerializeTest {

    public static void main(String[] args) {
        long start =System.currentTimeMillis();
        for (int i = 0; i <1000000 ; i++) {
            RechargeRequest rechargeRequest =new RechargeRequest();
            com.chongba.utils.JdkSerializeUtil.serialize(rechargeRequest);
        }
        System.out.println(" jdk 花费 "+(System.currentTimeMillis()-start));

        start =System.currentTimeMillis();
        for (int i = 0; i <1000000 ; i++) {
            RechargeRequest rechargeRequest =new RechargeRequest();
            com.chongba.utils.ProtostuffUtil.serialize(rechargeRequest);
        }
        System.out.println(" protostuff 花费 "+(System.currentTimeMillis()-start));
    }
}
