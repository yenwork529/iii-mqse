package org.iii.esd.mongo.service.integrate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import org.iii.esd.mongo.dao.DrResDataDao;
import org.iii.esd.mongo.dao.DrTxgDataDao;
import org.iii.esd.mongo.document.integrate.BidResBid;
import org.iii.esd.mongo.document.integrate.BidResData;
import org.iii.esd.mongo.document.integrate.BidResInfo;
import org.iii.esd.mongo.document.integrate.BidResStatistics;
import org.iii.esd.mongo.document.integrate.BidTxgBid;
import org.iii.esd.mongo.document.integrate.BidTxgData;
import org.iii.esd.mongo.document.integrate.BidTxgInfo;
import org.iii.esd.mongo.document.integrate.BidTxgStatistics;
import org.iii.esd.mongo.document.integrate.DrResData;
import org.iii.esd.mongo.document.integrate.DrTxgData;
import org.iii.esd.mongo.document.integrate.SettlementPrice;
import org.iii.esd.mongo.document.integrate.ThinClientProfile;
import org.iii.esd.mongo.document.integrate.TxgBid;
import org.iii.esd.mongo.document.integrate.TxgData;
import org.iii.esd.mongo.document.integrate.TxgDeviceProfile;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.repository.integrate.BidResBidRepository;
import org.iii.esd.mongo.repository.integrate.BidResDataRepository;
import org.iii.esd.mongo.repository.integrate.BidResInfoRepository;
import org.iii.esd.mongo.repository.integrate.BidResStatisticsRepository;
import org.iii.esd.mongo.repository.integrate.BidTxgBidRepository;
import org.iii.esd.mongo.repository.integrate.BidTxgDataRepository;
import org.iii.esd.mongo.repository.integrate.BidTxgInfoRepository;
import org.iii.esd.mongo.repository.integrate.BidTxgStatisticsRepository;
import org.iii.esd.mongo.repository.integrate.QseProfileRepository;
import org.iii.esd.mongo.repository.integrate.RelationshipProfileRepository;
import org.iii.esd.mongo.repository.integrate.SettlementPriceRepository;
import org.iii.esd.mongo.repository.integrate.TxgBidRepository;
import org.iii.esd.mongo.repository.integrate.TxgDataRepository;
import org.iii.esd.mongo.repository.integrate.TxgFieldProfileRepository;
import org.iii.esd.mongo.repository.integrate.TxgProfileRepository;
import org.iii.esd.utils.DatetimeUtils;

@Service
@Log4j2
public class IntegrateDataService implements IntegrateDataInterface {

    @Autowired
    public TxgProfileRepository txgRepo;

    @Autowired
    public TxgFieldProfileRepository resRepo;

    @Autowired
    public RelationshipProfileRepository relationRepo;

    @Autowired
    public QseProfileRepository qseRepo;

    @Autowired
    // public DrResDataRepository drResRepo;
    public DrResDataDao drResRepo;

    @Autowired
    public DrTxgDataDao drTxgRepo;

    @Autowired
    public TxgBidRepository bidRepo;

    @Autowired
    public TxgDataRepository biddataRepo;

    @Autowired
    public BidResBidRepository bidresbidRepo;
    @Autowired
    public BidResDataRepository bidresdataRepo;
    @Autowired
    public BidResInfoRepository bidresinfoRepo;
    @Autowired
    public BidResStatisticsRepository bidresstatRepo;

    @Autowired
    public BidTxgBidRepository bidtxgbidRepo;
    @Autowired
    public BidTxgDataRepository bidtxgdataRepo;
    @Autowired
    public BidTxgInfoRepository bidtxginfoRepo;
    @Autowired
    public BidTxgStatisticsRepository bidtxgstatRepo;

    @Autowired
    public SettlementPriceRepository settleRepo;

    public void save(SettlementPrice bd) {
        settleRepo.save(bd);
    }

    public List<SettlementPrice> seekSettlementPriceBetween(Long from, Long to) {
        Query searchQuery = makeTicksQuery(from, to);
        return mongoOperations.find(searchQuery, SettlementPrice.class);
    }

    public SettlementPrice seekSettlementPriceOn(Long tick) {
        Query searchQuery = makeTicksQuery(tick);
        List<SettlementPrice> lst = mongoOperations.find(searchQuery, SettlementPrice.class);
        if (lst == null || lst.size() != 1) {
            return null;
        }
        return lst.get(0);
    }

    public List<SettlementPrice> seekSettlementPriceBetween(Date from, Date to) {
        return seekSettlementPriceBetween(from.getTime(), to.getTime());
    }

    public void save(BidResBid bd) {
        bidresbidRepo.save(bd);
    }

    // for UNLOAD type only
    public List<BidResBid> seekBidResBidBetween(String resId, Long from, Long to) {
        Query searchQuery = makeTicksQuery("resId", resId, from, to);
        return mongoOperations.find(searchQuery, BidResBid.class);
    }

    public List<BidResBid> seekBidResBidBetween(String resId, Date from, Date to) {
        return seekBidResBidBetween(resId, from.getTime(), to.getTime());
    }

    public void save(BidResData bd) {
        bidresdataRepo.save(bd);
    }

    public BidResData seekBidResData(String resId, long on) {
        Query searchQuery = makeTicksQuery("resId", resId, on);
        List<BidResData> lst = mongoOperations.find(searchQuery, BidResData.class);
        if (lst == null || lst.size() != 1) {
            return null;
        }
        return lst.get(0);
    }

    public List<BidResData> seekBidResDataBetween(String resId, Long from, Long to) {
        Query searchQuery = makeTicksQuery("resId", resId, from, to);
        return mongoOperations.find(searchQuery, BidResData.class);
    }

    public List<BidResData> seekBidResDataBetween(String resId, Date from, Date to) {
        return seekBidResDataBetween(resId, from.getTime(), to.getTime());
    }

    public void save(BidResInfo bd) {
        bidresinfoRepo.save(bd);
    }

    public void saveAllBidResInfo(Iterable<BidResInfo> ob) {
        bidresinfoRepo.saveAll(ob);
    }

    public Query makeTicksQuery(Long on) {
        Criteria criteria = Criteria.where("timeticks").is(on);
        return new Query(criteria).with(new Sort(Sort.Direction.ASC, "timeticks"));
    }

    public Query makeTicksQuery(Long from, Long to) {
        Criteria criteria = Criteria.where("timeticks").gte(from).lt(to);
        return new Query(criteria).with(new Sort(Sort.Direction.ASC, "timeticks"));
    }

    public Query makeTicksQuery(String key, String val, Long on) {
        Criteria criteria = Criteria.where(key).is(val).and("timeticks").is(on);
        return new Query(criteria).with(new Sort(Sort.Direction.ASC, "timeticks"));
    }

    public Query makeTicksQuery(String key, String val, Long from, Long to) {
        Criteria criteria = Criteria.where(key).is(val).and("timeticks").gte(from).lt(to);
        return new Query(criteria).with(new Sort(Sort.Direction.ASC, "timeticks"));
    }

    public Query makeTicksQuery(String key, List<String> vals, Long on) {
        Criteria criteria = Criteria.where(key).in(vals).and("timeticks").is(on);
        return new Query(criteria).with(new Sort(Sort.Direction.ASC, "timeticks"));
    }

    public Query makeTicksQuery(String key, List<String> vals, Long from, Long to) {
        Criteria criteria = Criteria.where(key).in(vals).and("timeticks").gte(from).lt(to);
        return new Query(criteria).with(new Sort(Sort.Direction.ASC, "timeticks"));
    }

    public List<BidResInfo> seekBidResInfoCover(String resId, Long on) {
        on = (on / 3600 / 1000L) * 3600 * 1000L;
        Criteria criteria = Criteria.where("resId").is(resId).and("timeticks").gte(on).lt(on + 3600 * 1000L);
        Query searchQuery = new Query(criteria).with(new Sort(Sort.Direction.ASC, "timeticks"));
        return mongoOperations.find(searchQuery, BidResInfo.class);
    }

    public List<BidResInfo> seekBidResInfoBetween(String resId, Long from, Long to) {
        Query searchQuery = makeTicksQuery("resId", resId, from, to);
        return mongoOperations.find(searchQuery, BidResInfo.class);
    }

    public List<BidResInfo> seekBidResInfoBetween(String resId, Date from, Date to) {
        return seekBidResInfoBetween(resId, from.getTime(), to.getTime());
    }

    public List<BidResInfo> seekGroupBidResInfoOn(List<String> fklst, Long tick) {
        Query searchQuery = makeTicksQuery("resId", fklst, tick);
        return mongoOperations.find(searchQuery, BidResInfo.class);
    }


    public void save(BidResStatistics bd) {
        bidresstatRepo.save(bd);
    }

    public List<BidResStatistics> seekBidResStatisticsBetween(String resId, Long from, Long to) {
        Query searchQuery = makeTicksQuery("resId", resId, from, to);
        return mongoOperations.find(searchQuery, BidResStatistics.class);
    }

    public List<BidResStatistics> seekBidResStatisticsBetween(String resId, Date from, Date to) {
        return seekBidResStatisticsBetween(resId, from.getTime(), to.getTime());
    }

    public void save(BidTxgBid bd) {
        bidtxgbidRepo.save(bd);
    }

    public List<BidTxgBid> seekBidTxgBidBetween(String txgId, Long from, Long to) {
        Query searchQuery = makeTicksQuery("txgId", txgId, from, to);
        return mongoOperations.find(searchQuery, BidTxgBid.class);
    }

    public BidTxgBid seekBidTxgBid(String txgId, Long on) {
        Query searchQuery = makeTicksQuery("txgId", txgId, on);
        List<BidTxgBid> lst = mongoOperations.find(searchQuery, BidTxgBid.class);
        if (lst == null || lst.size() == 0) {
            return null;
        }
        return lst.get(0);
    }

    public BidResBid seekBidResBid(String resId, Long on) {
        Query searchQuery = makeTicksQuery("resId", resId, on);
        List<BidResBid> lst = mongoOperations.find(searchQuery, BidResBid.class);
        if (lst == null || lst.size() == 0) {
            return null;
        }
        return lst.get(0);
    }

    public List<BidTxgBid> seekBidTxgBidBetween(String txgId, Date from, Date to) {
        return seekBidTxgBidBetween(txgId, from.getTime(), to.getTime());
    }

    public void save(BidTxgData bd) {
        bidtxgdataRepo.save(bd);
    }

    public List<BidTxgData> seekBidTxgData_Unload(String txgId, Long from, Long to) {
        Criteria criteria = Criteria.where("txgId").is(txgId).and("timeticks").gte(from).lt(to).and("noticeType")
                                    .is("UNLOAD");
        Query searchQuery = new Query(criteria).with(new Sort(Sort.Direction.ASC, "timeticks"));
        return mongoOperations.find(searchQuery, BidTxgData.class);
    }

    public List<BidTxgData> seekBidTxgDataBetween(String txgId, Long from, Long to) {
        Query searchQuery = makeTicksQuery("txgId", txgId, from, to);
        return mongoOperations.find(searchQuery, BidTxgData.class);
    }

    public List<BidTxgData> seekBidTxgDataBetween(String txgId, Date from, Date to) {
        return seekBidTxgDataBetween(txgId, from.getTime(), to.getTime());
    }

    public BidTxgData seekBidTxgData(String txgId, Long on) {
        Query searchQuery = makeTicksQuery("txgId", txgId, on);
        List<BidTxgData> lst = mongoOperations.find(searchQuery, BidTxgData.class);
        if (lst == null || lst.size() != 1) {
            return null;
        }
        return lst.get(0);
    }

    public List<BidTxgData> seekBidTxgData(String txgId, long from, long to) {
        Query searchQuery = makeTicksQuery("txgId", txgId, from, to);
        List<BidTxgData> lst = mongoOperations.find(searchQuery, BidTxgData.class);
        if (lst == null || lst.size() == 0) {
            return null;
        }
        return lst;
    }


    public BidResInfo seekBidResInfo(String txgId, long on) {
        Query searchQuery = makeTicksQuery("resId", txgId, on);
        List<BidResInfo> lst = mongoOperations.find(searchQuery, BidResInfo.class);
        if (lst == null || lst.size() == 0) {
            return null;
        }
        return lst.get(0);
    }

    public void save(BidTxgInfo bd) {
        bidtxginfoRepo.save(bd);
    }

    public void saveAllBidTxgInfo(Iterable<BidTxgInfo> ob) {
        bidtxginfoRepo.saveAll(ob);
    }


    public List<BidTxgInfo> seekBidTxgInfoCover(String txgId, Long on) {
        on = (on / 3600 / 1000L) * 3600 * 1000L;
        Criteria criteria = Criteria.where("txgId").is(txgId).and("timeticks").gte(on).lt(on + 3600 * 1000L);
        Query searchQuery = new Query(criteria).with(new Sort(Sort.Direction.ASC, "timeticks"));
        // searchQuery.with(new Sort(Sort.Direction.ASC, "timeticks"));
        return mongoOperations.find(searchQuery, BidTxgInfo.class);
        // return bidtxginfoRepo.seekBetween(txgId, from, to);
    }


    public void save(BidTxgStatistics bd) {
        bidtxgstatRepo.save(bd);
    }

    public List<BidTxgStatistics> seekBidTxgStatisticsBetween(String txgId, Long from, Long to) {
        Query searchQuery = makeTicksQuery("txgId", txgId, from, to);
        return mongoOperations.find(searchQuery, BidTxgStatistics.class);
    }

    public List<BidTxgStatistics> seekBidTxgStatisticsBetween(String txgId, Date from, Date to) {
        return seekBidTxgStatisticsBetween(txgId, from.getTime(), to.getTime());
    }

    public void save(DrTxgData d) {
        mongoOperations.save(d);
    }

    public void save(DrResData d) {
        mongoOperations.save(d);
    }

    public void save(TxgBid d) {
        bidRepo.save(d);
    }

    public void save(List<TxgBid> dlst) {
        bidRepo.saveAll(dlst);
    }

    public void save(TxgData d) {
        biddataRepo.save(d);
    }

    public <T> List<T> loadResData(TxgFieldProfile resProfile, Date from, Date to, Class<T> entityClass) {
        Query searchQuery;
        searchQuery = makeTicksQuery("resId", resProfile.getResId(), from.getTime(), to.getTime());
        return mongoOperations.find(searchQuery, entityClass);
    }

    public <T> T loadResData(TxgFieldProfile resProfile, long on, Class<T> entityClass) {
        Query searchQuery;
        searchQuery = makeTicksQuery("resId", resProfile.getResId(), on);
        List<T> lst = mongoOperations.find(searchQuery, entityClass);
        if (lst == null || lst.size() != 1) {
            return null;
        }
        return lst.get(0);
    }

    public <T> List<T> seekResDataOn(String Id, Long on, Class<T> entityClass) {
        Query searchQuery = makeTicksQuery("resId", Id, on);
        return mongoOperations.find(searchQuery, entityClass);
    }

    public <T> List<T> seekResDataBetween(String Id, Long gte, Long lt, Class<T> entityClass) {
        // log.info("seekRes between {} and {}", new Date(gte), new Date(lt));
        Query searchQuery = makeTicksQuery("resId", Id, gte, lt);
        return mongoOperations.find(searchQuery, entityClass);
    }

    public <T> List<T> seekGroupResDataOn(List<String> fklst, Long tick, Class<T> entityClass) {
        Query searchQuery = makeTicksQuery("resId", fklst, tick);
        return mongoOperations.find(searchQuery, entityClass);
    }

    public <T> List<T> loadTxgData(TxgProfile txgProfile, Date from, Date to, Class<T> entityClass) {
        Query searchQuery;
        searchQuery = makeTicksQuery("txgId", txgProfile.getTxgId(), from.getTime(), to.getTime());
        return mongoOperations.find(searchQuery, entityClass);
    }

    public <T> T loadTxgData(TxgProfile txgProfile, long on, Class<T> entityClass) {
        Query searchQuery;
        searchQuery = makeTicksQuery("txgId", txgProfile.getTxgId(), on);
        List<T> lst = mongoOperations.find(searchQuery, entityClass);
        if (lst == null || lst.size() != 1) {
            return null;
        }
        return lst.get(0);
    }


    public <T> List<T> seekTxgDataOn(String Id, Long on, Class<T> entityClass) {
        Query searchQuery = makeTicksQuery("txgId", Id, on);
        return mongoOperations.find(searchQuery, entityClass);
    }

    public BidTxgData seekBidTxgDataAround(TxgProfile tx, Date dt) {
        Criteria criteria = Criteria.where("txgId").is(tx.getTxgId()) //
                                    .and("noticeTime").lte(dt).and("endTime").gte(dt);
        // return new Query(criteria).with(new Sort(Sort.Direction.ASC, "timeticks"));
        Query searchQuery = new Query(criteria);
        List<BidTxgData> lst = mongoOperations.find(searchQuery, BidTxgData.class);
        if (lst.size() != 1) {
            return null;
        }
        return lst.get(0);
    }

    public BidTxgInfo seekBidTxgInfoAround(TxgProfile tx, Date dt) {
        Criteria criteria = Criteria.where("txgId").is(tx.getTxgId()) //
                                    .and("noticeTime").lte(dt).and("endTime").gte(dt);
        Query searchQuery = new Query(criteria);
        List<BidTxgInfo> lst = mongoOperations.find(searchQuery, BidTxgInfo.class);
        if (lst.size() != 1) {
            return null;
        }
        return lst.get(0);
    }

    /**
     * To update the TxgBid List
     *
     * @param bidlst
     */
    public void updateBidData(List<TxgBid> bidlst) {
        bidRepo.saveAll(bidlst);
    }

    public void test() {
        String resid = "RES-000099-01";
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date from = DatetimeUtils.parseDate("2021-10-20 09:00:00", sf);
        Date to = DatetimeUtils.parseDate("2021-10-20 10:00:00", sf);

        List<TxgFieldProfile> rlst = resRepo.seekFromResId(resid);
        if (rlst == null || rlst.size() == 0) {
            return;
        }
        TxgFieldProfile res = rlst.get(0);
        Integer restype = rlst.get(0).getResType();

        List<TxgProfile> txlst = txgRepo.seekFromTxgId(res.getTxgId());
        TxgProfile tx = txlst.get(0);

        PageRequest pg = PageRequest.of(0, 2, new Sort(Sort.Direction.ASC, "timestamp"));
        List<DrResData> flst;
        flst = drResRepo.findRecent(resid, pg);
        List<DrResData> dreslst = loadResData(res, from, to, DrResData.class);
        from = flst.get(0).getTimestamp();
        to = flst.get(1).getTimestamp();
        dreslst = loadResData(res, from, to, DrResData.class);
        List<DrTxgData> dtxglst = loadTxgData(tx, from, to, DrTxgData.class);
    }

    @Autowired
    private MongoOperations mongoOperations;

    public void eraseDrResDataBetween(String resid, long from, long to) {
        Criteria criteria = Criteria.where("resId").is(resid).and("timeticks").gte(from).lt(to);
        Query searchQuery = new Query(criteria);
        mongoOperations.remove(searchQuery, DrResData.class);
    }

    public void eraseDrTxgDataBetween(String id, long from, long to) {
        Criteria criteria = Criteria.where("txgId").is(id).and("timeticks").gte(from).lt(to);
        Query searchQuery = new Query(criteria);
        mongoOperations.remove(searchQuery, DrTxgData.class);
    }

    public <T> void purgeDocumentsBefore(long tick, Class<T> entityClass) {
        Criteria criteria = Criteria.where("timeticks").lt(tick);
        Query searchQuery = new Query(criteria);
        mongoOperations.remove(searchQuery, entityClass);
    }

    public <T> List<T> loadXData(String keyname, String keyval, long from, long to, Class<T> entityClass) {
        Criteria criteria = Criteria.where(keyname).is(keyval).and("timeticks").gte(from).lt(to);
        Query searchQuery = new Query(criteria);
        return mongoOperations.find(searchQuery, entityClass);
    }

    public TxgFieldProfile loadTxgFieldProfile(String id) {
        Criteria criteria = Criteria.where("resId").is(id);
        Query searchQuery = new Query(criteria);
        List<TxgFieldProfile> flst = mongoOperations.find(searchQuery, TxgFieldProfile.class);
        if (flst.size() == 1) {
            return flst.get(0);
        }
        return null;
    }

    public List<TxgDeviceProfile> loadTxgDeviceProfilesByResourceId(String id) {
        Criteria criteria = Criteria.where("resId").is(id);
        Query searchQuery = new Query(criteria);
        List<TxgDeviceProfile> flst = mongoOperations.find(searchQuery, TxgDeviceProfile.class);
        return flst;
    }

    public ThinClientProfile seekThinClientProfile(String id) {
        Criteria criteria = Criteria.where("syncId").is(id);
        Query searchQuery = new Query(criteria);
        List<ThinClientProfile> flst = mongoOperations.find(searchQuery, ThinClientProfile.class);
        if (flst.size() != 1) {
            return null;
        }
        return flst.get(0);
    }

    public <T> Long countTimeticks(String key, String val, long tick, Class<T> entityClass) {
        Criteria criteria = Criteria.where(key).is(val).and("timeticks").is(tick);
        Query qry = new Query(criteria);
        long n = mongoOperations.count(qry, entityClass);
        return n;
    }

    public <T> Long countTimeticks(String key, String val, long gte, long lt, Class<T> entityClass) {
        Criteria criteria = Criteria.where(key).is(val).and("timeticks").gte(gte).lt(lt);
        Query qry = new Query(criteria);
        long n = mongoOperations.count(qry, entityClass);
        return n;
    }

    public boolean createIfNotPresentBidTxgBid(String id, Date dt) {
        BidTxgBid bd = seekBidTxgBid(id, dt.getTime());
        if (bd != null) {
            return false;
        }
        bd = new BidTxgBid(id, dt);
        mongoOperations.save(bd);
        return true;
    }

    public boolean createIfNotPresentBidResBid(String id, Date dt) {
        BidResBid bd = seekBidResBid(id, dt.getTime());
        if (bd != null) {
            return false;
        }
        bd = new BidResBid(id, dt);
        mongoOperations.save(bd);
        return true;
    }

    // check BidTxgInfo 2022/01/17
    public BidTxgInfo seekBidTxgInfo(String txgId, long on) {
        Query searchQuery = makeTicksQuery("txgId", txgId, on);
        List<BidTxgInfo> lst = mongoOperations.find(searchQuery, BidTxgInfo.class);
        if (lst == null || lst.size() == 0) {
            return null;
        }
        return lst.get(0);
    }

    public List<BidTxgInfo> seekBidTxgInfo(String txgId, long from, long to) {
        Query searchQuery = makeTicksQuery("txgId", txgId, from, to);
        List<BidTxgInfo> lst = mongoOperations.find(searchQuery, BidTxgInfo.class);
        if (lst == null || lst.size() == 0) {
            return null;
        }
        return lst;
    }

    public List<BidTxgInfo> seekBidTxgInfoBetween(String txgId, Date from, Date to) {
        log.info("seek.BidTxgInfo.{} between {} and {}", txgId, from, to);
        return seekBidTxgInfoBetween(txgId, from.getTime(), to.getTime());
    }

    public List<BidTxgInfo> seekBidTxgInfoBetween(String txgId, Long from, Long to) {
        Criteria criteria = Criteria.where("txgId").is(txgId).and("timeticks").gte(from).lt(to);
        Query searchQuery = new Query(criteria).with(new Sort(Sort.Direction.ASC, "timeticks"));
        return mongoOperations.find(searchQuery, BidTxgInfo.class);
    }

    public List<BidResInfo> seekGroupBidResInfoOn(List<TxgFieldProfile> flst, long on) {
        List<String> fklst = flst.stream().map(f -> f.getResId()).collect(Collectors.toList());
        return seekGroupBidResInfoOn(fklst, on);
    }

}
