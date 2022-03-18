INSERT INTO profiles (profile_id, preferences, bio, phone_number, dob, first_release)
VALUES  (1, 'My preferences for 1', 'My Bio for 1', '07879 899101', '1979-01-01', '1979-01-01'),
        (2, 'My preferences for 2', 'My Bio for 2', '07879 899102', '1973-06-12', '1979-01-01'),
        (3, 'My preferences for 3', 'My Bio for 3', '07879 899103', '1976-11-21', '1979-01-01'),
        (4, 'My preferences for 4', 'My Bio for 4', '07879 899104', '1979-04-12', '1976-01-01'),
        (5, 'My preferences for 5', 'My Bio for 5', '07879 899105', '1972-06-19', '1979-01-01'),
        (6, 'My preferences for 6', 'My Bio for 6', '07879 899106', '1974-01-01', '1972-01-01');

INSERT INTO artists (artist_id, profile_id, performance_name)
VALUES  (1, 1, 'Beautiful South'),
        (2, 2, 'Oasis'),
        (3, 3, 'Pink'),
        (4, 4, 'Adele'),
        (5, 5, 'Beach Boys'),
        (6, 6, 'Queen');

INSERT INTO concerts (concert_id, concert_name) VALUES
    (1, 'Live Aid 2020'),
    (2, 'Live Aid 2021');

INSERT INTO concert_artists (concert_id, artist_id) VALUES
    (1, 1),
    (1, 2),
    (2, 1),
    (2, 2),
    (2, 3),
    (2, 4),
    (2, 5),
    (2, 6);
    
INSERT INTO records (record_id, artist_id, record_name) VALUES
(1, 1, 'Song for Whoever'),
(2, 1, 'You Keep It All In'),
(3, 1, 'A Little Time'),
(4, 1, 'My Book'),
(5, 1, 'Let Love Speak Up Itself'),
(6, 1, 'Old Red Eyes Is Back'),
(7, 1, 'We Are Each Other'),
(8, 1, 'Bell Bottomed Tear'),
(9, 1, '36D'),
(10,2, 'Champagne Supernova'),
(11,2, 'Whatever'),
(12,2, 'Morning Glory'),
(13,2, 'Live Foreve'),
(14,2, 'Wonderwall'),
(15,2, 'Cast No Shadow'),
(16,3, 'Try'),
(17,3, 'So What'),
(18,4, 'Hello'),
(19,4, 'Easy on Me'),
(20,4, 'Set Fire To The Rain'),
(21,4, 'Someone Like You'),
(22,5, 'I Get Around'),
(23,5, 'Sloop John B'),
(24,5, 'Help Me Rhonda'),
(25,6, 'We Will Rock You'),
(26,6, 'Bohemian Rhapsody'),
(27,6, 'Killer Queen'),
(28,6, 'Bicycle Race'),
(29,6, 'Fat Bottomed Girls'),
(30,6, 'Crazy Little Thing Called Love');