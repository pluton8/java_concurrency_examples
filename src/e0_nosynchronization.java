/**
 * The simplest example of concurrency. Two threads do some job at the same time
 * (output a symbol) without any synchronization.
 *
 * Created by u on 2013-12-29.
 */
public class e0_nosynchronization {
    static class Worker implements Runnable {
        private final int id;

        Worker(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            for (int i = 0; i < 1000; ++i) {
                doJob(id);
            }
        }
    }

    public static void doJob(int id) {
        char c = '\0';
        switch (id) {
            case 0:
                c = '.';
                break;

            case 1:
                c = '$';
                break;
        }

        System.out.print(c);
        System.out.flush();
    }

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();

        Thread t0 = new Thread(new Worker(0), "Worker0");
        Thread t1 = new Thread(new Worker(1), "Worker1");

        t0.start();
        t1.start();

        t0.join();
        t1.join();

        long finish = System.currentTimeMillis();
        System.out.println("\nDone in " + (finish - start) + " ms.");
    }
}
