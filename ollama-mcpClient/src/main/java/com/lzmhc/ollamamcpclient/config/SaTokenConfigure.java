//package com.lzmhc.ollamamcpclient.config;
//import cn.dev33.satoken.context.SaHolder;
//import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
//import cn.dev33.satoken.reactor.filter.SaReactorFilter;
//import cn.dev33.satoken.router.SaHttpMethod;
//import cn.dev33.satoken.router.SaRouter;
//import cn.dev33.satoken.stp.StpLogic;
//import cn.dev33.satoken.stp.StpUtil;
//import cn.dev33.satoken.util.SaResult;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.reactive.config.WebFluxConfigurer;
//
///**
// * 注解鉴权
// * 注册拦截器
// */
//
///**
// * [Sa-Token 权限认证] 全局配置类
// */
//@Configuration
//public class SaTokenConfigure implements WebFluxConfigurer{
//    /**
//     * 注册 [Sa-Token全局过滤器]
//     */
//    @Bean
//    public SaReactorFilter getSaReactorFilter() {
//        return new SaReactorFilter()
//                // 指定 [拦截路由]
//                .addInclude("/**")    /* 拦截所有path */
//                // 指定 [放行路由]
//                .addExclude("/static/**", "/oauth2/qq/login", "/connection")
//                // 指定[认证函数]: 每次请求执行
//                .setAuth(obj -> {
//                    System.out.println("---------- sa全局认证");
//                    SaRouter.match("/**", () -> StpUtil.checkLogin());
//
//                })
//                // 指定[异常处理函数]：每次[认证函数]发生异常时执行此函数
//                .setError(e -> {
//                    System.out.println("---------- sa全局异常 ");
//                    return SaResult.error(e.getMessage());
//                })
//                // 前置函数：在每次认证函数之前执行
//                .setBeforeAuth(obj -> {
//                    SaHolder.getResponse()
//                            // ---------- 设置跨域响应头 ----------
//                            // 允许指定域访问跨域资源
//                            .setHeader("Access-Control-Allow-Origin", "*")
//                            // 允许所有请求方式
//                            .setHeader("Access-Control-Allow-Methods", "*")
//                            // 允许的header参数
//                            .setHeader("Access-Control-Allow-Headers", "*")
//                            // 有效时间
//                            .setHeader("Access-Control-Max-Age", "3600")
//                    ;
//                    // 如果是预检请求，则立即返回到前端
//                    SaRouter.match(SaHttpMethod.OPTIONS)
//                            .free(r -> System.out.println("--------OPTIONS预检请求，不做处理"))
//                            .back();
//                })
//                ;
//    }
//    // Sa-Token 整合 jwt (Simple 简单模式)
//    @Bean
//    public StpLogic getStpLogicJwt() {
//        return new StpLogicJwtForSimple();
//    }
//}
//
