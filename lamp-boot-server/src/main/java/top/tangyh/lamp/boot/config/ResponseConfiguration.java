package top.tangyh.lamp.boot.config;

import top.tangyh.basic.boot.handler.AbstractGlobalResponseBodyAdvice;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.Servlet;

import static top.tangyh.lamp.common.constant.BizConstant.BUSINESS_PACKAGE;

/**
 * 全局统一返回值 包装器
 *
 * @author zuihou
 * @date 2020/12/30 2:48 下午
 */
@Configuration
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@RestControllerAdvice(basePackages = { BUSINESS_PACKAGE })
public class ResponseConfiguration extends AbstractGlobalResponseBodyAdvice {
}
