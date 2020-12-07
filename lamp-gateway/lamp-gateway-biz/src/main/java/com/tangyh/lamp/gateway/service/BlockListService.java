package com.tangyh.lamp.gateway.service;

import com.tangyh.basic.base.service.SuperService;
import com.tangyh.lamp.gateway.entity.BlockList;

import java.util.Set;

/**
 * 阻止列表
 *
 * @author zuihou
 * @date 2020/8/4 下午12:22
 */
public interface BlockListService extends SuperService<BlockList> {
    /**
     * 根据IP查询阻止列表配置
     *
     * @param ip 请求端ip
     * @return 阻止列表
     */
    Set<Object> findBlockList(String ip);

    /**
     * 查询阻止列表配置
     *
     * @return 阻止列表
     */
    Set<Object> findBlockList();

    /**
     * 加载所有的阻止列表到缓存
     */
    void loadAllBlockList();
}
