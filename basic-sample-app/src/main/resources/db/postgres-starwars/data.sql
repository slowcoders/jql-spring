INSERT INTO starwars.episode (title) values
('NEWHOPE'), ('EMPIRE'), ('JEDI')
on conflict DO NOTHING;


INSERT INTO starwars.character (species, id, name, home_planet, height, mass, primary_function) values
('Human', 1000, 'Luke Skywalker', 'Tatooine', 1.72, 77, null),
('Human', 1001, 'Darth Vader', 'Tatooine', 2.02, 136, null),
('Human', 1002, 'Han Solo', null, 1.8, 80, null),
('Human', 1003, 'Leia Organa', 'Alderaan', 1.5, 49, null),
('Human', 1004, 'Wilhuff Tarkin', null, 1.8, null, null),
('Human', 1005, 'Extra-1', null, 1.19, 50, null),
('Human', 1006, 'Extra-2', null, 1.8, 121, null),
('Droid', 2000, 'C-3PO', null, 1.71, 75, 'protocol'),
('Droid', 2001, 'R2-D2', null, 1.09, 32, 'Astromech')
on conflict DO NOTHING;


INSERT INTO starwars.starship (id, name, length, pilot_id) values
(3000, 'Millenium Falcon', 34.37, 1002),
(3001, 'X-Wing', 12.5, 1000),
(3002, 'TIE Advanced x1', 9.2, 1001),
(3003, 'Imperial shuttle', 20, NULL)
on conflict DO NOTHING;


INSERT INTO starwars.character_episode_link (character_id, episode_id) values
(1000, 'NEWHOPE'),
(1000, 'EMPIRE'),
(1000, 'JEDI'),
(1001, 'NEWHOPE'),
(1001, 'EMPIRE'),
(1001, 'JEDI'),
(1002, 'NEWHOPE'),
(1002, 'EMPIRE'),
(1002, 'JEDI'),
(1003, 'NEWHOPE'),
(1003, 'EMPIRE'),
(1003, 'JEDI'),
(1004, 'NEWHOPE'),
(2000, 'NEWHOPE'),
(2000, 'EMPIRE'),
(2000, 'JEDI'),
(2001, 'NEWHOPE'),
(2001, 'EMPIRE'),
(2001, 'JEDI')
on conflict DO NOTHING;


INSERT INTO starwars.character_friend_link (character_id, friend_id)
values
(1000, 1002),
(1000, 1003),
(1000, 2000),
(1000, 2001),

(1001, 1004),

(1002, 1000),
(1002, 1003),
(1002, 2001),

(1003, 1000),
(1003, 1002),
(1000, 2000),
(1000, 2001),

(1004, 1001),

(2000, 1000),
(2000, 1002),
(2000, 1003),
(2000, 2001),

(2001, 1000),
(2001, 1002),
(2001, 1003)
on conflict DO NOTHING;