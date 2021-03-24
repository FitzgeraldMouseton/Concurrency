package atomiccounter;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CASing - Compare and Swap.
 * Использование синхронизации требует затрат ресурсов, но нужно не всегда, даже когда несколько
 * потоков работают с общей областью памяти. Например, когда они записывают значение в общую
 * переменну, в большинстве случаев они это будут делать в разное время. Для того, чтобы не помещать
 * эту переменную в синхронизированный блок, но при этом быть спокойным за результат мы применяем
 * концепцию CAS. При записи в переменную нового значения, мы сначала должны удостовериться, что
 * значение соответствует ожидаемому, и никакой поток не изменил его с момента последнего чтения.
 * В зависимости от этого возвращаем true и производим запись или возвращаем false. Все эти действия
 * выполняются атомарно, то есть как одно действие.
 * Например, мы инкрементируем переменную. Если значение не соответствует ожидаемому, значит какой-то поток
 * буквально только что изменил значение переменной, тогда данная операция отменяется и предпринимается
 * новая попытка.
 * Однако это хорошо работает в случае, когда потоков не слишком много (сколько - ˜\_()_/˜), т.к. в случае
 * с синхронизацией поток подошел к синх-му блоку и сидит тихонечко, ждёт, а тут каждый поток постоянно
 * пытается записать что-то своё в переменную, а другие в это время яростно переписывают ожидаемое им значение.
 *
 * В java эта концепция реализована при помощи atomic переменных.
 * Методы этих переменных реализованы по принципу modify + get, или get + modify. Однако иногда get не нужен.
 * Для таких случаев есть LongAdder и LongAccumulator.
 */

public class AtomicCounter {

	private static class MyAtomicCounter extends AtomicInteger {
		
		private static Unsafe unsafe = null;
		static {
			Field unsafeField;
			try {
				unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
				unsafeField.setAccessible(true);
				unsafe = (Unsafe) unsafeField.get(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private AtomicInteger countIncrement = new AtomicInteger(0);
		
		public MyAtomicCounter(int counter) {
			super(counter);
		}
	
		public int myIncrementAndGet() {

			long valueOffset = 0L;
			try {
				valueOffset = unsafe.objectFieldOffset(AtomicInteger.class.getDeclaredField("value"));
			} catch (NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
			}
			int v;
	        do {
	            v = unsafe.getIntVolatile(this, valueOffset);
	            countIncrement.incrementAndGet();
	        } while (!unsafe.compareAndSwapInt(this, valueOffset, v, v + 1));
	        
	        return v;
		}
		
		public int getIncrements() {
			return this.countIncrement.get();
		}
	}
	
	private static MyAtomicCounter counter = new MyAtomicCounter(0);
	
	public static void main(String[] args) {

		class Incrementer implements Runnable {
			
			public void run() {
				for (int i = 0 ; i < 1_000 ; i++) {
					counter.myIncrementAndGet();
				}
			}
		}
		
		class Decrementer implements Runnable {
			
			public void run() {
				for (int i = 0 ; i < 1_000 ; i++) {
					counter.decrementAndGet();
				}
			}
		}
		
		ExecutorService executorService = Executors.newFixedThreadPool(8);
		List<Future<?>> futures = new ArrayList<>();
		
		try {
				
			for (int i = 0 ; i < 4 ; i++) {
				futures.add(executorService.submit(new Incrementer()));
			}
			for (int i = 0 ; i < 4 ; i++) {
				futures.add(executorService.submit(new Decrementer()));
			}
			
			futures.forEach(
				future -> {
					try {
						future.get();
					} catch (InterruptedException | ExecutionException e) {
						System.out.println(e.getMessage());
					}
				}
			);
			
			System.out.println("counter = " + counter);
			System.out.println("# increments = " + counter.getIncrements());
			
		} finally {
			executorService.shutdown();
		}
	}
}
