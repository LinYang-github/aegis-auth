package com.company.aegis.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.aegis.modules.system.entity.SysApplication;
import com.company.aegis.modules.system.mapper.SysApplicationMapper;
import com.company.aegis.modules.system.service.SysApplicationService;
import org.springframework.stereotype.Service;

@Service
public class SysApplicationServiceImpl extends ServiceImpl<SysApplicationMapper, SysApplication>
        implements SysApplicationService {
}
