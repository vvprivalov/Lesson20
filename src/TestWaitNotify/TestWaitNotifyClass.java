package TestWaitNotify;

public class TestWaitNotifyClass {
    public static void main(String[] args) {

        WaitNotifyClass wts = new WaitNotifyClass();

        new Thread(() -> wts.print('A')).start();
        new Thread(() -> wts.print('B')).start();
        new Thread(() -> wts.print('C')).start();

    }
}
