package org.softwire.training.metadataStore;

import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.softwire.training.models.User;

import java.util.List;
import java.util.Optional;

@RegisterConstructorMapper(User.class)
public interface UserDao {

    @SqlQuery("SELECT uuid, name FROM users WHERE uuid = ?")
    Optional<User> getByUuid(String uuid);

    @SqlQuery("SELECT uuid, name FROM users")
    List<User> getAll();

    @SqlUpdate("INSERT INTO users (uuid, name) VALUES (:uuid, :name)")
    void add(@BindBean User user);

    @SqlUpdate("DELETE FROM users WHERE uuid = ?")
    int delete(String uuid);
}
