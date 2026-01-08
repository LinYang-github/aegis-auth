package com.company.aegis.common.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.company.aegis.modules.*.mapper")
public class MybatisConfig {
}
