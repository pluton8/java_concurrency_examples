/**
 * A classical, simplified example of the producer and consumer pattern. Here a
 * Producer works in its own thread, produces a product every 100 ms., and
 * notifies about that its listener. The listener is a Consumer, which processes
 * the product.
 *
 * The Producer creates 5 products every 100 ms., so the total run time should
 * be about 500 ms. However, since it takes 50 ms. for the Consumer to process a
 * product and the callback is synchronous, the total run time is at least 750
 * ms.
 *
 * Created by u on 2014-01-05.
 */
public class e100_producerconsumerbase {
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
        @Override
        public void run() {
            // strange, we're doing nothing here
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
