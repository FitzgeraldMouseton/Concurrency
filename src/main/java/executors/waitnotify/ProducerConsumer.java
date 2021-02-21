package executors.waitnotify;

import java.util.Random;

public class ProducerConsumer {
    private static final Object lock = new Object();
    private static Integer[] buffer;
    private static int count;

    public static void main(String[] args) throws InterruptedException {

        buffer = new Integer[10];
        count = 0;
        Producer producer = new Producer();
        Consumer consumer = new Consumer();

        Runnable produceTask = () -> {
            for (int i = 0; i < 5000; i++) {
                producer.produce();
            }
            System.out.println("Done producing");
        };

        Runnable consumeTask = () -> {
            for (int i = 0; i < 4990; i++) {
                consumer.consume();
            }
            System.out.println("Done consuming");
        };

        Thread producerThread = new Thread(produceTask);
        Thread consumerThread = new Thread(consumeTask);

        producerThread.start();
        consumerThread.start();

        producerThread.join();
        consumerThread.join();

        System.out.println("Data in the buffer: " + count);

    }

    static class Producer {

        private void produce () {
            synchronized (lock) {
                if (count == buffer.length) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                buffer[count++] = new Random().nextInt();
                lock.notifyAll();
            }
        }
    }

    static class Consumer {
        private void consume ()  {
            synchronized (lock) {
                if (count == 0) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                buffer[--count] = 0;
                lock.notifyAll();
            }
        }
    }
}
