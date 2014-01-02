/**
 * Two threads are trying to do some job at the same time (output a symbol), but
 * one of them must finish its job before the other one steps in. A shared lock
 * object is used to synchronize the run() method.
 *
 * Also, to give each thread a right to start first, every one sleeps for a
 * random amount of time (0…5 ms.) before start.
 *
 * Created by u on 2013-12-29.
 */
public class e1_fullblocksynchronization {
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
            int delay = (int) (Math.random() * 5);
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            synchronized (LOCK) {
                for (int i = 0; i < 1000; ++i) {
                    doJob(payload);
                }
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
