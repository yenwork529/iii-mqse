package org.iii.esd.mongo.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.DemandResponseProfile;
import org.iii.esd.mongo.repository.DemandResponseProfileRepository;

@Service
public class DemandResponseService {

    @Autowired
    private DemandResponseProfileRepository drProfileRepo;

    //	@Autowired
    //	private SpinReserveProfileRepository spProfileRepo;

    @Autowired
    private UpdateService updateService;

    //	@Autowired
    //	private SpinReserveBidService spinReserveBidService;
    //
    //	@Autowired
    //	private StatisticsService statisticsService;
    //
    //	private int defaultScale = 3;

    public long addDemandResponseProfile(DemandResponseProfile drProfile) {
        drProfile.setId(updateService.genSeq(DemandResponseProfile.class));
        drProfile.setCreateTime(new Date());
        drProfileRepo.insert(drProfile);
        return drProfile.getId();
    }

    public DemandResponseProfile updateDemandResponseProfile(DemandResponseProfile drProfile) {
        return drProfileRepo.save(drProfile);
    }

    public Optional<DemandResponseProfile> findDemandResponseProfile(Long id) {
        return drProfileRepo.findById(id);
    }

    public List<DemandResponseProfile> findDemandResponseProfileByDrIdSet(Set<Long> ids) {
        return drProfileRepo.findByIdIn(ids);
    }

    public List<DemandResponseProfile> findAllDemandResponseProfile() {
        return drProfileRepo.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    public List<DemandResponseProfile> findEnableDemandResponseProfile() {
        return drProfileRepo.findByEnableStatusNotOrderById(EnableStatus.disable);
    }

    public List<DemandResponseProfile> findDemandResponseProfileByCompanyIdAndEnableStatus(Long companyId, EnableStatus enableStatus) {
        return findDemandResponseProfileByExample(new DemandResponseProfile(companyId, enableStatus));
    }

    public List<DemandResponseProfile> findDemandResponseProfileByExample(DemandResponseProfile demandResponseProfile) {
        return drProfileRepo.findAll(Example.of(demandResponseProfile), Sort.by(Sort.Direction.ASC, "id"));
    }

    public void deleteDemandResponseProfile(Long id) {
        if (id != null) {
            drProfileRepo.deleteById(id);
        }
    }

    //	public long addSpinReserveProfile(SpinReserveProfile srProfile) {
    //		srProfile.setId(updateService.genSeq(SpinReserveProfile.class));
    //		srProfile.setCreateTime(new Date());
    //		spProfileRepo.insert(srProfile);
    //		return srProfile.getId();
    //	}
    //
    //	public SpinReserveProfile updateSpinReserveProfile(SpinReserveProfile srProfile) {
    //		return spProfileRepo.save(srProfile);
    //	}
    //
    //	public Optional<SpinReserveProfile> findSpinReserveProfile(Long id) {
    //		return spProfileRepo.findById(id);
    //	}
    //
    //	public List<SpinReserveProfile> findSpinReserveProfileBySrIdSet(Set<Long> ids) {
    //		return spProfileRepo.findByIdIn(ids);
    //	}
    //
    //	public List<SpinReserveProfile> findAllSpinReserveProfile() {
    //		return spProfileRepo.findAll(Sort.by(Sort.Direction.ASC, "id"));
    //	}
    //
    //	public List<SpinReserveProfile> findEnableSpinReserveProfile() {
    //		return spProfileRepo.findByEnableStatusNotOrderById(EnableStatus.disable);
    //	}
    //
    //	public List<SpinReserveProfile> findSpinReserveProfileByCompanyIdAndEnableStatus(Long companyId, EnableStatus enableStatus) {
    //		return findSpinReserveProfileByExample(new SpinReserveProfile(companyId, enableStatus));
    //	}
    //
    //	public List<SpinReserveProfile> findSpinReserveProfileByExample(SpinReserveProfile spinReserveProfile) {
    //		return spProfileRepo.findAll(Example.of(spinReserveProfile), Sort.by(Sort.Direction.ASC, "id"));
    //	}
    //
    //	public void deleteSpinReserveProfile(Long id) {
    //		if(id!=null) {
    //			spProfileRepo.deleteById(id);
    //		}
    //	}
    //
    //	public Boolean updateQuitBySrIdAndTime(Long srId, Date time) {
    //		// 超過22:30 不用做棄標
    //		if (LocalTime.fromDateFields(time).compareTo(new LocalTime(22, 30)) < 0) {
    //			Date start = DatetimeUtils.truncated(DatetimeUtils.add(time, Calendar.MINUTE, 90), Calendar.HOUR);
    //			Date end = DatetimeUtils.getLastTimeOfDay(time);
    //			return updateService.updateColumn1ByColumn2("operatorStatus", OperatorStatus.QUIT, "id",
    //					spinReserveBidService.findAllBySrIdAndTime(srId, start, end).stream().map(srBid->srBid.getId()).collect(Collectors.toSet()),
    //				SpinReserveBid.class, true);
    //		}else {
    //			return false;
    //		}
    //	}
    //
    //	/**
    //	 * 依據得標容量計算即時備轉各場域降載配比
    //	 * @param srId
    //	 * @param time
    //	 */
    //	public Map<Long, Double> calculateFieldSpinReserveBidRatioBySridAndTime(Long srId, Date time) {
    //		Optional<SpinReserveBid> spinReserveBid = spinReserveBidService.findOneBySrIdAndTime(srId, time);
    //		if (spinReserveBid.isPresent()) {
    //			BigDecimal awarded_capacity = spinReserveBid.get().getAwarded_capacity();
    //			return spinReserveBid.get().getList().stream().collect(
    //					Collectors.groupingBy(
    //							e->e.getFieldProfile().getId(),
    //							Collectors.averagingDouble(d->d.getAwarded_capacity().divide(awarded_capacity, defaultScale, BigDecimal.ROUND_HALF_UP).doubleValue() )
    //					)
    //			);
    //		} else {
    //			return new HashMap<>();
    //		}
    //	}
    //
    //	/**
    //	 * 計算即時備轉基準線(前五分鐘平均用電)
    //	 * @param noticeTime
    //	 * @param fieldId
    //	 */
    //	public BigDecimal calculateBaseLine(Date noticeTime, Long... fieldId) {
    //		if (fieldId.length > 0) {
    //			List<ElectricData> edList = statisticsService.findElectricDataByFieldIdAndDataTypeAndTime(
    //					new HashSet<Long>(Arrays.asList(fieldId)), DataType.T99,
    //					DatetimeUtils.add(noticeTime, Calendar.MINUTE, -5), noticeTime);
    //
    //			Double value = edList.stream()
    //					.collect(Collectors.groupingBy(ElectricData::getTime,
    //							Collectors.reducing(new ElectricData(), ElectricData::sum)))
    //					.values().stream().map(ed -> ed.getM1kW())
    //					.collect(Collectors.averagingDouble(d -> d.doubleValue()));
    //
    //			return value != null ? new BigDecimal(value.toString()).setScale(defaultScale ,BigDecimal.ROUND_HALF_UP) : null;
    //		} else {
    //			return null;
    //		}
    //	}

}