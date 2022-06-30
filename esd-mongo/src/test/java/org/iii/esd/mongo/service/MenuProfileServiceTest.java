package org.iii.esd.mongo.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.mongo.document.MenuProfile;

@SpringBootTest(classes = {MenuProfileService.class})
@EnableAutoConfiguration
        //@Log4j2
class MenuProfileServiceTest extends AbstractServiceTest {

    @Autowired
    private MenuProfileService service;

    @Test
    @Disabled
    void testSave() {
        List<MenuProfile> list = new ArrayList<>();

        MenuProfile mpRoot = MenuProfile.builder().
                name("ROOT").
                                                order(1).
                                                uri("#").
                                                build();
        mpRoot.setId("ROOT");
        list.add(mpRoot);
        MenuProfile mp01 = MenuProfile.builder().
                name("系統管理").
                                              order(1).
                                              uri("#").
                                              parentMenuProfile(mpRoot).
                                              build();
        mp01.setId("func01");
        list.add(mp01);
        MenuProfile mp0101 = MenuProfile.builder().
                name("公司管理").
                                                order(1).
                                                uri("#").
                                                parentMenuProfile(mp01).
                                                build();
        mp0101.setId("func0101");
        list.add(mp0101);
        MenuProfile mp0102 = MenuProfile.builder().
                name("策略管理").
                                                order(2).
                                                uri("#").
                                                parentMenuProfile(mp01).
                                                build();
        mp0102.setId("func0102");
        list.add(mp0102);
        service.add(list);

    }

}
