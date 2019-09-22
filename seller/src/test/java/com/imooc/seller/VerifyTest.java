package com.imooc.seller;

import com.imooc.seller.enums.ChanEnum;
import com.imooc.seller.service.VerifyService;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * 对账测试
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VerifyTest {

    @Autowired
    private VerifyService verifyService;

    @Test
    public void makeVerificationTest() {
        Date date = new GregorianCalendar(2018, 11, 30).getTime();
        File file = verifyService.makeVerificationFile("D:\\verification", "11", date);
        System.out.println("file.getAbsoluteFile() = " + file.getAbsoluteFile());
    }

    @Test
    public void saveOrdersTest() {
        Date date = new GregorianCalendar(2018, 11, 30).getTime();
        ChanEnum chan = ChanEnum.getByChanId("11");
        verifyService.saveChanOrders(chan.getRootDir(), chan.getChanId(), date);
    }

}
