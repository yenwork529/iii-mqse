package org.iii.esd.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import org.iii.esd.mongo.document.MenuProfile;

public interface MenuProfileRepository extends MongoRepository<MenuProfile, String> {

}
