import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The approach here is WRONG! Do not use it.
 *
 * A classical, simplified example of the producer and consumer pattern. Here a
 * Producer works in its own thread, produces a product every 100 ms., and
 * notifies about that its listener (on the producer's thread!). The listener is
 * a Consumer, which processes the product in 50 ms.
 *
 * The Producer creates 5 products every 100 ms., so the total run time should
 * be about 500 ms. In order not to block the producer in its thread, the
 * Consumer puts the product in its own BlockingQueue which is polled in the
 * thread it's working in. Thus, the Producer is able to produce the next
 * product right after the previous one is handed to the listener.
 *
 * To stop the Consumer's blocking queue, we use the thread's interrupt()
 * method, which sets the isInterrupted flag. The Consumer's loop should be
 * stopped by it, and the take() method throws an InterruptedException when
 * interrupt() is called. However, in this sample the sleep() during product
 * consumption is more likely to be interrupted. Plus, in case consumption takes
 * more time than production, and the queue will have some extra products in it,
 * we don't want to interrupt the Consumer before everything is processed.
 *
 * Created by u on 2014-01-07.
 */
public class e111_prodcon_blockingqueue_interrupt_WRONG {
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

    static class Consumer implements Runnable, ProducerListener {
        private BlockingQueue<Integer> productsQueue;
        private Thread consumerThread;

        @Override
        public void run() {
            productsQueue = new LinkedBlockingQueue<Integer>();
            consumerThread = Thread.currentThread();

            while (!Thread.interrupted()) {
                Integer product;
                try {
                    product = productsQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Logger.log("Hurray, the end! Time to rest.");
                    break;
                }

                consumeProduct();
                Logger.log("Consumed %d, nom-nom", product);
            }

            consumerThread = null;
        }

        @Override
        public void producerCreatedProduct(Producer producer, int product) {
            Logger.log("Producer created product %d, pushing to the queue",
                    product);
            if (!productsQueue.offer(product)) {
                Logger.log(
                        "Failed to put the product to the queue, dropping it");
            }
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
            consumerThread.interrupt();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();

        final Consumer consumer = new Consumer();
        final Producer producer = new Producer(consumer);
        Thread producerThread = new Thread(producer, "ProducerThread");
        Thread consumerThread = new Thread(consumer, "ConsumerThread");

        producerThread.start();
        consumerThread.start();

        producerThread.join();
        consumerThread.join();

        long finish = System.currentTimeMillis();
        System.out.println("\nDone in " + (finish - start) + " ms.");
    }
}
