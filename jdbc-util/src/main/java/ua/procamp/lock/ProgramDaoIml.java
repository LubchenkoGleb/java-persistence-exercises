package ua.procamp.lock;

import ua.procamp.util.JdbcUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.function.Consumer;

public class ProgramDaoIml implements ProgramDao {

    private static final String SELECT_QUERY = "select * from programs where id = ?";

    private static final String SELECT_QUERY_FOR_UPDATE_QUERY = "select * from programs where id = ? for update";

    private static final String UPDATE_QUERY = "update programs set name = ?, description = ?, version = ? where id = ? and version = ?";

    private static final DataSource dataSource = JdbcUtil.createPostgresDataSource(
            "jdbc:postgresql://localhost:5432/postgres",
            "gleb",
            "root");

    @Override
    public Program findById(long id) {
        try (Connection connection = dataSource.getConnection()) {
            return findById(SELECT_QUERY, connection, id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateWithOptimisticLock(Program program) {
        try (Connection connection = dataSource.getConnection()) {
            executeUpdate(connection, program);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateWithPessimisticLock(Long programId, Consumer<Program> programConsumer) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            System.out.println(Thread.currentThread().getName() + ", BeforeSelectForUpdate: " + LocalDateTime.now());
            Program programFromDb = findById(SELECT_QUERY_FOR_UPDATE_QUERY, connection, programId);
            programConsumer.accept(programFromDb);
            executeUpdate(connection, programFromDb);
            connection.commit();
            System.out.println(Thread.currentThread().getName() + ", AfterTransaction: " + LocalDateTime.now());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void executeUpdate(Connection connection, Program program) throws SQLException {
        PreparedStatement updateStatement = connection.prepareStatement(UPDATE_QUERY);
        setUpdateParam(updateStatement, program);
        int result = updateStatement.executeUpdate();
        if (result == 0) {
            throw new RuntimeException("Optimistic lock exception");
        }
    }

    private Program resultSetToProgram(ResultSet resultSet) throws SQLException {
        return new Program(
                resultSet.getLong(1),
                resultSet.getString(2),
                resultSet.getString(3),
                resultSet.getInt(4));
    }

    private Program findById(String query, Connection connection, long id) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setLong(1, id);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return resultSetToProgram(resultSet);
    }

    private void setUpdateParam(PreparedStatement preparedStatement, Program program) throws SQLException {
        preparedStatement.setString(1, program.getName());
        preparedStatement.setString(2, program.getDescription());
        preparedStatement.setInt(3, program.getVersion() + 1);
        preparedStatement.setLong(4, program.getId());
        preparedStatement.setInt(5, program.getVersion());
    }
}
