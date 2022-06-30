package org.iii.esd.mongo.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.iii.esd.mongo.document.MenuProfile;
import org.iii.esd.mongo.repository.MenuProfileRepository;

@Service
public class MenuProfileService {

    @Autowired
    private MenuProfileRepository menuProfileRepo;

    public MenuProfile add(MenuProfile menuProfile) {
        menuProfile.setCreateTime(new Date());
        menuProfile.setUpdateTime(new Date());
        return menuProfileRepo.insert(menuProfile);
    }

    public List<MenuProfile> add(List<MenuProfile> list) {
        list.forEach(mp -> {
            mp.setCreateTime(new Date());
            mp.setUpdateTime(new Date());
        });
        return menuProfileRepo.insert(list);
    }

}
