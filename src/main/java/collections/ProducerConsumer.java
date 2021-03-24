package collections;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Stack и Vector устарели - не используем.
 * CopyOnWriteArrayList и CopyOnWriteArraySet - при записи новых элементов вся внутренняя структура (массив)
 * копируется в новую. Т.о. имеет смысл использовать только тогда, когда у нас мало записей в коллекцию
 * и много обращений к ней. Например, записать в такую коллекци данные при инициализации программы.
 *
 * Очередь может быть блокируемой и неблокируемой. Так же есть просто очереди и двусторонние очереди.
 * У очередей есть несколько методов, для добавления новых элементов. Если очередь заполнена, то
 * add() - выбросит исключение;
 * offer() - вернет false;
 * put() - заблокирует очередь, пока не добавится новый элемент.
 *
 * ArrayBlockingQueue - нерасширяемая очередь.
 * ConcurrentLinkedQueue - расширяемая очередь.
 *
 *
 * ConcurrentHashMap создана для эффективной работы в сильно многопоточных средах с миллионами пар ключ-значение.
 * ConcurrentHashMap имеет несколько методов, где можно задать порог (поличество элементов), выше которого
 * вычисления будут производиться с применением паралеллизма
 * search(10_000, (k, v) -> v.startsWith("a") ? "a" : null), (также есть searchKey(), searchValue(), searchEntries())
 * reduce(10_000, (k, v) -> v.size(), (v1, v2) -> Integer.max(v1, v2))
 * foreach(10_000, (k, v) -> v.removeIf(s -> s.length() > 20)
 *
 * Для создания Concurrent Set используется следующий паттекрн
 * Set<String> set = ConcurrentHashMap.<String>newKeySet()
 *
 * SkipList structures. Используют AtomicReferences. Как и ConcurrentHashMap лучше всего использовать в случае,
 * когда много потоков и много данных. Тут нет проблемы, описанной в CAS, т.к. нет постоянной перезаписи данных
 * другими потоками, что являлось бы проблемой, если бы в нашей коллекции было мало данных.
 */

public class ProducerConsumer {

	public static void main(String[] args) throws InterruptedException {

		BlockingQueue<String> queue = new ArrayBlockingQueue<>(50);

		class Consumer implements Callable<String> {

			public String call() throws InterruptedException {
				int count = 0;
				while (count++ < 50) {
					queue.take();
				}
				return "Consumed " + (count - 1);
			}
		}

		class Producer implements Callable<String> {

			public String call() throws InterruptedException {
				int count = 0;
				while (count++ < 50) {
					queue.put(Integer.toString(count));
				}
				return "Produced " + (count - 1);
			}
		}

		List<Callable<String>> producersAndConsumers = new ArrayList<>();
		
		for (int i = 0; i < 2; i++) {
			producersAndConsumers.add(new Producer());
		}

		for (int i = 0; i < 2; i++) {
			producersAndConsumers.add(new Consumer());
		}

		System.out.println("Producers and Consumers launched");

		ExecutorService executorService = Executors.newFixedThreadPool(4);
		try {
			List<Future<String>> futures = executorService.invokeAll(producersAndConsumers);

			futures.forEach(future -> {
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
}
