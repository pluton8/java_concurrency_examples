import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * A classical, simplified example of the producer and consumer pattern. Here a
 * Producer works in its own thread, produces a product every 100 ms., and
 * notifies about that its listener. The listener here is a ProductManager which
 * creates a new Consumer for each new product, and then submits it for
 * execution to its ExecutorService. Each instance of Consumer has its own
 * created product and processes it for 50 ms.
 *
 * This implementation is much more flexible than the previous ones, because:
 *
 * 0. it separates the ProductManager (which only implements ProducerListener)
 * from the actual Consumer;
 *
 * 1. it allows you to create ExecutorService with different parameters (at
 * least, # of threads in the pool).
 *
 * Created by u on 2014-01-07.
 */
public class e120_prodcon_executorservice {
    static interface ProducerListener {
        void producerCreatedProduct(Producer producer, int product);

        void producerFinished(Producer producer);
    }

    static class Producer implements Runnable {
        ProducerListener listener;

        Producer(ProducerListener listener) {
            this.listener = listener;
        }

        @Override
        public void run() {
            for (int i = 0; i < 5; ++i) {
                waitForProduct();

                if (listener != null) {
                    listener.producerCreatedProduct(this, i);
                }
            }

            if (listener != null) {
                listener.producerFinished(this);
            }
        }

        private void waitForProduct() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class Consumer implements Runnable {
        private final int product;

        public Consumer(int product) {
            this.product = product;
        }

        @Override
        public void run() {
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
    }

    static class ProductManager implements ProducerListener {
        private final ExecutorService execService;

        public ProductManager(ExecutorService execService) {
            this.execService = execService;
        }

        @Override
        public void producerCreatedProduct(Producer producer, int product) {
            Logger.log("Producer created product %d", product);
            if (execService != null) {
                Consumer consumer = new Consumer(product);
                execService.execute(consumer);
            }
        }

        @Override
        public void producerFinished(Producer producer) {
            Logger.log("Producer finished");
            if (execService != null) {
                execService.shutdown();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();

        final ExecutorService execService = Executors.newFixedThreadPool(1);
        final ProductManager productManager = new ProductManager(execService);
        final Producer producer = new Producer(productManager);

        Thread producerThread = new Thread(producer, "ProducerThread");
        producerThread.start();

        producerThread.join();
        final boolean terminated = execService.awaitTermination(2, SECONDS);
        if (!terminated) {
            Logger.log("WARNING: Executor service was not terminated on time!");
        }

        long finish = System.currentTimeMillis();
        System.out.println("\nDone in " + (finish - start) + " ms.");
    }
}
