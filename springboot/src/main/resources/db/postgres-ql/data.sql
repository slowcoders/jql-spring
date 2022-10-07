INSERT INTO episode (title) values
('NEWHOPE'), ('EMPIRE'), ('JEDI')
on conflict DO NOTHING;


INSERT INTO character (dtype, id, name, home_planet, height, mass) values
('Human', 1000, 'Luke Skywalker', 'Tatooine', 1.72, 77),
('Human', 1001, 'Darth Vader', 'Tatooine', 2.02, 136),
('Human', 1002, 'Han Solo', null, 1.8, 80),
('Human', 1003, 'Leia Organa', 'Alderaan', 1.5, 49),
('Human', 1004, 'Wilhuff Tarkin', null, 1.8, null)
on conflict DO NOTHING;


INSERT INTO character (dtype, id, name, primary_function) values
('Droid', 2000, 'C-3PO', 'protocol'),
('Droid', 2001, 'R2-D2', 'Astromech')
on conflict DO NOTHING;


INSERT INTO starship (id, name, length) values
(3000, 'Millenium Falcon', 34.37),
(3001, 'X-Wing', 12.5),
(3002, 'TIE Advanced x1', 9.2),
(3003, 'Imperial shuttle', 20)
on conflict DO NOTHING;


INSERT INTO human_starship_link (human_id, starship_id) values
(1000, 3001),
(1000, 3003),

(1001, 3002),

(1002, 3000),
(1002, 3003)
on conflict DO NOTHING;


INSERT INTO character_episode_link (character_id, episode_id) values
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


INSERT INTO character_friend_link (character_id, friend_id)
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