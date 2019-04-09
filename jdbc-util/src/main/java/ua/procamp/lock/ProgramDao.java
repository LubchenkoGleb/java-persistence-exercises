package ua.procamp.lock;

import java.util.function.Consumer;

public interface ProgramDao {

    Program findById(long id);

    void updateWithOptimisticLock(Program program);


    void updateWithPessimisticLock(Long programId, Consumer<Program> programConsumer);
}
