package locks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ReentrantLock.
 * Для паттерна synchronized можно использовать любой объект. Однако иногда нужны дополнительные
 * возможности, которые предоставляет ReentrantLock. Например ситуация - поток в блоке synchronized
 * вследствие ошибки оказался заблокированным, что значит всё, приехали. Однако, если у нас синхр-я
 * осуществляется при помощи RL, то можно сделать так:
 *
 * Lock lock = new ReentrantLock();
 * try {
 *     lock.lock();
 *     // do some stuff
 * } finally {
 *     lock.unlock();
 * }
 *
 * Что еще хорошего приносит RL в нашу нелёгкую жизнь?
 * 1) lock.tryLock() - если поток подошёл к синхр-му блоку, а он занят, то tryLock() вернет false
 * и поток пойдет делать другие дела, а не стоять в очереди. Можно так же передать в метод время,
 * в течение которого поток всё-таки подождёт, вдруг блок снимется.
 * 2) Обычно, потоки толпятся перед синхр-м местом, и, когда наконец блок снимается, дальще проходит
 * рандомный поток. RL дает возможность сделать FIFO. Для этого надо сделать так -
 * Lock lock = new ReentrantLock(true). Дело это затратное и по умолчанию отключено.
 * 3) Не особо понятное. lock.lockInterruptibly() - позволяет некоторым образом с трудом, но прервать
 * блокировку.
 *
 * RL содержит метод для создания другого нужного нам объекта - Condition, у которого можно вызывать
 * методы await() и signal(), что является ананлогами wait() и notify().
 *
 * Бывают ситуации, когда мы хотим ограничить возможность записи, но разрешить параллельное чтение
 * несколькими потоками.
 * Для этого есть интерфейс ReadWriteLock, у которого имеются writeLock() и readLock(), возвращающие
 * инстансы Lock. Работает это очень логично -
 * а) Только один поток может удерживать writeLock. readLock в это время недоступен.
 * б) ReadLock может удерживаться любым количеством потоков
 *
 * Для демонстрации гибкости RL представим ситуацию, когда продюсер падает с ошибкой (здесь для этого
 * намеренно сделано деление на ноль) и ничего не производит. Соответственно ни один из консьюмеров
 * не начинают работу и программа виснет. Для решения этой проблемы мы можем сделать так, чтобы
 * консьюмер ждал некоторое время, и, если ничего не происходит, выбрасывал исключение.
 */

public class ProducerConsumerWithLocks {

	public static void main(String[] args) throws InterruptedException {

		List<Integer> buffer = new ArrayList<>();

		Lock lock = new ReentrantLock();
		Condition isEmpty = lock.newCondition();
		Condition isFull = lock.newCondition();

		class Consumer implements Callable<String> {

			public String call() throws InterruptedException, TimeoutException {
				int count = 0;
				while (count++ < 50) {
					try {
						lock.lock();
						while (isEmpty(buffer)) {
							// wait
							if (!isEmpty.await(10, TimeUnit.MILLISECONDS)) {
								throw new TimeoutException("Consumer time out");
							}
						}
						buffer.remove(buffer.size() - 1);
						// signal
						isFull.signalAll();
					} finally {
						lock.unlock();
					}
				}
				return "Consumed " + (count - 1);
			}
		}

		class Producer implements Callable<String> {

			public String call() throws InterruptedException {
				int count = 0;
				while (count++ < 50) {
					try {
						lock.lock();
						int i = 10/0;
						while (isFull(buffer)) {
							// wait
							isFull.await();
						}
						buffer.add(1);
						// signal
						isEmpty.signalAll();
					} finally {
						lock.unlock();
					}
				}
				return "Produced " + (count - 1);
			}
		}

		List<Producer> producers = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			producers.add(new Producer());
		}

		List<Consumer> consumers = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			consumers.add(new Consumer());
		}
		
		System.out.println("Producers and Consumers launched");
		
		List<Callable<String>> producersAndConsumers = new ArrayList<>();
		producersAndConsumers.addAll(producers);
		producersAndConsumers.addAll(consumers);

		ExecutorService executorService = Executors.newFixedThreadPool(8);
		try {
			List<Future<String>> futures = executorService.invokeAll(producersAndConsumers);

			futures.forEach(
					future -> {
						try {
							System.out.println(future.get());
						} catch (InterruptedException | ExecutionException e) {
							System.out.println("Exception: " + e.getMessage());
						}
					});

		} finally {
			executorService.shutdown();
			System.out.println("Executor service shut down");
		}

	}

	public static boolean isEmpty(List<Integer> buffer) {
		return buffer.size() == 0;
	}

	public static boolean isFull(List<Integer> buffer) {
		return buffer.size() == 10;
	}
}
