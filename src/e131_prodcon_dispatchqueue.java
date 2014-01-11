import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A classical, simplified example of the producer and consumer pattern. Here a
 * Producer works in its own thread, produces a product every 100 ms., and
 * notifies about that its listener. The listener is a Consumer, which processes
 * the product. A Consumer has a listener as well to notify it when it's done.
 *
 * NOTA BENE: in this example, the listener's callback functions are called
 * asynchronously and explicitly on the specified DispatchQueue. (The
 * DispatchQueue class is a humble and very simple mock of the dispatch_queue_t
 * type in Apple's GCD library.) The serial queue implies that one of the
 * callbacks will not be called while another one is executing.
 *
 * How to stop the queue though? In a usual case, the Consumer doesn't care
 * exactly which queue it's running in as long as it's the same one for every
 * invocation (to get rid of messy synchronization). In this sample, the main
 * thread is responsible for creation and shutting down the queue. To know when
 * to shut down and join() the queue thread, the main object implements the
 * ConsumerListener, and waits until the Consumer is done (using Lock and
 * Condition objects). The rest is easy, the object shuts down the queue (with a
 * sentinel), and joins its thread.
 *
 * Created by u on 2014-01-11.
 */
public class e131_prodcon_dispatchqueue {
    static interface ProducerListener {
        void producerCreatedProduct(Producer producer, int product);

        void producerFinished(Producer producer);
    }

    static class DispatchQueue implements Runnable {
        final BlockingQueue<Runnable> runnables =
                new LinkedBlockingQueue<Runnable>();
        private static final Runnable SENTINEL = new Runnable() {
            @Override
            public void run() {
            }
        };

        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    final Runnable runnable = runnables.take();
                    if (SENTINEL == runnable) {
                        break;
                    }
                    runnable.run();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void dispatchAsync(Runnable runnable) {
            try {
                runnables.put(runnable);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void shutdown() {
            dispatchAsync(SENTINEL);
        }
    }

    static class Producer implements Runnable {
        final DispatchQueue listenerQueue;
        ProducerListener listener;

        Producer(ProducerListener listener, DispatchQueue listenerQueue) {
            this.listener = listener;
            this.listenerQueue = listenerQueue;
        }

        @Override
        public void run() {
            Logger.log("Starting producer");
            for (int i = 0; i < 5; ++i) {
                waitForProduct();
                Logger.log("Created product %d", i);

                if (isListenerReady()) {
                    final int finalI = i;
                    listenerQueue.dispatchAsync(new Runnable() {
                        @Override
                        public void run() {
                            listener.producerCreatedProduct(Producer.this,
                                    finalI);
                        }
                    });
                }
            }

            if (isListenerReady()) {
                listenerQueue.dispatchAsync(new Runnable() {
                    @Override
                    public void run() {
                        listener.producerFinished(Producer.this);
                    }
                });
            }
        }

        private boolean isListenerReady() {
            return (listener != null) && (listenerQueue != null);
        }

        private void waitForProduct() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static interface ConsumerListener {
        void consumerFinished(Consumer consumer);
    }

    static class Consumer implements ProducerListener {
        final ConsumerListener listener;

        Consumer(ConsumerListener listener) {
            this.listener = listener;
        }

        @Override
        public void producerCreatedProduct(Producer producer, int product) {
            Logger.log("Producer created product %d", product);
            consumeProduct();
            Logger.log("Consumed %d, nom-nom", product);
        }

        private void consumeProduct() {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void producerFinished(Producer producer) {
            Logger.log("Producer finished");
            if (listener != null) {
                listener.consumerFinished(this);
            }
        }
    }

    static class Sample implements ConsumerListener {
        private final Lock lock = new ReentrantLock();
        private final Condition finishedCondition = lock.newCondition();
        private boolean finished = false;

        void run() throws InterruptedException {
            DispatchQueue workerQueue = new DispatchQueue();

            final Consumer consumer = new Consumer(this);
            final Producer producer = new Producer(consumer, workerQueue);

            Thread producerThread = new Thread(producer, "ProducerThread");
            Thread workerQueueThread = new Thread(workerQueue,
                    "WorkerQueueThread");

            producerThread.start();
            workerQueueThread.start();

            producerThread.join();

            lock.lock();
            try {
                while (!finished) {
                    finishedCondition.await();
                }
                Logger.log("The process is finished, shutting down the queue");
                workerQueue.shutdown();
                workerQueueThread.join();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void consumerFinished(Consumer consumer) {
            lock.lock();
            try {
                Logger.log("Consumer finished");
                finished = true;
                finishedCondition.signal();
            } finally {
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();

        new Sample().run();

        long finish = System.currentTimeMillis();
        System.out.println("\nDone in " + (finish - start) + " ms.");
    }
}
