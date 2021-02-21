package executors;

import java.util.concurrent.*;

/**
 * Вместо создания нового потока под каждое нове задание, гораздо лучше иметь пул из нескольких постоянно имеющихся
 * потоков, которым можно передавать задания. Это весьма распространенный в программировании подход и про создание
 * отдельных тредов в реальных приложениях лучше даже особо и не вспоминать.
 *
 * Королевой-маткой при таком подходе является интерфейс Executor, имеющий один метод execute(Runnable). Его экстендит
 * другой интерфейс ExecutorService (+10 методов), а от него расходятся все остальные имплементации.
 * Executors - класс со статическими методами для создания всякой всячины.
 * Два наиболее используемых варианта
 * 1) Executors.newSingleThreadExecutor() - часто используется в реактивном программировании
 * 2) Executors.newFixedThreadPool(n)
 * Менее популярны
 * 3) Executors.newCachedThreadPool() - создает по мере необходимости тред, сохраняет его 60 секунд после
 * выполнения задания, потом уничтожает. Можно использовать, если у нас иногда возникает необходимость что-то сделать
 * в отдельном потоке (например, раз в несколько часов); большую часть времени он будет пустым.
 * 4) Executors.newScheduledTreadPool(n) -
 * Executor нужно закрывать методом shutdown() в блоке finally на всякий случай
 * Три способа закрыть executor:
 * 1) shutdown() - дождется выполнения текущих и ожидающих заданий, но отклонит новые. Затем завершит процесс.
 * 2) shutdownNow() - сразу прикроет лавочку
 * 3) awaitTermination(timeout) - сначала действует как shutdown(), а по истечении таймаута - как shutdownNow()
 *
 * К недостаткам Runnable можно отнести то, что оно не возвращает никакого значения, и не бросает исключения,
 * т.е. если в теле runnable есть код, выбрасывающий исключение, runnable не может его пробросить дальше
 * И то, и другое реализовано в Callable. Callable можно передавать в ExecutorServices, в которых имеется метод
 * submit(). Этот метод возвращает объект Future. То есть, мы создаем Callable в main-потоке, передаем
 * его в executor, который выполняет это задане в отдельном потоке и в этот же поток возвращает результат
 * в виде Future, а потом перемещает его в главный поток поток. Таким образом Future - это объект для переноса
 * результата из одного потока в другой.
 */
public class Basics {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        //==============================================================================================================

        Runnable task1 = () -> System.out.println(Thread.currentThread().getName());
        Runnable task2 = () -> System.out.println(Thread.currentThread().getContextClassLoader());

        /*
         Создание executor'а. Еще одно отличие от простого создания тредов - executor гарантирует, что task2 будет
        выполнен после task1. Когда в пуле не хватает тредов (а тут single), то последующие задания становятся
        в очередь в том порядке, в котором выполняется код (задания добавляются в waiting queue)
         */
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(task1);
        executor.execute(task2);

        //==============================================================================================================

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Callable<String> callable = () -> "Fuck you!";
        Future<String> future = executorService.submit(callable);
        System.out.println(future.get());
    }
}
