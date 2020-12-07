package com.tangyh.lamp.gateway.runner;

import com.tangyh.basic.context.ContextUtil;
import com.tangyh.basic.database.properties.DatabaseProperties;
import com.tangyh.basic.database.properties.MultiTenantType;
import com.tangyh.lamp.gateway.service.BlockListService;
import com.tangyh.lamp.gateway.service.RateLimiterService;
import com.tangyh.lamp.tenant.dao.InitDatabaseMapper;
import com.tangyh.lamp.tenant.enumeration.TenantStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 应用启动成功后执行， 该类会在InitDatabaseOnStarted之后执行
 *
 * @author zuihou
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartedUpRunner implements ApplicationRunner {

    private final InitDatabaseMapper initDbMapper;
    private final BlockListService blockListService;
    private final RateLimiterService rateLimiterService;
    private final DatabaseProperties databaseProperties;

    @Override
    public void run(ApplicationArguments args) {
        if (MultiTenantType.NONE.eq(databaseProperties.getMultiTenantType())) {
            blockListService.loadAllBlockList();
            rateLimiterService.loadAllRateLimiters();
        } else {
            List<String> tenantCodeList = initDbMapper.selectTenantCodeList(TenantStatusEnum.NORMAL.name(), null);
            tenantCodeList.forEach((tenantCode) -> {
                ContextUtil.setTenant(tenantCode);
                blockListService.loadAllBlockList();
                rateLimiterService.loadAllRateLimiters();
            });
        }
    }
}
