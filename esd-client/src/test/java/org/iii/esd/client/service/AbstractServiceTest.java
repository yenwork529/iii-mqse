package org.iii.esd.client.service;

import org.iii.esd.client.AbstractTest;
import org.iii.esd.mongo.config.MongoDBConfig;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
	classes = { MongoDBConfig.class}
)
public class AbstractServiceTest extends AbstractTest {

}