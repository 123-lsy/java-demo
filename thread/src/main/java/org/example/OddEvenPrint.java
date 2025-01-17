package org.example;

public class OddEvenPrint {

    private static int count = 0;

    private static class Odd implements Runnable {
        @Override
        public void run() {
            while (count < 10) {
                synchronized (OddEvenPrint.class) {
                    while (count % 2 == 0) {
                        try {
                            OddEvenPrint.class.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("Odd: " + count);
                    count++;
                    OddEvenPrint.class.notifyAll();
                }
            }
        }
    }

    private static class Even implements Runnable {
        @Override
        public void run() {
            while (count < 10) {
                synchronized (OddEvenPrint.class) {
                    while (count % 2 == 1) {
                        try {
                            OddEvenPrint.class.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("Even: " + count);
                    count++;
                    OddEvenPrint.class.notifyAll();
                }
            }
        }
    }

    public static void main(String[] args) {
        Thread t1 = new Thread(new Odd());
        Thread t2 = new Thread(new Even());
        t1.start();
        t2.start();
    }
}
