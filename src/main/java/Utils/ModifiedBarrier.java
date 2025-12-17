package Utils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
Class: ModifiedBarrier

Public constructors:
 - ModifiedBarrier(int totalPlayers, Runnable barrierAction)

Public methods:
 - void chegouJogador()
 - void tempoExpirado()
 - void await() throws InterruptedException
 - void reset()
 - boolean isComplete()

Notes:
 - Simple barrier with timeout support and a single barrier action.
*/

public class ModifiedBarrier {

    private final Lock lock = new ReentrantLock();
    private final Condition todosChegaram = lock.newCondition();

    private final int totalPlayers;
    private final Runnable barrierAction;
    private final AtomicInteger arrivedPlayers = new AtomicInteger(0);
    private boolean tempoExpirado = false;
    private boolean acaoExecutada = false;

    public ModifiedBarrier(int totalPlayers, Runnable barrierAction) {
        this.totalPlayers = totalPlayers;
        this.barrierAction = barrierAction;
    }

    public void chegouJogador() {
        lock.lock();
        try {
            arrivedPlayers.incrementAndGet();
            if (arrivedPlayers.get() >= totalPlayers && !acaoExecutada) {
                acaoExecutada = true;
                barrierAction.run();
                todosChegaram.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    public void tempoExpirado() {
        lock.lock();
        try {
            tempoExpirado = true;
            if (!acaoExecutada) {
                acaoExecutada = true;
                barrierAction.run();
            }
            todosChegaram.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void await() throws InterruptedException {
        lock.lock();
        try {
            while (arrivedPlayers.get() < totalPlayers && !tempoExpirado) {
                todosChegaram.await();
            }
        } finally {
            lock.unlock();
        }
    }

    public void reset() {
        lock.lock();
        try {
            arrivedPlayers.set(0);
            tempoExpirado = false;
            acaoExecutada = false;
        } finally {
            lock.unlock();
        }
    }

    public boolean isComplete() {
        lock.lock();
        try {
            return tempoExpirado || acaoExecutada;
        } finally {
            lock.unlock();
        }
    }
}
