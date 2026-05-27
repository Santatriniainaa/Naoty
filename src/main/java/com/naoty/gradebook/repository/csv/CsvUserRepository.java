package com.naoty.gradebook.repository.csv;

import com.naoty.gradebook.model.Role;
import com.naoty.gradebook.model.User;
import com.naoty.gradebook.repository.UserRepository;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class CsvUserRepository extends AbstractCsvRepository<User, String>
        implements UserRepository {

    public CsvUserRepository(Path file, Path backupDir) {
        super(file, backupDir, new Mapper(), User::id);
    }

    public static CsvMapper<User> mapper() { return new Mapper(); }

    @Override public List<User> findAll()                  { return super.findAll(); }
    @Override public Optional<User> findById(String id)    { return super.findById(id); }
    @Override public User save(User e)                     { return super.save(e); }
    @Override public void deleteById(String id)            { super.deleteById(id); }

    @Override
    public List<User> findByRole(Role role) {
        return findAll().stream().filter(u -> u.role() == role).toList();
    }

    private static class Mapper implements CsvMapper<User> {
        @Override public String[] headers() {
            return new String[]{"id", "role", "nom", "prenom"};
        }
        @Override public String[] toRow(User u) {
            return new String[]{u.id(), u.role().name(), u.nom(), u.prenom()};
        }
        @Override public User fromRow(String[] r) {
            return new User(r[0], Role.valueOf(r[1]), r[2], r[3]);
        }
    }
}
