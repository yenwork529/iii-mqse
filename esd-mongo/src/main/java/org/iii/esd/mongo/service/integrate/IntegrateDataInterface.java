package org.iii.esd.mongo.service.integrate;

// import org.iii.esd.mongo.document.integrate.RelationshipProfile;
// import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
// import org.iii.esd.mongo.document.integrate.TxgProfile;
// import org.iii.esd.mongo.document.integrate.CgenResData;
// import org.iii.esd.mongo.document.integrate.QseProfile;
import org.iii.esd.mongo.document.integrate.*;

import java.util.*;

public interface IntegrateDataInterface {

    /**
     * Giving a *resProfile* and a time range between *from* <= t < *to*, this function
     * returns a XXXResData List according to its resource type.
     * 
     * @param <T>
     * @param resProfile
     * @param from
     * @param to
     * @return
     */
    // public <T> List<T>  loadResData(TxgFieldProfile resProfile, Date from, Date to);
    
    /**
     * Giving a *TxgProfile* and a time range between *from* <= t < *to*, 
     * this function returns a XXXResData List according to its resource type.
     * 
     * @param <T>
     * @param txgProfile
     * @param from
     * @param to
     * @return
     */
    // public <T> T loadResData(TxgProfile txgProfile, Date from, Date to);

    /**
     * Giving a *txgProfile* and a time range between *from* <= t < *to*, 
     * this function returns a XXXTxgData List according to its resource type.
     * 
     * @param <T>
     * @param txgProfile
     * @param from
     * @param to
     * @return
     */
    // public <T> T loadTxgData(TxgProfile txgProfile, Date from, Date to);

    /**
     * Giving a *txgProfile* and a time range between *from* <= t < *to*, 
     * this function returns a TxgBid List.
     * 
     * @param txgProfile
     * @param from
     * @param to
     * @return
     */
    // public List<TxgBid> loadBidData(TxgProfile txgProfile, Date from, Date to);

    /**
     * To update the TxgBid List
     * 
     * @param bidlst
     */
    public void updateBidData(List<TxgBid> bidlst);
}
