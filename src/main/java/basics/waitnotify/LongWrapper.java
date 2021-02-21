package basics.waitnotify;

/**
 * Класс для демонстрации race condition
 * Раскомментировать, чтобы победить
 */

public class LongWrapper {

    private long l;
//    private final Object lock = new Object();

    public LongWrapper(long l) {
        this.l = l;
    }

    public long getValue() {
        return l;
    }

    public void incrementValue() {
//        synchronized (lock) {
            l = l + 1;
//        }
    }
}
