package org.example;

import java.util.concurrent.Semaphore;

public class OddEvenPrint2 {

    private static Semaphore oddSem =  new Semaphore(1);
    private static Semaphore evenSem =  new Semaphore(0);

    public static void main(String[] args) {

        Thread oddThread = new Thread( ()->{
            for (int i = 1; i <=  10; i=i+2) {
                try {
                    oddSem.acquire();
                    System.out.println(i);
                    evenSem.release();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Thread evenThread = new Thread(() -> {
            for (int i = 2; i <= 10; i = i + 2) {
                try {
                    evenSem.acquire();
                    System.out.println(i);
                    oddSem.release();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        oddThread.start();
        evenThread.start();

    }
}
