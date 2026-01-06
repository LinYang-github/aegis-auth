package com.company.aegis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.company.aegis.modules.*.mapper") // 扫描 Mapper 接口
public class AegisAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AegisAuthApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  Aegis Auth Service Started Successfully!   ლ(´ڡ`ლ)ﾞ");
    }
}