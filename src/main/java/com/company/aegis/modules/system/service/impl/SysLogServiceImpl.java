package com.company.aegis.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.aegis.modules.system.entity.SysLog;
import com.company.aegis.modules.system.mapper.SysLogMapper;
import com.company.aegis.modules.system.service.SysLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@lombok.extern.slf4j.Slf4j
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveLog(String username, String operation, String method, String params, String ip, Long time) {
        log.info("Saving SysLog - User: {}, Operation: {}, Method: {}, IP: {}", username, operation, method, ip);
        SysLog sysLog = new SysLog();
        sysLog.setUsername(username);
        sysLog.setOperation(operation);
        sysLog.setMethod(method);
        sysLog.setParams(params);
        sysLog.setIp(ip);
        sysLog.setTime(time);
        sysLog.setCreateTime(LocalDateTime.now());
        this.save(sysLog);
    }
}
