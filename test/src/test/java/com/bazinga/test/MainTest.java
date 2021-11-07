package com.bazinga.test;

import com.bazinga.replay.util.JoinQuantUtil;

import java.io.IOException;


public class MainTest {


    public static void main(String[] args) {

        try {
            String token = JoinQuantUtil.getToken();
            System.out.println(token);

           // String result = JoinQuantUtil.getDragonTiger("123048.XSHE", "2021-11-04", token);
            String result = JoinQuantUtil.getTicks("123048.XSHE", "2021-11-04", token);

            String[] array = result.split(" ");
            for (String s : array) {
                System.out.println(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
