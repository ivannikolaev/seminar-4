package ru.tinkoff.edu;

public class Door {

    private boolean lock1;
    private boolean lock2;

    public void doLock1() {
        lock1 = true;
    }

    public void doLock2() {
        lock2 = true;
    }

    public boolean locked() {
        return !lock1 && !lock2;
    }
}
