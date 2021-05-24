package com.bazinga.replay.component;

import com.bazinga.exception.BusinessException;
import com.bazinga.replay.dto.CirculateInfoExcelDTO;
import com.bazinga.replay.model.BlockInfo;
import com.bazinga.replay.model.BlockStockDetail;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.query.BlockInfoQuery;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.service.BlockInfoService;
import com.bazinga.replay.service.BlockStockDetailService;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.util.CommonUtil;
import com.bazinga.util.Excel2JavaPojoUtil;
import com.bazinga.util.MarketUtil;
import com.tradex.util.Conf;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author yunshan
 * @date 2019/5/13
 * 同步信息组件
 */
@Component
@Slf4j
public class SynInfoComponent {

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private BlockInfoService blockInfoService;

    @Autowired
    private BlockStockDetailService blockStockDetailService;


    /**
     * 同步板块信息
     */
    public   void synBlockInfo() throws IOException {
        File file = new File(Conf.get("block.dir"));
        List<String> list = FileUtils.readLines(file, "GBK");
        list.forEach(item -> {
            String[] contents = item.split("\t");
            if (contents.length < 3) {
                throw new IllegalArgumentException("原始数据异常");
            }
            BlockInfoQuery blockInfoQuery = new BlockInfoQuery();
            String blockCode = contents[1].trim();
            blockInfoQuery.setBlockCode(contents[1].trim());
            List<BlockInfo> blockInfos = blockInfoService.listByCondition(blockInfoQuery);
            if (CollectionUtils.isNotEmpty(blockInfos)) {
                BlockInfo blockInfo = blockInfos.get(0);
                blockInfo.setBlockCode(blockCode);
                blockInfo.setBlockName(contents[0]);
                blockInfo.setTotalCount(contents.length - 2);
                blockInfoService.updateById(blockInfo);
            } else {
                BlockInfo blockInfo = new BlockInfo();
                blockInfo.setBlockCode(blockCode);
                blockInfo.setBlockName(contents[0]);
                blockInfo.setTotalCount(contents.length - 2);
                blockInfoService.save(blockInfo);
            }
            blockStockDetailService.deleteByBlockCode(blockCode);
            for (int i = 2; i < contents.length; i++) {
                BlockStockDetail blockStockDetail = new BlockStockDetail();
                blockStockDetail.setBlockCode(blockCode);
                blockStockDetail.setBlockName(contents[0]);
                blockStockDetail.setStockCode(contents[i]);
                CirculateInfoQuery query = new CirculateInfoQuery();
                query.setStockCode(contents[i]);
                List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(query);
                if (!CollectionUtils.isEmpty(circulateInfos)) {
                    blockStockDetail.setStockName(circulateInfos.get(0).getStockName());
                }
                blockStockDetailService.save(blockStockDetail);
            }
            log.info("处理板块blockCode ={} blockName ={},个股数量={}", contents[1], contents[0], contents.length - 2);
        });
        log.info("处理完毕,处理总板块数量 size ={}", list.size());
    }

    public void synCirculateInfo() {
        File file = new File("D:/circulate/circulate.xlsx");
        if (!file.exists()) {
            throw new BusinessException("文件:" + "D:/circulate/circulate.xlsx" + "不存在");
        }
        try {
            List<CirculateInfoExcelDTO> dataList = new Excel2JavaPojoUtil(file).excel2JavaPojo(CirculateInfoExcelDTO.class);
            dataList.forEach(item -> {
                CirculateInfoQuery circulateInfoQuery = new CirculateInfoQuery();
                circulateInfoQuery.setStockCode(item.getStock());
                List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(circulateInfoQuery);
                CirculateInfo circulateInfo = null;
                if (!CollectionUtils.isEmpty(circulateInfos)) {
                    circulateInfo = circulateInfos.get(0);
                }
                if (circulateInfo==null) {
                    circulateInfo = convert2Mode(item);
                    circulateInfoService.save(circulateInfo);
                } else {
                    circulateInfo.setStockCode(item.getStock());
                    circulateInfo.setCirculate(item.getCirculateZ().longValue());
                    circulateInfo.setCirculateZ(item.getCirculateZ().longValue());
                    circulateInfo.setStockName(item.getStockName());
                    circulateInfo.setStockType(CommonUtil.getStockType(item.getCirculateZ().longValue()));
                    circulateInfoService.updateById(circulateInfo);
                }
            });
            log.info("更新流通 z 信息完毕 size = {}", dataList.size());
        } catch (Exception e) {
            log.error("更新流通 z 信息异常", e);
            throw new BusinessException("文件解析及同步异常", e);
        }
    }
    private CirculateInfo convert2Mode(CirculateInfoExcelDTO item) {
        Integer marketCode = MarketUtil.getMarketCode(item.getStock());
        CirculateInfo circulateInfo = new CirculateInfo();
        circulateInfo.setStockCode(item.getStock());
        circulateInfo.setMarketType(marketCode);
        circulateInfo.setCirculate(item.getCirculateZ().longValue());
        circulateInfo.setCirculateZ(item.getCirculateZ().longValue());
        circulateInfo.setStockName(item.getStockName());
        circulateInfo.setStockType(CommonUtil.getStockType(item.getCirculateZ().longValue()));
        return circulateInfo;
    }


}
