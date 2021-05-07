package com.bazinga.replay.component;

import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CacheManager implements InitializingBean {


    @Override
    public void afterPropertiesSet() throws Exception {

        TdxHqUtil.initConnect();
        log.info("初始化L1行情连接完成>");
    }
}
