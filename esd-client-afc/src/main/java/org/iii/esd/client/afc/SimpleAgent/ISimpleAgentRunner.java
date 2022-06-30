package org.iii.esd.client.afc.SimpleAgent;

public interface ISimpleAgentRunner<T> extends Runnable{
    
    public T Read();
    public void Write(T ob);
    public Boolean isReady();
    public Long Counter();
}
