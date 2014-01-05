import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Two threads are doing some job in pieces. Each one must give control to the
 * other one after doing its one piece of work. A higher-level Lock object with
 * associated conditional variables are used to block when ready to do some work
 * and unblock when notified.
 *
 * Created by u on 2014-01-04.
 */
public class e30_conditionalvariable {
    static final int WORKERS_COUNT = 2;
    static Integer turn = 0;

    static class Worker implements Runnable {
        private final int id;
        private final Lock LOCK;
        private final char payload;
        private final Condition myTurn;
        private final Condition nextTurn;

        Worker(int id, char payload, Lock lock, Condition myTurn,
               Condition nextTurn) {
            this.id = id;
            this.payload = payload;

            this.LOCK = lock;
            this.myTurn = myTurn;
            this.nextTurn = nextTurn;
        }

        @Override
        public void run() {
            for (int workPiece = 0; workPiece < 100; ++workPiece) {
                LOCK.lock();
                try {
                    while (!isMyTurn()) {
                        myTurn.await();
                    }

                    for (int i = 0; i < 10; ++i) {
                        doJob(payload);
                    }

                    nextTurn();
                    nextTurn.signal();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    LOCK.unlock();
                }
            }
        }

        private boolean isMyTurn() {
            return turn == id;
        }

        private void nextTurn() {
            turn = (turn + 1) % WORKERS_COUNT;
        }
    }

    public static void doJob(char c) {
        System.out.print(c);
        System.out.flush();
    }

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();

        final Lock LOCK = new ReentrantLock();
        Condition t0Turn = LOCK.newCondition();
        Condition t1Turn = LOCK.newCondition();
        Thread t0 = new Thread(new Worker(0, '.', LOCK, t0Turn, t1Turn),
                "Worker0");
        Thread t1 = new Thread(new Worker(1, '$', LOCK, t1Turn, t0Turn),
                "Worker1");

        t0.start();
        t1.start();

        t0.join();
        t1.join();

        long finish = System.currentTimeMillis();
        System.out.println("\nDone in " + (finish - start) + " ms.");
    }
}
