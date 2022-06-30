package org.iii.esd.mongo.service.integrate;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.integrate.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import org.iii.esd.utils.DatetimeUtils;
import org.iii.esd.mongo.repository.integrate.TxgFieldProfileRepository;
import org.iii.esd.mongo.repository.integrate.RelationshipProfileRepository;
import org.iii.esd.mongo.repository.integrate.QseProfileRepository;
import org.iii.esd.mongo.repository.integrate.TxgProfileRepository;
import org.iii.esd.mongo.vo.data.setup.SetupData;
import org.iii.esd.mongo.document.integrate.TxgDeviceProfile;
import org.iii.esd.mongo.enums.DeviceType;
import org.iii.esd.mongo.enums.LoadType;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class IntegrateRelationService implements IntegrateRelationInterface {

    @Autowired
    private MongoOperations mongoOperations;

    @Autowired
    public TxgProfileRepository txgRepo;

    @Autowired
    public TxgFieldProfileRepository resRepo;

    @Autowired
    public RelationshipProfileRepository relationRepo;

    @Autowired
    private QseProfileRepository qseRepo;

    public void rebuildRelatins() {
        // relationRepo.deleteAll();
        List<QseProfile> qlst = qseRepo.seekAll();
        List<TxgProfile> tlst;
        List<TxgFieldProfile> flst;

        if (qlst == null) {
            return;
        }

        for (QseProfile q : qlst) {
            tlst = txgRepo.seekFromQseId(q.getQseId());
            if (tlst == null) {
                continue;
            }
            for (TxgProfile t : tlst) {
                flst = resRepo.seekFromTxgId(t.getTxgId());
                if (flst == null) {
                    continue;
                }
                for (TxgFieldProfile f : flst) {
                    updateOrAddRelationship(q, t, f);
                }
            }

        }
    }

    void updateOrAddRelationship(QseProfile q, TxgProfile t, TxgFieldProfile f) {
        RelationshipProfile re;
        re = new RelationshipProfile(q, t, f);
        relationRepo.save(re);
    }

    /**
     * Seek for available QSE Profiles
     */
    public List<QseProfile> seekQseProfiles() {
        return qseRepo.findAll();
    }

    public QseProfile seekQseProfile(String qseId) {
        Query searchQuery = makeIdQuery("qseId", qseId);
        List<QseProfile> lst = mongoOperations.find(searchQuery, QseProfile.class);
        if (lst == null || lst.size() != 1) {
            return null;
        }
        return lst.get(0);
    }

    public QseProfile seekQseProfile(int qseCode) {
        Query searchQuery = makeCodeQuery("qseCode", qseCode);
        List<QseProfile> lst = mongoOperations.find(searchQuery, QseProfile.class);
        if (lst == null || lst.size() != 1) {
            return null;
        }
        return lst.get(0);
    }

    /**
     * Seek TxgProfiles for current time.
     * 
     * @return
     */
    public List<TxgProfile> seekTxgProfiles() {
        return txgRepo.seekAll();
    }

    /**
     * Seek TxgProfiles from QSE Id for current time.
     * 
     * @param qseId
     * @return
     */
    public List<TxgProfile> seekTxgProfilesFromQseId(String qseId) {
        return txgRepo.seekFromQseId(qseId);
    }

    /**
     * Seek Txg Profiles from QSE Id for the specific date.
     * 
     * @param qseId
     * @param dt
     * @return
     */
    public List<TxgProfile> seekTxgProfilesFromQseIdAndDate(String qseId, Date dt) {
        return txgRepo.seekFromQseId(qseId, dt, dt);
    }

    /**
     * Seek Txg Profiles for the specific date.
     * 
     * @param dt
     * @return
     */
    public List<TxgProfile> seekTxgProfilesForDate(Date dt) {
        return txgRepo.seekAll(dt, dt);
    }

    /**
     * Seek txg Profile from txgCode for current time.
     * 
     * @param code
     * @return
     */
    public TxgProfile seekTxgProfileFromTxgCode(Integer code) {
        List<TxgProfile> lst = txgRepo.seekFromTxgCode(code);
        if (lst == null || lst.size() == 0) {
            return null;
        }
        return lst.get(0);
    }

    /**
     * Seek Txg Profile from txgCode for the specific date.
     * 
     * @param code
     * @param dt
     * @return
     */
    public TxgProfile seekTxgProfileFromTxgCodeAndDate(Integer code, Date dt) {
        List<TxgProfile> lst = txgRepo.seekFromTxgCode(code, dt, dt);
        if (lst == null || lst.size() == 0) {
            return null;
        }
        return lst.get(0);

    }

    /**
     * Seek txg Profile from txgId
     * 
     * @param txgId
     * @return
     */
    public TxgProfile seekTxgProfileFromTxgId(String txgId) {
        List<TxgProfile> lst = txgRepo.seekFromTxgId(txgId);
        if (lst == null || lst.size() == 0) {
            return null;
        }
        return lst.get(0);
    }

    /**
     * Seek txg Profile from txgId for specific date
     * 
     * @param txgId
     * @param dt
     * @return
     */
    public TxgProfile seekTxgProfileFromTxgIdAndDate(String txgId, Date dt) {
        List<TxgProfile> lst = txgRepo.seekFromTxgId(txgId, dt, dt);
        if (lst == null || lst.size() == 0) {
            return null;
        }
        return lst.get(0);
    }

    /**
     * Seek Txg Profile from RES Id for current time.
     * 
     * @param resId
     * @return
     */
    public TxgProfile seekTxgProfileFromResId(String resId) {
        List<TxgFieldProfile> lst = resRepo.seekFromResId(resId);
        if (lst == null || lst.size() == 0) {
            return null;
        }
        List<TxgProfile> txlst = txgRepo.seekFromTxgId(lst.get(0).getTxgId());
        if (txlst == null || txlst.size() == 0) {
            return null;
        }
        return txlst.get(0);
    }

    /**
     * Seek Txg Profile from RES Id for the specific date.
     * 
     * @param resId
     * @param dt
     */
    public TxgProfile seekTxgProfileFromResIdAndDate(String resId, Date dt) {
        List<TxgFieldProfile> lst = resRepo.seekFromResId(resId, dt, dt);
        if (lst == null || lst.size() == 0) {
            return null;
        }
        List<TxgProfile> txlst = txgRepo.seekFromTxgId(lst.get(0).getTxgId());
        if (txlst == null || txlst.size() == 0) {
            return null;
        }
        return txlst.get(0);
    }

    /**
     * Seek Txg Field Profiles for TXG Id for current time.
     */
    public List<TxgFieldProfile> seekTxgFieldProfilesFromTxgId(String txgId) {
        List<TxgFieldProfile> lst = resRepo.seekFromTxgId(txgId);
        if (lst == null || lst.size() == 0) {
            return null;
        }
        return lst;
    }

    public TxgFieldProfile seekTxgFieldProfiles(String resId) {
        List<TxgFieldProfile> lst = resRepo.seekFromResId(resId);
        if (lst == null || lst.size() == 0) {
            return null;
        }
        return lst.get(0);
    }

    /**
     * Seek Txg Field Profiles for TXG Id for the specific date.
     */
    public List<TxgFieldProfile> seekTxgFieldProfilesFromTxgIdAndDate(String txgId, Date dt) {
        List<TxgFieldProfile> lst = resRepo.seekFromTxgId(txgId, dt, dt);
        if (lst == null || lst.size() == 0) {
            return null;
        }
        return lst;
    }

    public void test() {
        List<TxgProfile> txlst;
        TxgProfile tx;
        List<TxgFieldProfile> flst;

        txlst = txgRepo.seekAll();

        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date from1 = DatetimeUtils.parseDate("2021-04-11 09:00:00", sf);
        txlst = txgRepo.seekAll(from1, from1);

        Date from2 = DatetimeUtils.parseDate("2021-10-20 09:00:00", sf);
        txlst = txgRepo.seekAll(from2, from2);

        String resid = "RES-000099-01";

        tx = seekTxgProfileFromResId(resid);
        tx = seekTxgProfileFromResIdAndDate(resid, from1);
        tx = seekTxgProfileFromResIdAndDate(resid, from2);
        tx = seekTxgProfileFromResIdAndDate(resid, new Date());
        flst = seekTxgFieldProfilesFromTxgId(tx.getTxgId());
        flst = seekTxgFieldProfilesFromTxgIdAndDate(tx.getTxgId(), new Date());

        log.info("fin");
    }

    public void purgeSampleConfiguration() {
        qseRepo.deleteAll();
        txgRepo.deleteAll();
        resRepo.deleteAll();
    }

    // IIISR1-RES-01 1 1 0 0 0 0 IIISR1R1--------M1

    // IIISR2-RES-01 1 1 0 0 0 0 IIISR2R1--------M1
    // IIISR2-RES-02 1 1 0 0 0 0 IIISR2R2--------M1

    public void createSampleIntegrate() {
        log.info("creating sample devivces");
        TxgDeviceProfile dp;
        dp = TxgDeviceProfile.builder().id("IIISR2R1--------M1")
                .name("資策會-Device-IIISR2R1--------M1")
                .deviceType(DeviceType.Meter)
                .loadType(LoadType.M1)
                .resId("IIISR2-RES-01")
                .enableStatus(EnableStatus.enable)
                .isMainLoad(true)
                .setupData(SetupData.builder().ct(BigDecimal.ONE).pt(BigDecimal.ONE).build())
                .build();
        mongoOperations.save(dp);
        dp = TxgDeviceProfile.builder().id("IIISR2R2--------M1")
                .name("資策會-Device-IIISR2R2--------M1")
                .deviceType(DeviceType.Meter)
                .loadType(LoadType.M1)
                .resId("IIISR2-RES-02")
                .enableStatus(EnableStatus.enable)
                .isMainLoad(true)
                .setupData(SetupData.builder().ct(BigDecimal.ONE).pt(BigDecimal.ONE).build())
                .build();
        mongoOperations.save(dp);
    }

    public void createSampleDevices() {
        log.info("creating sample devivces");
        TxgDeviceProfile dp;
        dp = TxgDeviceProfile.builder().id("II10III---------M1")
                .name("資策會-Device-資策會-RES-14")
                .deviceType(DeviceType.Meter)
                .loadType(LoadType.M1)
                .resId("RES-000099-01")
                .enableStatus(EnableStatus.enable)
                .isMainLoad(true)
                .setupData(SetupData.builder().ct(BigDecimal.ONE).pt(BigDecimal.ONE).build())
                .build();
        mongoOperations.save(dp);
        dp = TxgDeviceProfile.builder().id("II10III---------K1")
                .name("資策會-Device-資策會-RES-15")
                .deviceType(DeviceType.Meter)
                .loadType(LoadType.M1)
                .resId("RES-0000AA-01")
                .enableStatus(EnableStatus.enable)
                .isMainLoad(true)
                .setupData(SetupData.builder().ct(BigDecimal.ONE).pt(BigDecimal.ONE).build())
                .build();
        mongoOperations.save(dp);
    }

    public void createSampleConfiguration() {
        QseProfile qe;
        qe = new QseProfile(99, "QSE-000001-01");
        qe.setDnpUrl("http://myagent:8585");
        qe.setName("QSE#1");
        qe.setStartTime(new Date());
        qe.setEndTime(null);
        log.info("creating qse {}", qe);
        qseRepo.save(qe);

        TxgProfile tg;
        tg = new TxgProfile(3, "TXG-000088-01");
        tg.setQseId(qe.getQseId());
        tg.setName("資策會");
        tg.setServiceType(TxgProfile.SERVICE_SR);
        tg.setStartTime(new Date());
        tg.setEndTime(null);
        tg.setEnableStatus(EnableStatus.enable);
        tg.setEfficiencyPrice(BigDecimal.valueOf(130.0));
        tg.setRegisterCapacity(BigDecimal.valueOf(2000.0));
        log.info(tg);
        txgRepo.save(tg);

        TxgFieldProfile rs;
        rs = new TxgFieldProfile(14, "RES-000099-01");
        rs.setTxgId(tg.getTxgId());
        rs.setName("資策會-RES-14");
        rs.setResType(TxgProfile.RESOURCE_DR);
        rs.setStartTime(new Date());
        rs.setEndTime(null);
        rs.setTcEnable(EnableStatus.enable);
        rs.setTcUrl("http://140.92.24.47:8060");
        rs.setRegisterCapacity(BigDecimal.valueOf(500.0));
        resRepo.save(rs);

        // TxgFieldProfile rs;
        rs = new TxgFieldProfile(15, "RES-0000AA-01");
        rs.setTxgId(tg.getTxgId());
        rs.setName("資策會-RES-15");
        rs.setResType(TxgProfile.RESOURCE_DR);
        rs.setStartTime(new Date());
        rs.setEndTime(null);
        rs.setTcEnable(EnableStatus.enable);
        rs.setTcUrl("http://140.92.24.47:8060");
        rs.setRegisterCapacity(BigDecimal.valueOf(1500.0));
        resRepo.save(rs);

        tg = new TxgProfile(1, "TXG-0000DR-01");
        tg.setQseId(qe.getQseId());
        tg.setName("資策會");
        tg.setServiceType(TxgProfile.SERVICE_DREG);
        tg.setStartTime(new Date());
        tg.setEndTime(null);
        tg.setEnableStatus(EnableStatus.enable);
        tg.setEfficiencyPrice(BigDecimal.valueOf(140.0));
        tg.setRegisterCapacity(BigDecimal.valueOf(2000.0));
        log.info(tg);
        txgRepo.save(tg);

        rs = new TxgFieldProfile(1, "RES-0000DR-01");
        rs.setTxgId(tg.getTxgId());
        rs.setName("RES-0000DR-01");
        rs.setResType(TxgProfile.RESOURCE_GESS);
        rs.setStartTime(new Date());
        rs.setEndTime(null);
        rs.setTcEnable(EnableStatus.enable);
        rs.setTcUrl("http://140.92.24.47:8060");
        rs.setRegisterCapacity(BigDecimal.valueOf(2000.0));
        resRepo.save(rs);
    }

    // public String loadFieldEMailAddress(String resId) {
    // return "hylinker07@gmail.com";
    // }

    // public String loadGroupEMailAddress(String txgId) {
    // return "hylinker07@gmail.com";
    // }

    public String[] loadGroupLineToken(TxgProfile tx) {
        List<TxgFieldProfile> flst = seekTxgFieldProfilesFromTxgId(tx.getTxgId());
        List<String> nlst = new ArrayList<>();
        if (!StringUtils.isBlank(tx.getLineToken())) {
            nlst.add(tx.getLineToken());
        }
        for (TxgFieldProfile f : flst) {
            if (!StringUtils.isBlank(f.getLineToken())) {
                nlst.add(f.getLineToken());
            }
        }
        if (nlst.size() == 0) {
            return null;
        }
        return nlst.toArray(new String[nlst.size()]);
    }

    public String[] loadLineToken(TxgProfile tx) {
        if (StringUtils.isBlank(tx.getLineToken())) {
            return null;
        }
        return new String[] { tx.getLineToken() };
    }

    public String[] loadLineToken(QseProfile tx) {
        if (StringUtils.isBlank(tx.getLineToken())) {
            return null;
        }
        return new String[] { tx.getLineToken() };
    }

    public String[] loadLineToken(TxgFieldProfile tx) {
        if (StringUtils.isBlank(tx.getLineToken())) {
            return null;
        }
        return new String[] { tx.getLineToken() };
    }

    // public String[] loadLineToken(QseProfile tx) {
    // if (StringUtils.isBlank(tx.getLineToken())) {
    // return null;
    // }
    // return new String[] { tx.getLineToken() };
    // }

    public String[] loadGroupEmails(TxgProfile tx) {
        List<TxgFieldProfile> flst = seekTxgFieldProfilesFromTxgId(tx.getTxgId());
        List<String> mlst = new ArrayList<>();
        UserProfile uf;
        uf = loadTxgUserProfile(tx.getTxgId());
        if (uf != null && uf.getEmail() != null) {
            mlst.add(uf.getEmail());
        }
        for (TxgFieldProfile f : flst) {
            uf = loadTxgUserProfile(f.getResId());
            if (uf != null && uf.getEmail() != null) {
                mlst.add(uf.getEmail());
            }
        }
        return mlst.toArray(new String[] {});
    }

    public String[] loadEmail(TxgFieldProfile fp) {
        UserProfile uf;
        uf = loadTxgUserProfile(fp.getResId());
        if (uf == null) {
            return null;
        }
        return new String[] { uf.getEmail() };
    }

    public String[] loadEmail(String kid) {
        UserProfile uf;
        uf = loadTxgUserProfile(kid);
        if (uf == null) {
            return null;
        }
        return new String[] { uf.getEmail() };
    }

    public static <T> T[] concatWithArrayCopy(T[] array1, T[] array2) {
        T[] result = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

    public String[] loadPhones(String... kids) {
        if (kids == null) {
            return null;
        }
        String[] ret = null;
        for (String kid : kids) {
            String[] x = loadPhones(kid);
            if (x == null) {
                continue;
            }
            if (ret == null) {
                ret = x;
            } else {
                ret = concatWithArrayCopy(ret, x);
            }
        }
        return ret;
    }

    public String[] loadPhones(String kid) {
        if (kid == null) {
            return null;
        }
        UserProfile uf;
        uf = loadTxgUserProfile(kid);
        if (uf == null) {
            return null;
        }
        return uf.getPhones();
    }

    public String[] loadResourceAndItsGroupEmail(TxgFieldProfile fp) {
        List<String> mlst = new ArrayList<>();
        UserProfile uf;
        uf = loadTxgUserProfile(fp.getResId());
        if (uf != null) {
            mlst.add(uf.getEmail());
        }
        TxgProfile txg = seekTxgProfileFromResId(fp.getResId());
        uf = loadTxgUserProfile(txg.getTxgId());
        if (uf != null) {
            mlst.add(uf.getEmail());
        }
        return mlst.toArray(new String[] {});
    }

    public CompanyProfile getTxgCompanyProfile(String comId) {
        Criteria criteria = Criteria.where("companyId").is(comId);
        Query searchQuery = new Query(criteria);
        List<CompanyProfile> lst = mongoOperations.find(searchQuery, CompanyProfile.class);
        if (lst.size() == 0) {
            return null;
        }
        return lst.get(0);
    }

    public UserProfile loadTxgUserProfile(String id) {
        Criteria criteria = Criteria.where("orgId._id").is(id);
        Query searchQuery = new Query(criteria);
        List<UserProfile> ulst = mongoOperations.find(searchQuery, UserProfile.class);
        if (ulst.size() == 0) {
            return null;
        }
        return ulst.get(0);
    }

    public TxgDeviceProfile seekTxgDeviceProfile(String devId) {
        Criteria criteria = Criteria.where("_id").is(devId);
        Query searchQuery = new Query(criteria);
        List<TxgDeviceProfile> lst = mongoOperations.find(searchQuery, TxgDeviceProfile.class);
        if (lst.size() == 0) {
            return null;
        }
        return lst.get(0);
    }

    public List<TxgDeviceProfile> seekTxgDeviceProfilesFromResId(String id) {
        Criteria criteria = Criteria.where("resId").is(id);
        Query searchQuery = new Query(criteria);
        List<TxgDeviceProfile> lst = mongoOperations.find(searchQuery, TxgDeviceProfile.class);
        if (lst.size() == 0) {
            return null;
        }
        return lst;
    }

    // public String loadDnpUrl(TxgProfile txg) {
    // List<QseProfile> qse = seekQseProfiles();
    // if (qse == null || qse.size() == 0) {
    // return null;
    // }
    // return qse.get(0).getDnpUrl();
    // }

    public Query makeIdQuery(String key, String val) {
        Criteria criteria = Criteria.where(key).is(val);
        return new Query(criteria);
    }

    public Query makeCodeQuery(String key, int val) {
        Criteria criteria = Criteria.where(key).is(val);
        return new Query(criteria);
    }

    public static <T> boolean isNone(List<T> obs) {
        if (obs == null || obs.size() == 0) {
            return true;
        }
        return false;
    }

    public static <T> boolean isUnique(List<T> obs) {
        if (obs == null || obs.size() != 1) {
            return false;
        }
        return true;
    }

    public QseProfile seekParent(TxgProfile tx) {
        Query searchQuery = makeIdQuery("qseId", tx.getQseId());
        List<QseProfile> lst = mongoOperations.find(searchQuery, QseProfile.class);
        if (!isUnique(lst)) {
            log.error("!!!Multiple QSE Parents found for {}", tx.getTxgId());
            return null;
        }
        return lst.get(0);
    }

    public List<TxgFieldProfile> seekChildren(TxgProfile tx) {
        Query searchQuery = makeIdQuery("txgId", tx.getTxgId());
        List<TxgFieldProfile> lst = mongoOperations.find(searchQuery, TxgFieldProfile.class);
        return lst;
    }

    public String[] seekChildrenId(TxgProfile tx) {
        Query searchQuery = makeIdQuery("txgId", tx.getTxgId());
        List<TxgFieldProfile> lst = mongoOperations.find(searchQuery, TxgFieldProfile.class);
        if(isNone(lst)){
            return null;
        }
        // String[] ret= new String[lst.size()];
        List<String> ilst = new ArrayList<>();
        lst.forEach(f -> ilst.add(f.getResId()));
        return ilst.toArray(new String[] {});
        // return lst;
    }

}
