package Utils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Timer extends Thread {
    private final ModifiedCountdownLatch latch;
    private final ModifiedBarrier barrier;

    public Timer(ModifiedCountdownLatch latch) {
        this.latch = latch;
    }

    public Timer(ModifiedBarrier barrier) {
        this.barrier = barrier;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(latch.getWaitPeriod() * 1000);
            if(barrier != null){
                barrier.tempoExpirado();
            } else {
                latch.tempoExpirado();
            }
            System.out.println("Tempo expirado! Todas as threads foram liberadas.");
        } catch (InterruptedException e) {
            System.out.println("Timer interrompido!");
        }

    }



//    public static void main(String[] args) {
//        Timer t = new Timer(5); // exemplo com 5 segundos
//        t.start();
//        try {
//            t.join();
//        } catch (InterruptedException e) {
//            System.out.println("Main interrompida!");
//            Thread.currentThread().interrupt();
//        }
//    }
}
