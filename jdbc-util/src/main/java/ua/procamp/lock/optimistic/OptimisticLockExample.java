package ua.procamp.lock.optimistic;

import ua.procamp.lock.Program;
import ua.procamp.lock.ProgramDao;
import ua.procamp.lock.ProgramDaoIml;

public class OptimisticLockExample {

    public static void main(String[] args) {
        ProgramDao optimisticLock = new ProgramDaoIml();

        Program program = optimisticLock.findById(1L);

        program.setName("try1");
        optimisticLock.updateWithOptimisticLock(program);

        program.setName("try2");
        optimisticLock.updateWithOptimisticLock(program);
    }
}
