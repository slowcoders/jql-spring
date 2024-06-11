package org.slowcoders.hyperql.sample.jdbc.starwars.service;

import org.slowcoders.hyperql.HyperRepository;
import org.slowcoders.hyperql.HyperStorage;
import static org.slowcoders.hyperql.qb.Filter.*;

import org.slowcoders.hyperql.qb.Filter;
import org.slowcoders.hyperql.util.KVEntity;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

@Service
public class SecuredCharacterService {

    private final HyperRepository<Long> characterEntitySet;

    SecuredCharacterService(HyperStorage storage) {
        characterEntitySet = storage.loadRepository("starwars.character");
    }

    public HyperRepository<Long> getCharacterEntitySet() {
        return characterEntitySet;
    }

    public void deleteCharacter(Collection<Long> idList, String accessToken) {
        if ("1234".equals(accessToken)) {
            characterEntitySet.delete(idList);
        } else {
            throw new RuntimeException("Not authorized");
        }
    }

    public Long addNewCharacter(Map<String, Object> properties) {
        if (properties.get("metadata") == null) {
            properties.put("metadata", createNote());
        }
        Filter filter = _and_(
                _like_("name", "L%"),
                _in_("starship_",
                    _ge_("length", 15)
                )
        );

        return characterEntitySet.insert(properties);
    }

    private KVEntity createNote() {
        KVEntity entity = new KVEntity();
        entity.put("autoCreated", new Date());
        return entity;
    }

}
