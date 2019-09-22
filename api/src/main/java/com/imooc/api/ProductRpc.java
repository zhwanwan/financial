package com.imooc.api;

import com.googlecode.jsonrpc4j.JsonRpcService;
import com.imooc.api.domain.ParamInf;
import com.imooc.entity.Product;

import java.util.List;

/**
 * 产品相关的rpc服务
 */
//@JsonRpcService("rpc/products") //修改rpc源码无需配置路径，而使用类全名作为path
@JsonRpcService
public interface ProductRpc {

    List<Product> query(ParamInf req);

    Product findOne(String id);

}
