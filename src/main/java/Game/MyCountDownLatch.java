package Game;

public class MyCountDownLatch {
    private int count;

    public MyCountDownLatch(int count) {
        if (count < 0) throw new IllegalArgumentException("count < 0");
        this.count = count;
    }

    // As threads chamam isto para esperar
    public synchronized void await() throws InterruptedException {
        while (count > 0) {
            wait();
        }
    }

    // O temporizador chama isto para reduzir o contador
    public synchronized void countDown() {
        if (count > 0) {
            count--;
            if (count == 0) {
                notifyAll(); // liberta todas as threads à espera
            }
        }
    }

    public synchronized int getCount() {
        return count;
    }
}
