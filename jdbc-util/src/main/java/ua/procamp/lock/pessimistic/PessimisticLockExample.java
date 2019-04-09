package ua.procamp.lock.pessimistic;

import ua.procamp.lock.Program;
import ua.procamp.lock.ProgramDao;
import ua.procamp.lock.ProgramDaoIml;

public class PessimisticLockExample {
    public static void main(String[] args) throws InterruptedException {

        ProgramDao programDao = new ProgramDaoIml();

        Thread thread1 = new Thread(() -> programDao.updateWithPessimisticLock(1L, programFromDB -> {
            programFromDB.setName("Thread1");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
            }
        }));
        thread1.setName("Thread-1");
        thread1.start();

        Thread.sleep(1000);

        Thread thread2 = new Thread(() -> {
            programDao.updateWithPessimisticLock(1L, programFromDB -> programFromDB.setName("Thread2"));
        });
        thread2.setName("Thread-2");
        thread2.start();

        thread1.join();
        thread2.join();

        Program programAfterUpdate = programDao.findById(1L);
        System.out.println(programAfterUpdate);
    }
}
