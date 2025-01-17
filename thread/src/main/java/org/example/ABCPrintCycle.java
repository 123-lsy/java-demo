package org.example;

public class ABCPrintCycle {

    private static int count = 1;
    public static void main(String[] args) {

        Thread t1 = new Thread(() ->{
            while(count<=21){
                synchronized (ABCPrintCycle.class){
                    if (count%3!=1){
                        try {
                            ABCPrintCycle.class.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                    }else {
                        System.out.println("A");
                        count++;
                        ABCPrintCycle.class.notifyAll();
                    }
                }
        }

    });
        Thread t2 = new Thread(() ->{
            while(count<=21){
                synchronized (ABCPrintCycle.class){
                    if (count%3!=2){
                        try {
                            ABCPrintCycle.class.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                    }else {
                        System.out.println("B");
                        count++;
                        ABCPrintCycle.class.notifyAll();
                    }
                }
            }

        });
        Thread t3 = new Thread(() ->{
            while(count<=21){
                synchronized (ABCPrintCycle.class){
                    if (count%3!=0){
                        try {
                            ABCPrintCycle.class.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                    }else {
                        System.out.println("C");
                        count++;
                        ABCPrintCycle.class.notifyAll();
                    }
                }
            }

        });
        
        t1.start();
        t2.start();
        t3.start();




}
}
