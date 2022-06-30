package org.iii.esd.client.afc.SimpleAgent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class SimpleQueueRunner<T> implements ISimpleAgentRunner<T> {

    BlockingQueue<T> que = new ArrayBlockingQueue<>(10);

    public Boolean isReady() {
        return false;
    }

    public Long Counter(){
        return 0L;
    }

    public T Read() {
        try {
            T n = que.take();
            return n;
        } catch (Exception ex) {
            return null;
        }
    }

    public void Write(T ob) {
        try {
            if (que.remainingCapacity() < 2) {
                que.take();
            }
            que.put(ob);
        } catch (Exception ex) {

        }
    }

    @Override
    public void run() {
        for (;;) {
            // n++;
            // System.out.println("SimpleRunnerA.running." + n.toString());
            ISimpleAgent.mSleep(30L);
        }
    }

}
