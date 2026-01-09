package com.company.aegis.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.company.aegis.modules.system.entity.SysLog;

public interface SysLogService extends IService<SysLog> {
    void saveLog(String username, String operation, String method, String params, String ip, Long time);
}
