--- 780 ~ 840 ms
SELECT
    t_0.name, t_0.height, t_1.name, t_1.height, t_2.name, t_2.height, t_3.name,
       t_3.height, t_4.name, t_4.height, t_5.name, t_5.height
FROM starwars.character as t_0
         inner join starwars.character_friend_link as t_a on
        t_a.character_id = t_0.id
         inner join starwars.character as t_1 on
        t_a.friend_id = t_1.id
         inner join starwars.character_friend_link as t_b on
        t_b.character_id = t_1.id
         inner join starwars.character as t_2 on
        t_b.friend_id = t_2.id
         inner join starwars.character_friend_link as t_c on
        t_c.character_id = t_2.id
         inner join starwars.character as t_3 on
        t_c.friend_id = t_3.id
         inner join starwars.character_friend_link as t_d on
        t_d.character_id = t_3.id
         inner join starwars.character as t_4 on
        t_d.friend_id = t_4.id
         inner join starwars.character_friend_link as t_e on
        t_e.character_id = t_4.id
         inner join starwars.character as t_5 on
        t_e.friend_id = t_5.id
;

---------
--- 620 ~ 670 ms
SELECT json_build_object(
   'name', u.name,
   '+friend', (
       SELECT json_agg(json_build_object(
               'name', t_1.name,
               'height', t_1.height,
               '+friend', (
                   SELECT json_agg(json_build_object(
                           'name', t_2.name,
                           'height', t_2.height,
                           '+friend', (
                               SELECT json_agg(json_build_object(
                                       'name', t_3.name,
                                       'height', t_3.height,
                                       '+friend', (
                                           SELECT json_agg(json_build_object(
                                                   'name', t_4.name,
                                                   'height', t_4.height,
                                                   '+friend', (
                                                       SELECT json_agg(json_build_object(
                                                               'name', t_5.name,
                                                               'height', t_5.height
                                                           ))
                                                       FROM starwars.character_friend_link as t_e
                                                                inner join starwars.character as t_5 on
                                                               t_e.friend_id = t_5.id
                                                       where t_e.character_id = t_4.id
                                                   )
                                               ))
                                           FROM starwars.character_friend_link as t_d
                                                    inner join starwars.character as t_4 on
                                                   t_d.friend_id = t_4.id
                                           where t_d.character_id = t_3.id
                                       )
                                   ))
                               FROM starwars.character_friend_link as t_c
                                        inner join starwars.character as t_3 on
                                       t_c.friend_id = t_3.id
                               where t_c.character_id = t_2.id
                           )
                       ))
                   FROM starwars.character_friend_link as t_b
                            inner join starwars.character as t_2 on
                           t_b.friend_id = t_2.id
                   where
                           t_b.character_id = t_1.id
               )
           ))
       FROM starwars.character_friend_link as t_a
                inner join starwars.character as t_1 on
               t_a.friend_id = t_1.id
       where
               t_a.character_id = u.id
   )
)
FROM starwars.character u;
------------