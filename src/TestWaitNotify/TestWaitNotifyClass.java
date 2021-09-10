package TestWaitNotify;

public class TestWaitNotifyClass {
    public static void main(String[] args) {
        WaitNotifyClass wts = new WaitNotifyClass();

        new Thread(wts::printA).start();
        new Thread(wts::printB).start();
        new Thread(wts::printC).start();


    }
}
