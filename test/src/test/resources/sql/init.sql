insert into circulate_info SELECT id,stock stock_code, stock_name,circulate,stock_type,market_type,circulate_z,now() create_time from circulate_info_copy;


truncate table circulate_info_all;
INSERT INTO circulate_info_all SELECT null id , stock_code stock, stock_name , circulate, stock_type,circulate_z,'202101' year_quater,create_time from circulate_info