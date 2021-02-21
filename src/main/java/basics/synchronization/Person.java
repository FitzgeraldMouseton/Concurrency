package basics.synchronization;

/**
 * Разберем ситуацию, когда мы хотим синхронизировать сразу два метода - getName() и getAge();
 *
 * NB: Reentrant lock - ситуациая, когда один поток вызвал метод, получит ключ, а этот метод вызывает другой метод,
 * заблокированный тем же ключом. Получается, треду нужен доступ ко второму методу методу, а ключ он сам же и держит.
 * Что делать? Да ничего, у треда ж ключ, он просто заходит в нужный метод, да и все. В целом, ситуация выеденного яйца не стоит.
 * Другое дело Deadlock - когда два потока взаимно держат ключи, нужные другому потоку для продолжения работы. Вот это
 * реально днище.
 */
public class Person {

    private final String name;
    private final int age;

    private final Object firstLock = new Object();
    private final Object secondLock = new Object();

    private static final Object fullLock = new Object();

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }


    // 1) Блокирующим объектом будет инстанс класса, поэтому если Т1 вызвал getNameA, Т2 будет невозможно
    // одновременно вызвать getAgeA. Однако разные объекты могут одновременно вызывать методы
    public synchronized String getNameA() {
        return name;
    }

    public synchronized int getAgeA() {
        return age;
    }

    // 2) Блокирующим объектом будет сам Person.class, поэтому в один момент времени может быть вызван только один
    // из методов одного из объектов класса
    public String getNameB() {
        synchronized (fullLock) {
            return name;
        }
    }

    public int getAgeB() {
        synchronized (fullLock) {
            return age;
        }
    }

    // 3) Для каждого метода свой объект - соответственно можно одновременно вызывать методы
    public String getNameC() {
        synchronized (firstLock) {
            return name;
        }
    }

    public int getAgeC() {
        synchronized (secondLock) {
            return age;
        }
    }
}
