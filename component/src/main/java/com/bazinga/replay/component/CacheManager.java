package com.bazinga.replay.component;

import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CacheManager implements InitializingBean {

    @Autowired
    private ThsLoginComponent thsLoginComponent;
    @Override
    public void afterPropertiesSet() throws Exception {

        TdxHqUtil.initConnect();
        log.info("初始化L1行情连接完成>");
        //thsLoginComponent.thsLogin();
        log.info("初始化ths连接完成>");

    }

}
