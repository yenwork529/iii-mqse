package org.iii.esd.server.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.iii.esd.caculate.Utility;
import org.iii.esd.mongo.document.integrate.BidResInfo;
import org.iii.esd.mongo.document.integrate.BidTxgInfo;
import org.iii.esd.mongo.service.SpinReserveService;
import org.iii.esd.mongo.service.integrate.IntegrateRelationService;
import org.iii.esd.server.api.request.BidCloneRequest;

@Service
@Log4j2
public class BidCloningService {

    //maximum days of cloning bidding data
    public final static Integer MAXIMUM_OFFSET = 30;

    @Autowired
    private SpinReserveService spinReserveService;
    @Autowired
    private IntegrateRelationService relationService;
    @Autowired
    private IntegrateBidService bidService;

    @Async
    public void executeAsyncTask(String txgId, BidCloneRequest bidCloneRequest) {
        log.info("begin to execute cloning bidding data...");
        Date baseDate = DateUtils.truncate(bidCloneRequest.getBaseDate(), Calendar.DATE);
        Date targetDate = DateUtils.truncate(bidCloneRequest.getTargetDate(), Calendar.DATE);
        // diffDays might be zero or negative
        int diffDays = Utility.getDatesInterval(baseDate, targetDate);

        // List<SpinReserveBid> sourceBidList = spinReserveService.findAllBySrIdAndTime(txgId, baseDate);
        List<BidTxgInfo> sourceBidList = bidService.findInfoByTxgIdAndTime(txgId, baseDate);
        List<BidTxgInfo> cloneBidList = new ArrayList<>();

        if (sourceBidList != null && sourceBidList.size() > 0) {
            for (int offset = 0; offset <= bidCloneRequest.getOffset(); offset++) {
                for (BidTxgInfo sourceBid : sourceBidList) {
                    Date current = new Date();
                    Date cloneDate = Utility.addDays(sourceBid.getTimestamp(), diffDays + offset);

                    BidTxgInfo cloneBid = new BidTxgInfo();
                    cloneBid.setDt(cloneDate);
                    cloneBid.setTxgId(txgId);
                    cloneBid.setTimestamp(cloneDate);
                    cloneBid.setCapacity(sourceBid.getCapacity());
                    cloneBid.setPrice(sourceBid.getPrice());
                    cloneBid.setEnergyPrice(sourceBid.getEnergyPrice());
                    cloneBid.setCreateTime(current);
                    cloneBid.setUpdateTime(current);

                    List<BidResInfo> sourceBidDetailList = sourceBid.getList();
                    if (sourceBidDetailList != null && sourceBidDetailList.size() > 0) {
                        List<BidResInfo> cloneBidDetailList = new ArrayList<>();
                        for (BidResInfo sourceBidDetail : sourceBidDetailList) {
                            BidResInfo cloneBidDetail = new BidResInfo();
                            cloneBidDetail.setResId(sourceBidDetail.getResId());
                            cloneBidDetail.setDt(cloneDate);
                            cloneBidDetail.setCapacity(sourceBidDetail.getCapacity());
                            cloneBidDetail.setCreateTime(current);
                            cloneBidDetailList.add(cloneBidDetail);
                        }
                        cloneBid.setList(cloneBidDetailList);
                    }
                    cloneBidList.add(cloneBid);
                }
            }

            if (cloneBidList.size() > 0) {
                bidService.saveInfoByTxgId(txgId, cloneBidList);
            }

            log.info("The bidding data has been cloned(srId=" + txgId + ", baseDate=" + bidCloneRequest.getBaseDate() + ", targetDate=" +
                    bidCloneRequest.getTargetDate() + ", offset=" + bidCloneRequest.getOffset() + ", size=" + cloneBidList.size());
        }
    }
}