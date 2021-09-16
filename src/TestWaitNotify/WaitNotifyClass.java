package TestWaitNotify;

public class WaitNotifyClass {
    private final Object monitor = new Object();
    private static char currentLetter = 'A';

    public void print(char letter) {
        synchronized (monitor) {
            try {
                for (int i = 0; i < 5; i++) {
                    while (currentLetter != letter) {
                        monitor.wait();
                    }
                    System.out.print(letter);

                    if (letter == 'A') {
                        currentLetter = 'B';
                    } else if (letter == 'B') {
                        currentLetter = 'C';
                    } else {
                        currentLetter = 'A';
                    }

                    monitor.notifyAll();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
