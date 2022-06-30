package org.iii.esd.mongo.service.integrate;

import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.document.integrate.QseProfile;

import java.util.*;

public interface IntegrateRelationInterface {

    /**
     * Seek for available QSE Profiles
     */
    public List<QseProfile> seekQseProfiles();

    /**
     * Seek TxgProfiles for current time.
     * @return
     */
    public List<TxgProfile> seekTxgProfiles();

    /**
     * Seek TxgProfiles from QSE Id for current time.
     * @param qseId
     * @return
     */
    public List<TxgProfile> seekTxgProfilesFromQseId(String qseId);

    /**
     * Seek Txg Profiles from QSE Id for the specific date.
     * @param qseId
     * @param dt
     * @return
     */
    public List<TxgProfile> seekTxgProfilesFromQseIdAndDate(String qseId, Date dt);

    /**
     * Seek Txg Profiles for the specific date.
     * @param dt
     * @return
     */
    public List<TxgProfile> seekTxgProfilesForDate(Date dt);

    /**
     * Seek txg Profile from txgCode for current time.
     * @param code
     * @return
     */
    public TxgProfile seekTxgProfileFromTxgCode(Integer code);

    /**
     * Seek Txg Profile from txgCode for the specific date.
     * @param code
     * @param dt
     * @return
     */
    public TxgProfile seekTxgProfileFromTxgCodeAndDate(Integer code, Date dt);

    /**
     * Seek txg Profile from txgId
     * @param txgId
     * @return
     */
    public TxgProfile seekTxgProfileFromTxgId(String txgId);

    /**
     * Seek txg Profile from txgId for specific date
     * @param txgId
     * @param dt
     * @return
     */
    public TxgProfile seekTxgProfileFromTxgIdAndDate(String txgId, Date dt);

    /**
     * Seek Txg Profile from RES Id for current time.
     * @param resId
     * @return
     */
    public TxgProfile seekTxgProfileFromResId(String resId);

    /**
     * Seek Txg Profile from RES Id for the specific date.
     * @param resId
     * @param dt
     */
    public TxgProfile seekTxgProfileFromResIdAndDate(String resId, Date dt);

    /**
     * Seek Txg Field Profiles for TXG Id  for current time.
     */
    public List<TxgFieldProfile> seekTxgFieldProfilesFromTxgId(String txgId);

    /**
     * Seek Txg Field Profiles for TXG Id for the specific date.
     */
    public List<TxgFieldProfile> seekTxgFieldProfilesFromTxgIdAndDate(String txgId, Date dt);

}
