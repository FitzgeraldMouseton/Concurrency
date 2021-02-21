package basics.synchronization;

import basics.waitnotify.LongWrapper;

/**
 * Если у нас есть многоядерный процессор, то мы можем выполнять несколько операций, производимых на компьютере,
 * одновременнно. Однако мы не можем контролировать, какое ядро выполняет конкретную операцию. Распределением
 * операций по ядрам занимается Thread Scheduler.
 */

public class ThreadBasics {
    public static void main(String[] args) throws InterruptedException {

        // Элементарный пример создание потока. Создаем экземпляр Runnable, в котором задаем действие, которое должен
        // выполнить поток, и передаем его потоку в качестве параметра. Не забываем вызвать start().
        Runnable task = () -> System.out.println("Current thread is: " + Thread.currentThread().getName());
        Thread t1 = new Thread(task);
        t1.setName("F-thread");
        t1.start();
        t1.run(); // Вызывает выполнение кода не в отдельном потоке, а в текущем (в данном случае - основной поток), не использовать!!!


        // Сравним выполнение одного и того же задания в разных обстоятельствах
        LongWrapper longWrapper = new LongWrapper(0L);

        Runnable runnable = () -> {
            for (int i = 0; i < 10000; i++) {
                longWrapper.incrementValue();
            }
        };

        // 1-поточная среда
        Thread t2 = new Thread(runnable);
        t2.start();

        // Waits for this thread to die. Последующий код начнет выполняться только после того, как завершится t2
        t2.join();

        System.out.println(longWrapper.getValue()); // 1000 - все хорошо

        // Многопоточная среда

        Thread[] threads = new Thread[5];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(runnable);
            threads[i].start();
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
        System.out.println(longWrapper.getValue());

        /**
         * Но вообще-то, это не лучший способ использовать многопоточность. Тут мы, получается, можем бесконтрольно создавать
         * треды, которые будут убиваться после выполнения задания. А создание, и уничтодение треда - дорогая операция,
         * как и другие операции, предоставляемые ОС. Поэтому лучше так не делать (особенно в энтерпрайзе), а использовать
         * интерфейс Executor
         */
    }
}
