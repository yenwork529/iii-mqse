package org.iii.esd.server.wrap;

import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;

import org.iii.esd.api.vo.integrate.BidInfo;
import org.iii.esd.mongo.document.SpinReserveBid;
import org.iii.esd.mongo.document.integrate.BidResBid;
import org.iii.esd.mongo.document.integrate.BidResInfo;
import org.iii.esd.mongo.document.integrate.BidTxgBid;
import org.iii.esd.mongo.document.integrate.BidTxgInfo;

@Log4j2
public final class BidInfoWrapper {
    private BidInfoWrapper() {}

    public static BidInfo unwrap(SpinReserveBid entity) {
        BidInfo vo = new BidInfo();
        List<BidInfo.BidDetail> list = entity.getList().stream().map(detail -> {
            BidInfo.BidDetail voDetaiL = new BidInfo.BidDetail();
            BeanUtils.copyProperties(detail, voDetaiL);
            return voDetaiL;
        }).collect(Collectors.toList());

        BeanUtils.copyProperties(entity, vo, "list");

        vo.setList(list);

        return vo;
    }

    public static BidInfo unwrap(BidTxgInfo entity) {
        /*
        BidInfo vo = new BidInfo();
        List<BidInfo.BidDetail> list = entity.getList().stream().map(detail -> {
            BidInfo.BidDetail voDetail = new BidInfo.BidDetail();
            BeanUtils.copyProperties(detail, voDetail);
            return voDetail;
        }).collect(Collectors.toList());

        BeanUtils.copyProperties(entity, vo, "list");

        vo.setList(list);

        return vo;
         */

        List<BidInfo.BidDetail> list = entity.getList().stream().map(detail ->
                BidInfo.BidDetail.builder()
                                 .resId(detail.getResId())
                                 .createTime(detail.getCreateTime())
                                 .timestamp(detail.getTimestamp())
                                 .sr_capacity(detail.getCapacity())
                                 .capacity(detail.getCapacity())
                                 .awarded_capacity(detail.getAwardedCapacity())
                                 .ppa_capacity(detail.getPpaCapacity())
                                 .build()).collect(Collectors.toList());

        BidInfo vo = BidInfo.builder()
                            .list(list)
                            .txgId(entity.getTxgId())
                            .createTime(entity.getCreateTime())
                            .updateTime(entity.getUpdateTime())
                            .timestamp(entity.getTimestamp())
                            .capacity(entity.getCapacity())
                            .price(entity.getPrice())
                            .energyPrice(entity.getEnergyPrice())
                            .awarded_capacity(entity.getAwardedCapacity())
                            .ppa_capacity(entity.getPpaCapacity())
                            .ppaEnergyPrice(entity.getPpaEnergyPrice())
                            .energyPrice(entity.getEnergyPrice())
                            .build();

        // log.info("unwrapped vo: {}", vo);

        return vo;
    }

    public static BidTxgInfo wrapInfo(BidInfo vo) {
        List<BidResInfo> list = vo.getList().stream().map(detail ->
                BidResInfo.builder()
                          .resId(detail.getResId())
                          .dt(detail.getTimestamp())
                          .createTime(vo.getCreateTime())
                          .awardedCapacity(detail.getAwarded_capacity())
                          .ppaCapacity(detail.getPpa_capacity())
                          .capacity(detail.getCapacity())
                          .build()).collect(Collectors.toList());

        BidTxgInfo entity = BidTxgInfo.builder()
                                      .txgId(vo.getTxgId())
                                      .dt(vo.getTimestamp())
                                      .createTime(vo.getCreateTime())
                                      .updateTime(vo.getCreateTime())
                                      .awardedCapacity(vo.getAwarded_capacity())
                                      .ppaEnergyPrice(vo.getPpaEnergyPrice())
                                      .ppaCapacity(vo.getPpa_capacity())
                                      .energyPrice(vo.getEnergyPrice())
                                      .capacity(vo.getCapacity())
                                      .price(vo.getPrice())
                                      .list(list)
                                      .build();

        // log.info("wrapped entity: {}", entity);

        return entity;
    }

    public static BidTxgBid wrapBid(BidInfo vo) {
        BidTxgBid entity = new BidTxgBid();

        List<BidResBid> list = vo.getList().stream().map(detail -> {
            BidResBid entDetail = new BidResBid();
            BeanUtils.copyProperties(detail, entDetail);
            return entDetail;
        }).collect(Collectors.toList());

        BeanUtils.copyProperties(vo, entity, "list");

        entity.setList(list);

        return entity;
    }
}
