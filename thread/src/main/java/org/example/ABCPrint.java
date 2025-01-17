package org.example;

public class ABCPrint {

    public static void main(String[] args) throws InterruptedException {
        Thread printA =  new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("A");
            }
        });

        Thread printB =  new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("B");
            }
        });

        Thread printC =  new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("C");
            }
        });

        printA.start();
        printA.join();
        printB.start();
        printB.join();
        printC.start();
        printC.join();
    }





}
