package org.softwire.training.metadataStore;

import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.softwire.training.models.Image;

import java.util.List;
import java.util.Optional;

@RegisterConstructorMapper(Image.class)
public interface ImageMetadataDao {

    @SqlQuery("SELECT id, user, author, name, filename, license, score, thumbnail FROM images WHERE user = ? ORDER BY RAND() LIMIT 1")
    Optional<Image> getRandom(String user);

    @SqlQuery("SELECT id, user, author, name, filename, license, score, thumbnail FROM images WHERE user = ? AND id = ?")
    Optional<Image> get(String user, int id);

    @SqlQuery("SELECT id, user, author, name, filename, license, score, thumbnail FROM images WHERE user = ?")
    List<Image> get(String user);

    @SqlUpdate("INSERT INTO images (user, author, name, filename, license, score, thumbnail) VALUES (:user, :author, :name, :filename, :license, :score, :thumbnail)")
    @GetGeneratedKeys
    int insert(@BindBean Image image);

    @SqlUpdate("DELETE FROM images WHERE user = ? AND id = ?")
    int delete(String user, int id);

    @SqlUpdate("DELETE FROM images WHERE user = ?")
    int deleteAll(String user);

    @SqlUpdate("UPDATE images SET score = score + 1 WHERE user = ? AND id = ?")
    int incrementScore(String user, int id);

    @SqlUpdate("UPDATE images SET score = GREATEST(0, score - 1) WHERE user = ? AND id = ?")
    int decrementScore(String user, int id);

    @SqlQuery("SELECT id, user, author, name, filename, license, score, thumbnail FROM images WHERE user = ? ORDER BY score DESC LIMIT 1")
    Optional<Image> getTopScoring(String user);
}
