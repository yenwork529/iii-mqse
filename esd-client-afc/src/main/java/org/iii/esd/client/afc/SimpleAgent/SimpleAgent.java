package org.iii.esd.client.afc.SimpleAgent;

public class SimpleAgent implements ISimpleAgent<Integer> {
    
    ISimpleAgentRunner<Integer> rn;

    public Integer PullData(){
        return rn.Read();
    }

    public void PushCommand(Integer ob)
    {
        rn.Write(ob);
    }

    public SimpleAgent setRunner(ISimpleAgentRunner<Integer> rn){
        this.rn = rn;
        return this;
    }

    public void Run(){
        new Thread(rn).start();
    }

    public Boolean isReady(){
        return rn.isReady();
    }
}
