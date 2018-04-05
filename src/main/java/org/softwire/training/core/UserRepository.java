package org.softwire.training.core;


import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.transaction.Transaction;
import org.softwire.training.metadataStore.ImageMetadataDao;
import org.softwire.training.metadataStore.UserDao;
import org.softwire.training.models.Image;
import org.softwire.training.models.User;
import org.softwire.training.rawImageStore.RawImageDao;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public interface UserRepository {

    @CreateSqlObject
    UserDao userDao();

    @CreateSqlObject
    ImageMetadataDao imageMetadataDao();

    @Transaction
    default void deleteUser(RawImageDao rawImageDao, String uuid) {
        List<String> filenames = imageMetadataDao()
                .get(uuid)
                .stream()
                .map(Image::getFilename)
                .collect(Collectors.toList());
        imageMetadataDao().deleteAll(uuid);
        userDao().delete(uuid);
        rawImageDao.delete(filenames);
    }

    default String createUser(String name) {
        String uuid = UUID.randomUUID().toString();
        User user = new User(uuid, name);
        userDao().add(user);
        return uuid;
    }
}
