package com.tangyh.lamp.gateway.controller;

import com.tangyh.basic.base.R;
import com.tangyh.basic.base.controller.SuperController;
import com.tangyh.basic.database.mybatis.conditions.Wraps;
import com.tangyh.lamp.gateway.entity.BlockList;
import com.tangyh.lamp.gateway.service.BlockListService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 阻止列表
 * <p>
 * <p>
 * 本来想打造一个zuul 和 gateway 都能共用的限流和阻止列表功能，但由于2者机制不同，zuul服务在使用该功能时，需要自行调整以下2个地方：
 * 1，lamp-ui/src/api/BlockList.js 所有的url增加 /gate 的前缀。 如： /gate/gateway/blockList/page
 * 2，lamp-ui/src/api/RateLimiter.js 所有的url增加 /gate 的前缀。 如： /gate/gateway/rateLimiter/page
 *
 * @author zuihou
 * @date 2020/8/4 上午8:59
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/gateway/blockList")
@Api(value = "BlockListController", tags = "阻止列表")
public class BlockListController extends SuperController<BlockListService, Long, BlockList, BlockList, BlockList, BlockList> {

    @ApiOperation(value = "查询阻止列表是否存在")
    @GetMapping("exist")
    public R<Boolean> exist(@RequestParam(required = false) String ip, @RequestParam String requestUri, @RequestParam String requestMethod) {
        return R.success(baseService.count(Wraps.<BlockList>lbQ()
                .eq(BlockList::getIp, ip).eq(BlockList::getRequestUri, requestUri)
                .eq(BlockList::getRequestMethod, requestMethod)) > 0);
    }


}
