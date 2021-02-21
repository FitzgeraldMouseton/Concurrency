package basics.synchronization;

/**
 * Race condition - возникает, когда несколько потоков одновременно пытаются получить доступ (и на чтение, и на запись)
 * к одной переменной.
 * Для предотвращения такой ситуации используется слово synchronized. Как это работает. Для синхронизации java использует
 * некоторый объект (может использоваться любой объект, на самом деле). Когда поток Т1 подходит к синкронизированному
 * блоку, этот объект дает ему ключ и Т1 попадает в блок и начинает что-то там делать. В это время к блоку подходит Т2,
 * но у объекта нет ключа, поэтому Т2 вынужден ждать. Тем временем Т1, закончив свои дела возвращает ключ объекту, объект
 * передает его Т2, и тот, наконец-то, попадает в блок. Ключ так же называют монитором. В разных ситуациях разные объекты
 * выступают хранителями ключа
 * 1) Статический класс - сам класс
 * 2) Нестатический класс - инстанс класса
 * 3) synchronized(some object) - тот объект, который мы сами передали в качестве параметра
 */

public class RaceConditionSingleton {

    private static RaceConditionSingleton instance;

    private RaceConditionSingleton() {
    }

    /*
    Такая реализация не годится для многопоточной среды, т.к. два потока могут одновременно зайти в блок if()
    public static basics.synchronization.RaceConditionSingleton getInstance() {
        if (instance == null) {
            instance = RaceConditionSingleton();
        }
        return instance;
    }
     */

    public static synchronized RaceConditionSingleton getInstance() {
        if (instance == null) {
            instance = new RaceConditionSingleton();
        }
        return instance;
    }
}
