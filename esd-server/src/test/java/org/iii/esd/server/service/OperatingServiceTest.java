package org.iii.esd.server.service;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.exception.WebException;
import org.iii.esd.mongo.document.SiloUserProfile;
import org.iii.esd.mongo.document.integrate.UserProfile;
import org.iii.esd.mongo.service.SiloCompanyProfileService;
import org.iii.esd.mongo.service.FieldProfileService;
import org.iii.esd.mongo.service.SpinReserveService;
import org.iii.esd.mongo.service.SiloUserService;
import org.iii.esd.api.vo.OrgTree;
import org.iii.esd.mongo.service.integrate.UserService;
import org.iii.esd.server.services.OperatingService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
        OperatingService.class,
        SiloUserService.class,
        SiloCompanyProfileService.class,
        SpinReserveService.class,
        FieldProfileService.class,})
@EnableAutoConfiguration
@Log4j2
public class OperatingServiceTest extends AbstractServiceTest {

    @Autowired
    private OperatingService operatingService;
    @Autowired
    private SiloUserService siloUserService;
    @Autowired
    private UserService userService;

    private static final String TEST_QSE_USER = "qse@iii.org.tw";
    private static final String TEST_TXG_USER = "txg@iii.org.tw";
    private static final String TEST_RES_USER = "res14@iii.org.tw";

    @Test
    public void testBuildOrgTreeOfQse() throws WebException {
        SiloUserProfile user = siloUserService.findByEmail(TEST_QSE_USER);
        assertThat(user).isNotNull();

        OrgTree orgTree = operatingService.buildOrgTreeFromUser(user);
        assertThat(orgTree).isNotNull();
        assertThat(orgTree.getMyUnit().getUnitType()).isEqualTo(OrgTree.Type.QSE);

        log.info(orgTree);
    }

    @Test
    public void testBuildOrgTreeOfTxg() throws WebException {
        SiloUserProfile user = siloUserService.findByEmail(TEST_TXG_USER);
        assertThat(user).isNotNull();

        OrgTree orgTree = operatingService.buildOrgTreeFromUser(user);
        assertThat(orgTree).isNotNull();
        assertThat(orgTree.getMyUnit().getUnitType()).isEqualTo(OrgTree.Type.TXG);

        log.info(orgTree);
    }

    @Test
    public void testBuildOrgTreeOfRes() throws WebException {
        SiloUserProfile user = siloUserService.findByEmail(TEST_RES_USER);
        assertThat(user).isNotNull();

        OrgTree orgTree = operatingService.buildOrgTreeFromUser(user);
        assertThat(orgTree).isNotNull();
        assertThat(orgTree.getMyUnit().getUnitType()).isEqualTo(OrgTree.Type.RES);

        log.info(orgTree);
    }

    private static final String TEST_QSE1_USER = "qse1@iii.org.tw";
    private static final String TEST_TXG1_USER = "txg1@iii.org.tw";
    private static final String TEST_RES1_USER = "res1@iii.org.tw";
    private static final String TEST_RES3_USER = "res3@iii.org.tw";
    private static final String TEST_RES4_USER = "res4@iii.org.tw";

    @Test
    public void testBuildOrgTreeOfMine() throws WebException{
        UserProfile user = userService.findByEmail(TEST_RES4_USER);
        OrgTree topOrgTree = operatingService.buildTopOrgTree();
        log.info("top orgTree {}", topOrgTree);

        OrgTree myOrgTree = operatingService.filterOrgTreeByMyAuthor(topOrgTree, user);
        log.info("my orgTree {}", myOrgTree);
    }
}
