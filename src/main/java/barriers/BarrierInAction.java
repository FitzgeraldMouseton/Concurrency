package barriers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Сегодня, ребята, мы с вами познакомимся с новым объектом - CyclicBarrier. Для чего он нужен?
 * Допустим, у нас есть задание обработать большой массив данных несколькими потоками. Потоки закончат работу
 * в разное время, и для того, чтобы собрать их вместе прежде, чем переходить к следующему этапу программы,
 * мы как раз и используем барьер.
 *
 * Для этого мы создаем CyclicBarrier, в который параметром конструктора передаём количество потоков,
 * которое он будет собирать. Например, четыре. Затем создаем Callable, в котором, после выполнения
 * основной работы пишем barrier.awaits(). И метод awaits() будет вызван у barrier 4 раза, он пропустит
 * процесс дальше и счетчик вызовов сбросится.
 *
 * Представим другую ситуацию - у нас есть большое приложение, и мы хотим чтобы сначала запустились
 * определенные сервисы, а потом уж само приложение. Может показаться, что это работа для CyclicBarrier.
 * Но тут действие-то разовое, а барьер цикличный. Сбросится и продолжит работу и снова может что-нибудь
 * заблокировать. Таким образом нам нужен одноразовый барьер, который больше не закроется после открытия.
 * И у нас есть такой барьер - CountDownLatch.
 * Как вариант, можно использовать в таком случае - нужно, чтобы три задания запустились одно за другим
 * в нужной последовательности. Для этого можно поставить одну заслонку после первого задания, вторую -
 * после второго.
 *
 * В данном примере смоделирована ситуация, когда четыре друга решили пойти в кино. Они едут из разных
 * частей города и приезжают в разное время, дожидаются последнего и идут.
 */

public class BarrierInAction {

	
	public static void main(String[] args) {

		class Friend implements Callable<String> {

			private final CyclicBarrier barrier;
		
			public Friend(CyclicBarrier barrier) {
				this.barrier = barrier;
			}
			
			public String call() throws Exception {
				
				try {
					Random random = new Random();
					Thread.sleep((random.nextInt(20)*100 + 100));
					System.out.println("I just arrived, waiting for the others...");
					
					barrier.await();
					
					System.out.println("Let's go to the cinema!");
					return "ok";
				} catch(InterruptedException e) {
					System.out.println("Interrupted");
				}
				return "nok";
			}
		}
		
		ExecutorService executorService = Executors.newFixedThreadPool(4);

		CyclicBarrier barrier = new CyclicBarrier(4, () -> System.out.println("Barrier is opening"));
		List<Future<String>> futures = new ArrayList<>();
		
		try {
			for (int i = 0 ; i < 4 ; i++) {
				Friend friend = new Friend(barrier);
				futures.add(executorService.submit(friend));
			}
			
			futures.forEach(
				future -> {
					try {
						future.get(2000, TimeUnit.MILLISECONDS);
					} catch (InterruptedException | ExecutionException e) {
						System.out.println(e.getMessage());
					} catch (TimeoutException e) {
						System.out.println("Timed out");
						future.cancel(true);
					}
				}
			);
			
		} finally {
			executorService.shutdown();
		}
	}
}
