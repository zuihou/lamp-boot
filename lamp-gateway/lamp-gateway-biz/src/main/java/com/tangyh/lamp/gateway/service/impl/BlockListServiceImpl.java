package com.tangyh.lamp.gateway.service.impl;


import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.tangyh.basic.base.service.SuperServiceImpl;
import com.tangyh.basic.cache.model.CacheKey;
import com.tangyh.basic.cache.repository.CachePlusOps;
import com.tangyh.lamp.common.cache.gateway.BlockListCacheKeyBuilder;
import com.tangyh.lamp.common.cache.gateway.BlockListIdCacheKeyBuilder;
import com.tangyh.lamp.gateway.dao.BlockListMapper;
import com.tangyh.lamp.gateway.entity.BlockList;
import com.tangyh.lamp.gateway.service.BlockListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 阻止列表
 *
 * @author zuihou
 * @date 2020/8/4 下午12:22
 */
@Slf4j
@Service

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockListServiceImpl extends SuperServiceImpl<BlockListMapper, BlockList> implements BlockListService {

    private final CachePlusOps cacheOps;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(BlockList model) {
        int bool = baseMapper.insert(model);
        this.saveBlockList(model);
        return SqlHelper.retBool(bool);
    }

    public void saveBlockList(BlockList blockList) {
        CacheKey idKey = new BlockListIdCacheKeyBuilder().key(blockList.getId());
        CacheKey key = new BlockListCacheKeyBuilder().key(blockList.getIp());
        cacheOps.set(idKey, blockList);
        cacheOps.sAdd(key, blockList.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(BlockList blockList) {

        CacheKey idKey = new BlockListIdCacheKeyBuilder().key(blockList.getId());
        CacheKey key = new BlockListCacheKeyBuilder().key(blockList.getIp());
        cacheOps.del(idKey);
        cacheOps.sRem(key, blockList.getId());

        int bool = baseMapper.updateById(blockList);
        cacheOps.set(idKey, blockList);
        cacheOps.sAdd(key, blockList.getId());
        return SqlHelper.retBool(bool);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        if (idList.isEmpty()) {
            return true;
        }

        List<BlockList> blockLists = listByIds(idList);
        if (blockLists.isEmpty()) {
            return true;
        }
        baseMapper.deleteBatchIds(idList);

        blockLists.forEach(this::removeBlockList);
        return true;
    }

    public void removeBlockList(BlockList blockList) {
        CacheKey idKey = new BlockListIdCacheKeyBuilder().key(blockList.getId());
        CacheKey key = new BlockListCacheKeyBuilder().key(blockList.getIp());

        cacheOps.del(idKey);
        cacheOps.sRem(key, blockList.getId());
    }

    @Override
    public Set<Object> findBlockList(String ip) {
        CacheKey key = new BlockListCacheKeyBuilder().key(ip);
        Set<Object> members = cacheOps.sMembers(key);
        if (members.isEmpty()) {
            return new HashSet<>(16);
        }
        return members.stream().map(id -> cacheOps.get(new BlockListIdCacheKeyBuilder().key(id)))
                .filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override
    public Set<Object> findBlockList() {
        CacheKey key = new BlockListCacheKeyBuilder().key();
        Set<Object> members = cacheOps.sMembers(key);
        if (members.isEmpty()) {
            return Collections.emptySet();
        }
        return members.stream().map(id -> cacheOps.get(new BlockListIdCacheKeyBuilder().key(id)))
                .filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override
    public void loadAllBlockList() {
        List<BlockList> list = list();

        list.forEach(blockList -> {
            CacheKey idKey = new BlockListIdCacheKeyBuilder().key(blockList.getId());
            CacheKey key = new BlockListCacheKeyBuilder().key(blockList.getIp());

            cacheOps.set(idKey, blockList);
            cacheOps.sAdd(key, blockList.getId());
        });
    }
}
