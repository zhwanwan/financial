package com.imooc.seller.service;

import com.imooc.entity.VerificationOrder;
import com.imooc.seller.enums.ChanEnum;
import com.imooc.seller.repositoriesbackup.VerifyRepository;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 */
@Service
public class VerifyService {

    @Autowired
    private VerifyRepository verifyRepository;

    private static DateFormat DAY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static DateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final String END_LINE = System.getProperty("line.separator", "\n");

    /**
     * 生成渠道对账文件
     *
     * @param chanId
     * @param date
     * @return
     */
    public File makeVerificationFile(String rootDir, String chanId, Date date) {
        File path = getPath(rootDir, chanId, date);
        if (path.exists())
            return path;
        else {
            try {
                path.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Date start = getStartOfDay(date);
        Date end = add24Hours(start);
        List<String> orders = verifyRepository.queryVerificationOrders(chanId, start, end);
        String content = String.join(END_LINE, orders);
        FileUtil.writeAsString(path, content);
        return path;
    }

    private File getPath(String rootDir, String chanId, Date date) {
        String name = DAY_FORMAT.format(date) + "-" + chanId + ".txt";
        return Paths.get(rootDir, name).toFile();
    }

    private Date add24Hours(Date start) {
        return new Date(start.getTime() + 1000 * 60 * 60 * 24);
    }

    private Date getStartOfDay(Date day) {
        String start_str = DAY_FORMAT.format(day);
        Date start = null;
        try {
            start = DAY_FORMAT.parse(start_str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return start;
    }

    private static VerificationOrder parseLine(String line) {
        VerificationOrder order = new VerificationOrder();
        String[] props = line.split("\\|");
        order.setOrderId(props[0]);
        order.setOuterOrderId(props[1]);
        order.setChanId(props[2]);
        order.setChanUserId(props[3]);
        order.setProductId(props[4]);
        order.setOrderType(props[5]);
        order.setAmount(new BigDecimal(props[6]));
        try {
            order.setCreateAt(DATETIME_FORMAT.parse(props[7]));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return order;
    }

    /**
     * 保存渠道订单
     *
     * @param chanId
     * @param date
     */
    public void saveChanOrders(String rootDir, String chanId, Date date) {
        ChanEnum conf = ChanEnum.getByChanId(chanId);
        File file = getPath(rootDir, chanId, date);
        if (!file.exists())
            return;
        try {
            String content = FileUtil.readAsString(file);
            String[] lines = content.split(END_LINE);
            List<VerificationOrder> orders = new ArrayList<>();
            for (String line : lines) {
                orders.add(parseLine(line));
            }
            verifyRepository.saveAll(orders);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> verifyOrder(String chanId, Date day) {

        List<String> errors = new ArrayList<>();
        Date start = getStartOfDay(day);
        Date end = add24Hours(start);
        List<String> excessOrders = verifyRepository.queryExcessOrders(chanId, start, end);
        List<String> missOrders = verifyRepository.queryMissOrders(chanId, start, end);
        List<String> differentOrders = verifyRepository.queryDifferentOrders(chanId, start, end);

        errors.add("长款订单号:" + String.join(",", excessOrders));
        errors.add("漏单订单号:" + String.join(",", missOrders));
        errors.add("不一致订单号:" + String.join(",", differentOrders));

        return errors;
    }

}
