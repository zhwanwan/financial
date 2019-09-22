package com.imooc.seller.service;

import com.imooc.api.events.ProductStatusEvent;
import com.imooc.entity.Product;
import com.imooc.entity.enums.ProductStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 产品服务
 * <p>
 * ApplicationListener容器初始化之后触发事件
 */
@Service
public class ProductRpcService implements ApplicationListener<ContextRefreshedEvent> {

    private static Logger LOG = LoggerFactory.getLogger(ProductRpcService.class);

    //Consumer.cache.VirtualTopic.PRODUCT_STATUS
    private static final String MQ_DESTINATION = "Consumer.cache.VirtualTopic.PRODUCT_STATUS";

    @Autowired
    private ProductCache productCache;

    public Product findOne(String id) {
        Product product = productCache.readCache(id);
        if (product == null)
            productCache.removeCache(id);
        return product;
    }

    public List<Product> findAll() {
        return productCache.readAllCache();
    }

    //@PostConstruct
    public void testFindAll() {
        findOne("0001");
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        List<Product> products = findAll();
        products.forEach(product -> productCache.putCache(product));
    }

    @JmsListener(destination = MQ_DESTINATION)
    void updateCache(ProductStatusEvent event) {
        LOG.info("receive event:{}", event);
        productCache.removeCache(event.getId());
        if (ProductStatus.IN_SELL.equals(event.getStatus()))
            productCache.readCache(event.getId());
    }
}
