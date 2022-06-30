package org.iii.esd.server.service;

import org.springframework.test.context.ContextConfiguration;

import org.iii.esd.mongo.config.MongoDBConfig;
import org.iii.esd.server.AbstractTest;
import org.iii.esd.thirdparty.config.Config;

@ContextConfiguration(
        classes = {Config.class, MongoDBConfig.class}
)
public class AbstractServiceTest extends AbstractTest {

}