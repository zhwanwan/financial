package com.imooc.manager.error;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author zhwanwan
 * @create 2019-09-19 9:48 AM
 */
public class MyErrorController extends BasicErrorController {

    public MyErrorController(ErrorAttributes errorAttributes, ErrorProperties errorProperties) {
        super(errorAttributes, errorProperties);
    }

    public MyErrorController(ErrorAttributes errorAttributes, ErrorProperties errorProperties, List<ErrorViewResolver> errorViewResolvers) {
        super(errorAttributes, errorProperties, errorViewResolvers);
    }


    /*{
            x"timestamp": "2019-09-19 09:50:12.247",
            x"status": 500,
            x"error": "Internal Server Error",
            x"exception": "java.lang.IllegalArgumentException",
            "message": "编号不可为空",
            x"path": "/manager/products"
            +code
    }*/

    @Override
    protected Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace) {
        Map<String, Object> attributes = super.getErrorAttributes(request, includeStackTrace);
        attributes.remove("timestamp");
        attributes.remove("status");
        attributes.remove("error");
        attributes.remove("exception");
        attributes.remove("path");

        String errorCode = (String) attributes.get("message");
        ErrorEnum errorEnum = ErrorEnum.getByCode(errorCode);
        attributes.put("message", errorEnum.getMessage());
        attributes.put("code", errorEnum.getCode());
        attributes.put("canRetry", errorEnum.isCanRetry());

        return attributes;
    }
}
