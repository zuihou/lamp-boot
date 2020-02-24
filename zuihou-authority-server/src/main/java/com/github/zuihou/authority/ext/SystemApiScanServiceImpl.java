package com.github.zuihou.authority.ext;

import cn.hutool.core.bean.BeanUtil;
import com.github.zuihou.authority.service.auth.SystemApiService;
import com.github.zuihou.scan.model.SystemApiScanSaveDTO;
import com.github.zuihou.scan.service.SystemApiScanService;

/**
 * 本地实现
 *
 * @author zuihou
 * @date 2020年02月24日16:42:56
 */
public class SystemApiScanServiceImpl implements SystemApiScanService {
    private SystemApiService systemApiService;

    public SystemApiScanServiceImpl(SystemApiService systemApiService) {
        this.systemApiService = systemApiService;
    }

    @Override
    public Boolean batchSave(SystemApiScanSaveDTO data) {
        return systemApiService.batchSave(BeanUtil.toBean(data, com.github.zuihou.authority.dto.auth.SystemApiScanSaveDTO.class));
    }
}
