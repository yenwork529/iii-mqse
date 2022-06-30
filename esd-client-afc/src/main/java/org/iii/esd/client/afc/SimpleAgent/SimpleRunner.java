// package org.iii.esd.client.afc.SimpleAgent;

// public class SimpleRunner implements ISimpleAgentRunner<Integer> {

//     Integer n=0;

//     public Integer Read() {
//         System.out.println("SimpleRunnerA.read."+n.toString());
//         return n;
//     }

//     public void Write(Integer ob) {
//         System.out.println("SimpleRunnerA.write."+ob.toString());
//         n = ob;
//     }

//     @Override
//     public void run() {
//         for (;;){
//             n++;
//             System.out.println("SimpleRunnerA.running." + n.toString());
//             ISimpleAgent.mSleep(30L);
//         }
//     }

// }
