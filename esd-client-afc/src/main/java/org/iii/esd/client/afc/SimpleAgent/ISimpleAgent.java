package org.iii.esd.client.afc.SimpleAgent;

import java.util.Calendar;
import java.util.Date;
import org.apache.commons.lang3.time.DateUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public interface ISimpleAgent<T> {

    
   
    public static void mSleep(long ms){
        try {
            Thread.sleep(ms);
        } catch (Exception ex){
            
        }

    }

    public static String toJson(Object ob){
        return new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").setPrettyPrinting().create()
                .toJson(ob);
    }

    public static <T> T fromJson(String si, Class<T> classOfT) {
        try {
            return new Gson().fromJson(si, classOfT);
        } catch (Exception ex) {
            return null;
        }
    }

    public static <T> T clone(T obj, Class<T> classOfT){
        String so = toJson(obj);
        T ro = fromJson(so, classOfT);
        return ro;
    }

    public static Long genId() {
    	Long id = (DateUtils.getFragmentInMilliseconds(new Date(), Calendar.MINUTE) %10000)/100;;
    	return id;
    }

    public T PullData();
    public void PushCommand(T ob);
    public ISimpleAgent<T> setRunner(ISimpleAgentRunner<T> rn);
    public Boolean isReady();
    public void Run();
}
