package com.bazinga.replay.sharding;

import com.bazinga.util.DateUtil;
import com.dangdang.ddframe.rdb.sharding.api.ShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.SingleKeyTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.router.strategy.SingleKeyShardingAlgorithm;

import java.util.Collection;
import java.util.Date;

public class ShStockOrderShardingAlgorithm implements SingleKeyTableShardingAlgorithm<Date> {

    @Override
    public String doEqualSharding(Collection<String> tableNames, ShardingValue<Date> shardingValue) {
        for (String tableName : tableNames) {
            String format = DateUtil.format(shardingValue.getValue(), DateUtil.yyyy_MM_dd_);
            if (tableName.endsWith(format)) {
                return tableName;
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public Collection<String> doInSharding(Collection<String> collection, ShardingValue<Date> shardingValue) {
        return null;
    }

    @Override
    public Collection<String> doBetweenSharding(Collection<String> collection, ShardingValue<Date> shardingValue) {
        return null;
    }
}
