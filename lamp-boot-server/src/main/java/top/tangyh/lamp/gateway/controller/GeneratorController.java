package top.tangyh.lamp.gateway.controller;

import cn.hutool.core.map.MapUtil;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import top.tangyh.basic.base.R;
import top.tangyh.lamp.model.vo.result.Option;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 常用Controller
 *
 * @author zuihou
 * @date 2019-06-21 18:22
 */
@Controller
public class GeneratorController {

    @Value("${spring.application.name:}")
    private String application;

    @ResponseBody
    @Operation(summary = "查询在线服务的前缀")
    @GetMapping("/gateway/findOnlineServicePrefix")
    public R<Map<String, String>> findOnlineServicePrefix() {

        Map<String, String> map = MapUtil.newHashMap();
        map.put(application, "base");
        return R.success(map);
    }

    @ResponseBody
    @Operation(summary = "查询在线服务")
    @GetMapping("/gateway/findOnlineService")
    public R<List<Option>> findOnlineService() {
        List<Option> list = new ArrayList();

        list.add(Option.builder()
                .value("base").text(application).label(application)
                .build());

        return R.success(list);
    }
}
