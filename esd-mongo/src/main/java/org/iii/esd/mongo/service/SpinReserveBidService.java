package org.iii.esd.mongo.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import org.iii.esd.mongo.document.SpinReserveBid;
import org.iii.esd.mongo.document.SpinReserveProfile;
import org.iii.esd.mongo.repository.SpinReserveBidRepository;

@Service
@Deprecated
public class SpinReserveBidService {

    @Autowired
    private SpinReserveBidRepository spinReserveBidRepo;

    public void addOrUpdateAll(Long srId, List<SpinReserveBid> spinReserveBidList) {
        for (SpinReserveBid spinReserveBid : spinReserveBidList) {
            spinReserveBid.setSpinReserveProfile(new SpinReserveProfile(srId));
            Optional<SpinReserveBid> existingOne = findOneBySrIdAndTime(srId, spinReserveBid.getTimestamp());
            if (existingOne.isPresent()) {
                spinReserveBid.setCreateTime(existingOne.get().getCreateTime());
                spinReserveBid.setId(existingOne.get().getId());
            }
        }
        spinReserveBidRepo.saveAll(spinReserveBidList);
    }

    public Optional<SpinReserveBid> findOneBySrIdAndTime(Long id, Date timestamp) {
        return spinReserveBidRepo
                .findOne(Example.of(SpinReserveBid.builder().spinReserveProfile(new SpinReserveProfile(id)).timestamp(timestamp).build()));
    }

    public List<SpinReserveBid> findAll() {
        return spinReserveBidRepo.findAll();
    }

    public List<SpinReserveBid> findAllBySrIdAndTime(Long id, Date start) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        calendar.add(Calendar.DATE, 1);
        Date end = calendar.getTime();
        return spinReserveBidRepo.findBySrIdAndTime(id, start, end);
    }

    public List<SpinReserveBid> findAllBySrIdAndTime(Long id, Date start, Date end) {
        return spinReserveBidRepo.findBySrIdAndTime(id, start, end);
    }

}