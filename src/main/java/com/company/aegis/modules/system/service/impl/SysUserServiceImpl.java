package com.company.aegis.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.aegis.modules.system.entity.SysUser;
import com.company.aegis.modules.system.mapper.SysUserMapper;
import com.company.aegis.modules.system.service.SysUserService;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
}
