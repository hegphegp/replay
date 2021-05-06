
CREATE TABLE `ticket_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `ticker` varchar(30) NOT NULL COMMENT '股票代码',
  `ticket_name` varchar(60) NOT NULL COMMENT '股票名称',
  `exchange_type` tinyint(2) NOT NULL COMMENT '交易所类型 上海 1， 深圳 2， 未知 3',
  `ticker_type` tinyint(2) DEFAULT NULL COMMENT '合约类型  1 股票 2 其他',
  `buy_qty_unit` int(8) NOT NULL COMMENT '合约最小交易量',
  `upper_limit_price` decimal(10,2) NOT NULL COMMENT '涨停板价',
  `lower_limit_price` decimal(10,2) NOT NULL COMMENT '跌停板价',
  `pre_close_price` decimal(10,2) NOT NULL COMMENT '昨收盘价',
  `price_tick` decimal(10,2) NOT NULL COMMENT '最小变动价位',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8 COMMENT='股票信息';

CREATE TABLE `depth_market_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `ask` varchar(100) NOT NULL COMMENT '10档申卖价',
  `ask_qty` varchar(10) NOT NULL COMMENT '十档申卖量',
  `avg_price` decimal(10,2) NOT NULL COMMENT '当日均价=(turnover/qty)',
  `bid` varchar(100) DEFAULT NULL COMMENT '十档申买价',
	`bid_qty` varchar(10) NOT NULL COMMENT '十档申买量',
  `close_price`decimal(10,2) NOT NULL COMMENT '今收盘',
  `curr_delta` decimal(10,2) NOT NULL COMMENT '预留',
	`dataTime` TIMESTAMP COMMENT '行情时间',
  `exchange_type` TINYINT(2) NOT NULL COMMENT '交易所类型 上海 1， 深圳 2， 未知 3',
  `high_price` decimal(10,2) NOT NULL COMMENT '最高价',
  `last_price` decimal(10,2) NOT NULL COMMENT '最新价',
  `lower_limit_price` decimal(10,2) NOT NULL COMMENT '跌停价',
  `low_price` decimal(10,2) NOT NULL COMMENT '最低价',
  `open_price` decimal(10,2) NOT NULL COMMENT '今日开盘价 9点25分集合竞价结束的价格',
	`pre_close_price` decimal(10,2) NOT NULL COMMENT '昨日收盘价',
	`pre_delta` decimal(10,2) NOT NULL COMMENT '昨日预留',
 	`pre_settlement_price` decimal(10,2) NOT NULL COMMENT '昨日结算价',
	`pre_total_long_positon` bigint(10)   COMMENT '昨日持仓量(张)(目前未填写)',
	`qty` bigint(10)   COMMENT '数量，为总成交量（单位股，与交易所一致）',
	`settlement_price` decimal(10,2) NOT NULL COMMENT '今日结算价',
	`ticker` varchar(100) NOT NULL COMMENT '股票代码',
	`ticker_status` varchar(100) NOT NULL COMMENT '当前交易状态说明',
 	`total_long_positon` bigint(10)   COMMENT '持仓量(张)',
	`trades_count` bigint(10)   COMMENT '成交笔数',
	`turnover` decimal(10,2) NOT NULL COMMENT '成交金额，为总成交金额（单位元，与交易所一致）',
	`upper_limit_price` decimal(10,2) NOT NULL COMMENT '涨停价',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='普通行情';

CREATE TABLE `tick_by_tick` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `datetime` mediumtext NOT NULL COMMENT '委托时间',
  `seq` bigint(20) COMMENT '预留',
  `ticker` varchar(16) NOT NULL COMMENT '合约代码',
  `entrust_id` bigint(20) NOT NULL COMMENT '逐笔委托id',
  `exchange_type` tinyint(4) NOT NULL COMMENT '交易所代码',
  `trade_id` bigint(20) NOT NULL COMMENT '逐笔成交id',
  `type` tinyint(4) NOT NULL COMMENT '委托 or 成交',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='逐笔行情通知 包括股票指数和期权';

CREATE TABLE `tick_by_tick_entrust` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `channel_no` int(10) NOT NULL COMMENT '频道代码',
  `ord_type` varchar(5) NOT NULL COMMENT '订单类别: 1: 市价; 2: 限价; 3: 本方最优',
  `price` decimal(10,2) NOT NULL COMMENT '委托价格',
  `qty` bigint(20) NOT NULL COMMENT '委托数量',
  `seq` bigint(20) NOT NULL COMMENT '委托序号(在同一个channel_no内唯一，从1开始连续)',
  `side` varchar(5) NOT NULL COMMENT '1:买; 2:卖; G:借入; F:出借',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='逐笔委托(仅适用深交所)';


CREATE TABLE `order_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `business_type` TINYINT(4) DEFAULT NULL COMMENT '业务类型 普通股票业务 1 新股申购业务 2 回购业务 3 ',
  `cancel_time` datetime DEFAULT NULL COMMENT '撤销时间',
  `insert_time` datetime DEFAULT NULL COMMENT '委托时间',
  `market_type` TINYINT(4) NOT NULL COMMENT '初始化值或者未知 0 上海A股 1 深圳A股 2 未知交易市场类型 3',
  `order_cancel_client_id` int(20) DEFAULT NULL COMMENT '报单操作引用，用户自定义',
  `order_cancel_xtp_id` int(20) DEFAULT NULL COMMENT '撤单在XTP系统中的id，在XTP系统中唯一',
  `order_client_id` int(20) DEFAULT NULL COMMENT '报单引用，用户自定义',
  `order_local_id` varchar(30) DEFAULT NULL COMMENT '本地报单编号 OMS生成的单号，不等同于order_xtp_id，为服务器传到报盘的单号',
  `order_status_type` TINYINT(4) DEFAULT NULL COMMENT '报单状态类型 0 全部成交, 1 已撤单, 2 初始化, 3 未成交, 4部分撤单,5 部分成交,6 已拒绝, 7 未知订单状态 ',
  `order_submit_status_type` TINYINT(4) DEFAULT NULL COMMENT '报单提交状态 1 撤单已经被接受,2撤单已经被拒绝, 3撤单已经提交, 4 订单已经被接受,5 订单已经被拒绝 6订单已经提交',
  `order_xtp_id` varchar(50) DEFAULT NULL COMMENT 'XTP系统订单ID',
  `position_effect_type` TINYINT(4) DEFAULT NULL COMMENT '开平标志 1平,2平今,3平昨,4强平,5强减,6初始值或未知值开平标识，现货适用,7,本地强平,8开,9未知的开平标识类型',
  `price` DECIMAL(10,2) DEFAULT NULL COMMENT '价格',
  `price_type` TINYINT(4) DEFAULT NULL COMMENT '价格类型 1全部成交或撤销,市价单-深, 2即时成交剩余转撤销，市价单-深,3最优5档即时成交剩余转撤销，市价单-沪深,4最优五档即时成交剩余转限价，市价单-沪,
                                                  5 本方最优，市价单-深,6限价单-沪深（除普通股票业务外，其余业务均使用此种类型）,7期权限价申报FOK, 8 对方最优剩余转限价，市价单-深,9未知或者无效价格类型',
  `qty_left` bigint(20) DEFAULT NULL COMMENT '剩余数量，当撤单成功时，表示撤单数量',
  `qty_traded` bigint(20) DEFAULT NULL COMMENT '今成交数量，为此订单累计成交数量',
  `quantity` bigint(20) DEFAULT NULL COMMENT '数量，此订单的报单数量',
  `request_id` bigint(20) DEFAULT NULL COMMENT '此消息响应函数对应的请求ID',
  `side_type` TINYINT(4) DEFAULT NULL COMMENT '买卖方向类型 1 买（新股申购、ETF买等）,2买平.3买开,4合并,5 申购,6 赎回 7卖（逆回购）,8卖平,9卖开 ,10拆分 ,11未知或者无效买卖方向',
  `ticker` varchar(20) DEFAULT NULL COMMENT '合约代码',
  `trade_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '成交金额，为此订单的成交总金额',
  `txtp_order_type_type` varchar(20) DEFAULT NULL COMMENT '报单类型',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='订单信息';

CREATE TABLE `asset` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `account_type` tinyint(4) DEFAULT NULL COMMENT '账户类型 信用账户 1 衍生品账户 2 普通账户 3 未知账户类型 4',
  `banlance` decimal(10,2) DEFAULT NULL COMMENT '当前余额',
  `buying_power` decimal(10,2) DEFAULT NULL COMMENT '可用资金',
  `captial_asset` decimal(10,2) DEFAULT NULL COMMENT '资金资产',
  `deposit_withdraw` decimal(10,2) DEFAULT NULL COMMENT '当天出入金',
  `force_freeze_amount` decimal(10,2) DEFAULT NULL COMMENT '强锁资金',
  `frozen_exec_cash` decimal(10,2) DEFAULT NULL COMMENT '行权冻结资金',
  `frozen_exec_fee` decimal(10,2) DEFAULT NULL COMMENT '行权费用',
  `frozen_margin` decimal(10,2) DEFAULT NULL COMMENT '冻结的保证金',
  `fund_buy_amount` decimal(10,2) DEFAULT NULL COMMENT '累计买入成交证券占用资金',
  `fund_buy_fee` decimal(10,2) DEFAULT NULL COMMENT '累计买入成交交易费用',
  `fund_sell_amount` decimal(10,2) DEFAULT NULL COMMENT '累计卖出成交证券所得资金',
  `fund_sell_fee` decimal(10,2) DEFAULT NULL COMMENT '累计卖出成交交易费用',
  `last_resp` tinyint(4) DEFAULT NULL COMMENT '此消息响应函数是否为request_id这条请求所对应的最后一个响应，当为最后一个的时候为true，如果为false，表示还有其他后续消息响应',
  `orig_banlance` decimal(10,2) DEFAULT NULL COMMENT '昨日余额',
  `pay_later` decimal(10,2) DEFAULT NULL COMMENT '垫付资金',
  `preadva_pay` decimal(10,2) DEFAULT NULL COMMENT '预垫付资金',
  `preferred_amount` decimal(10,2) DEFAULT NULL COMMENT '可取资金',
  `request_id` bigint(20) DEFAULT NULL COMMENT '此消息响应函数对应的请求ID',
  `security_asset` decimal(10,2) DEFAULT NULL COMMENT '证券资产',
  `total_asset` decimal(10,2) DEFAULT NULL COMMENT '总资产(=可用资金 + 证券资产（目前为0）+ 预扣的资金)',
  `trade_netting` decimal(10,2) DEFAULT NULL COMMENT '当日交易资金轧差',
  `unknown` bigint(20) DEFAULT NULL COMMENT '保留字段',
  `withholding_amount` decimal(10,2) DEFAULT NULL COMMENT 'XTP系统预扣的资金（包括购买卖股票时预扣的交易资金+预扣手续费）',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='资产信息';

CREATE TABLE `circulate_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ticker` varchar(255) NOT NULL COMMENT 'ticker编号',
  `ticker_name` varchar(255)  NOT NULL  COMMENT 'ticker名称',
  `circulate` bigint(20) NOT NULL DEFAULT '0' COMMENT '总流通量',
  `circulate_z` bigint(20) NOT NULL DEFAULT '0' COMMENT '流通量z',
  `circulate_type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '类型  0 一亿一下  1 1亿到3亿   2 3亿以上',
  `change_time` datetime NOT NULL COMMENT '季度',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8 COMMENT '流通信息';

CREATE TABLE `depth_market_data_ex` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ask1_Count` int(10) NOT NULL DEFAULT '0' COMMENT '卖一队列的有效委托笔数',
  `ask1_qty` varchar(255) NOT NULL DEFAULT '' COMMENT '卖一队列数据',
  `bid1_count` int(11) NOT NULL DEFAULT '0' COMMENT '买一队列的有效委托笔数',
  `bid1_qty` varchar(255) NOT NULL DEFAULT '' COMMENT '买一队列数据',
  `max_ask1_count` int(10) NOT NULL DEFAULT '0' COMMENT '卖一队列总委托笔数',
  `max_bid1_count` int(10) NOT NULL DEFAULT '0' COMMENT '买一队列总委托笔数',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '买一买一队列信息'


CREATE TABLE `circulate_type_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `circulate_type` tinyint(4) NOT NULL COMMENT '类型  0 一亿一下  1 1亿到3亿   2 3亿以上',
  `percent` int(4) NOT NULL COMMENT '百分比 如果是5% 则值为5',
  `min_insert_quantity` bigint(10) NOT NULL COMMENT '能下单的最小股数',
  `check_circulate_delay` int(4) NOT NULL COMMENT '查询行情与流通信息对比的延时时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8 COMMENT='股票分类配置信息';

CREATE TABLE `capital_order_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `ticker` varchar(30) NOT NULL COMMENT '股票代码',
  `ticket_name` varchar(60) DEFAULT NULL COMMENT '股票名称',
  `exchange_type` tinyint(4) NOT NULL COMMENT '交易所类型 上海 1， 深圳 2， 未知 3',
  `order_xtp_id` varchar(30) DEFAULT NULL COMMENT 'xtp 订单号',
  `order_cancel_xtp_id` varchar(30) DEFAULT NULL COMMENT 'xtp 撤单号',
  `quantity` int(8) NOT NULL COMMENT '下单数量',
  `traded_quantity` int(8) NOT NULL COMMENT '成交数量',
  `order_price` decimal(16,2) NOT NULL COMMENT '下单价格',
  `status` tinyint(4) NOT NULL COMMENT '订单 状态 1  本地初始化, 2 xtp 系统初始化,3 未成交, 4 全部成交,5 部分成交,6 全部撤单, 7部分撤单',
  `stop_insert` tinyint(4) NOT NULL COMMENT '0 可以下单  1 禁止下单',
  `stop_cancel` tinyint(4) NOT NULL COMMENT '0 允许撤单, 1 禁止撤单',
  `cancel_time` datetime DEFAULT NULL COMMENT '撤单时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_xtp_id` (`order_xtp_id`) USING HASH,
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1480 DEFAULT CHARSET=utf8 COMMENT='系统订单信息';

CREATE TABLE `cancel_order_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `ticker` varchar(10) NOT NULL COMMENT '股票代码',
  `ticker_name` varchar(30) DEFAULT NULL COMMENT '股票名称',
  `order_xtp_id` varchar(30) NOT NULL COMMENT '订单号',
  `status` tinyint(4) NOT NULL COMMENT '状态 1 成功  2 失败  0 初始化',
  `retry_count` int(8) DEFAULT '0' COMMENT '重试次数',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_xtp_id` (`order_xtp_id`) USING HASH
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8 COMMENT='撤单动作日志表';

CREATE TABLE `disable_operate_ticket_pool` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `ticker` varchar(10) NOT NULL COMMENT '股票代码',
  `ticker_name` varchar(30) DEFAULT NULL,
  `day` varchar(15) DEFAULT NULL COMMENT '日期显示 格式 yyyy-MM-dd',
  `operate_status` tinyint(4) NOT NULL COMMENT '0 系统操作 1 人工操作',
  `business_status` tinyint(4) NOT NULL COMMENT '2 进制标识 11 禁止下单 禁止撤单, 10 禁止下单,01 禁止撤单 00 都不禁止',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ticker_day` (`ticker`,`day`) USING HASH
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8 COMMENT='禁止操作股票池';