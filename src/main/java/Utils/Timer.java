package Utils;
public class Timer extends Thread {
    private ModifiedCountdownLatch latch = null;
    private ModifiedBarrier barrier = null;

    private int tempo = 0;

    public Timer(ModifiedCountdownLatch latch) {
        this.latch = latch;
    }

    public Timer(int tempo, ModifiedBarrier barrier) {
        this.tempo = tempo;
        this.barrier = barrier;
    }

    @Override
    public void run() {
        try {
            if (latch != null) {
                Thread.sleep(latch.getWaitPeriod() * 1000);
                latch.tempoExpirado();
                System.out.println("LATCH, Tempo expirado! Todas as threads foram liberadas.");

            }
            if (barrier != null) {
                Thread.sleep(tempo * 1000);
                barrier.tempoExpirado();
                System.out.println(" BARREIRA, Tempo expirado! Todas as threads foram liberadas.");

            }
        } catch (InterruptedException e) {
            System.out.println("Timer interrompido!");
            Thread.currentThread().interrupt();
        }

    }



    public static void main(String[] args) {
        Timer t = new Timer(new ModifiedCountdownLatch(0,0,5,5)); // exemplo com 5 segundos
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            System.out.println("Main interrompida!");
            Thread.currentThread().interrupt();
        }
    }
}
