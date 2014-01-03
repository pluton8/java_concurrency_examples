/**
 * Two threads are doing some job in pieces. Each one must give control to the
 * other one after doing its one piece of work. The wait()/notify() methods are
 * used on the same object to block when ready to do some work and unblock when
 * notified.
 *
 * Also, to give each thread a right to start first, every one sleeps for a
 * random amount of time (0…5 ms.) before start.
 *
 * Created by u on 2014-01-02.
 */
public class e20_pingpongnotification {
    static class Worker implements Runnable {
        private final int id;
        private final Object LOCK;
        private final char payload;

        Worker(int id, char payload, Object lock) {
            this.id = id;
            this.payload = payload;
            this.LOCK = lock;
        }

        @Override
        public void run() {
            randomWait();

            for (int workPiece = 0; workPiece < 100; ++workPiece) {
                // without synchronized() you'll get an
                // IllegalMonitorStateException
                synchronized (LOCK) {
                    try {
                        LOCK.wait(10);
                        for (int i = 0; i < 10; ++i) {
                            doJob(payload);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    LOCK.notify();
                }
            }
        }

        private void randomWait() {
            int delay = (int) (Math.random() * 5);
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void doJob(char c) {
        System.out.print(c);
        System.out.flush();
    }

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();

        final Object LOCK = new Object();
        Thread t0 = new Thread(new Worker(0, '.', LOCK), "Worker0");
        Thread t1 = new Thread(new Worker(1, '$', LOCK), "Worker1");

        t0.start();
        t1.start();

        t0.join();
        t1.join();

        long finish = System.currentTimeMillis();
        System.out.println("\nDone in " + (finish - start) + " ms.");
    }
}
