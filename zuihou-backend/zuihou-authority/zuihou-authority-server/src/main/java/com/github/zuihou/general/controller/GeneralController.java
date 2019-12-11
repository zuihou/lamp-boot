package com.github.zuihou.general.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.github.zuihou.authority.entity.core.Org;
import com.github.zuihou.authority.entity.core.Station;
import com.github.zuihou.authority.enumeration.auth.AuthorizeType;
import com.github.zuihou.authority.enumeration.auth.Sex;
import com.github.zuihou.authority.enumeration.common.LogType;
import com.github.zuihou.authority.enumeration.defaults.TenantStatusEnum;
import com.github.zuihou.authority.enumeration.defaults.TenantTypeEnum;
import com.github.zuihou.authority.service.common.DictionaryItemService;
import com.github.zuihou.authority.service.core.OrgService;
import com.github.zuihou.authority.service.core.StationService;
import com.github.zuihou.base.BaseEnum;
import com.github.zuihou.base.R;
import com.github.zuihou.common.constant.DictionaryCode;
import com.github.zuihou.common.enums.HttpMethod;
import com.github.zuihou.context.BaseContextConstants;
import com.github.zuihou.context.BaseContextHandler;
import com.github.zuihou.database.mybatis.auth.DataScopeType;
import com.github.zuihou.file.enumeration.DataType;
import com.github.zuihou.msgs.enumeration.MsgsBizType;
import com.github.zuihou.msgs.enumeration.MsgsCenterType;
import com.github.zuihou.sms.enumeration.ProviderType;
import com.github.zuihou.sms.enumeration.SendStatus;
import com.github.zuihou.sms.enumeration.SourceType;
import com.github.zuihou.sms.enumeration.TaskStatus;
import com.github.zuihou.utils.MapHelper;
import com.google.common.collect.ImmutableMap;

import cn.hutool.extra.servlet.ServletUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通用 控制器
 *
 * @author zuihou
 * @date 2019/07/25
 */
@Slf4j
@RestController
@Api(value = "Common", tags = "通用Controller")
public class GeneralController {

    @Autowired
    private OrgService orgService;
    @Autowired
    private StationService stationService;
    @Autowired
    private DictionaryItemService dictionaryItemService;

    @ApiOperation(value = "获取当前系统所有枚举", notes = "获取当前系统所有枚举")
    @GetMapping("/enums")
    public R<Map<String, Map<String, String>>> enums() {
        return R.success(getEnums());
    }

    private Map getEnums() {
        Map<String, Map<String, String>> map = new HashMap<>(16, 0.95F);
        map.put(HttpMethod.class.getSimpleName(), BaseEnum.getMap(HttpMethod.values()));
        map.put(DataScopeType.class.getSimpleName(), BaseEnum.getMap(DataScopeType.values()));
        map.put(LogType.class.getSimpleName(), BaseEnum.getMap(LogType.values()));
        map.put(AuthorizeType.class.getSimpleName(), BaseEnum.getMap(AuthorizeType.values()));
        map.put(Sex.class.getSimpleName(), BaseEnum.getMap(Sex.values()));
        map.put(TenantTypeEnum.class.getSimpleName(), BaseEnum.getMap(TenantTypeEnum.values()));
        map.put(TenantStatusEnum.class.getSimpleName(), BaseEnum.getMap(TenantStatusEnum.values()));

        map.put(MsgsCenterType.class.getSimpleName(), BaseEnum.getMap(MsgsCenterType.values()));
        map.put(MsgsBizType.class.getSimpleName(), BaseEnum.getMap(MsgsBizType.values()));
        map.put(ProviderType.class.getSimpleName(), BaseEnum.getMap(ProviderType.values()));
        map.put(SourceType.class.getSimpleName(), BaseEnum.getMap(SourceType.values()));
        map.put(SendStatus.class.getSimpleName(), BaseEnum.getMap(SendStatus.values()));
        map.put(TaskStatus.class.getSimpleName(), BaseEnum.getMap(TaskStatus.values()));

        map.put(DataType.class.getSimpleName(), BaseEnum.getMap(DataType.values()));
        return map;
    }

    @ApiOperation(value = "获取当前系统所有数据字典和枚举", notes = "获取当前系统所有数据字典和枚举")
    @GetMapping("/dictionary/enums")
    public R<Map<String, Map<String, String>>> dictionaryAndEnum(HttpServletRequest request) {
        BaseContextHandler.setTenant(request.getHeader(BaseContextConstants.TENANT));
        Map<String, Map<String, String>> map = new HashMap<>(4);

        map.putAll(getEnums());

        //整个系统的数据字典
        Map<String, Map<String, String>> itemMap = dictionaryItemService.map(DictionaryCode.ALL);
        if (!itemMap.isEmpty()) {
            map.putAll(itemMap);
        }
        return R.success(map);
    }

    /**
     * 查询所有组织
     *
     * @return 查询结果
     */
    @ApiOperation(value = "查询所有组织", notes = "查询所有组织")
    @GetMapping("/orgs")
    public R<Map<String, Map<Long, String>>> orgs() {
        Map<String, Map<Long, String>> map = new HashMap<>(2);
        List<Station> stationList = stationService.list();
        List<Org> orgList = orgService.list();
        ImmutableMap<Long, String> stationMap = MapHelper.uniqueIndex(stationList, Station::getId, Station::getName);
        ImmutableMap<Long, String> orgMap = MapHelper.uniqueIndex(orgList, Org::getId, Org::getName);
        map.put(Org.class.getSimpleName(), orgMap);
        map.put(Station.class.getSimpleName(), stationMap);
        return R.success(map);
    }

    /**
     * 测试网关熔断和超时，
     *
     * @param millis
     * @param request
     * @return
     */
    @GetMapping("/test")
    @SneakyThrows
    public R<Object> test(@RequestParam(value = "millis", defaultValue = "29") Long millis, HttpServletRequest request) {
        String clientIP = ServletUtil.getClientIP(request);
        log.info("clientIP={}", clientIP);
        Thread.sleep(millis);
        return R.success(clientIP);
    }

}
