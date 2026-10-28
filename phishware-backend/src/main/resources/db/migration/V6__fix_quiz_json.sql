-- V6: tolerancia a correct_answers legacy en formato array '["B"]' -> '{"correct": ["B"]}'
UPDATE quiz_questions
SET correct_answers = jsonb_build_object('correct', correct_answers)
WHERE jsonb_typeof(correct_answers) = 'array';

-- Normaliza correct_answers tipo texto simple '"B"' -> '{"correct": ["B"]}'
UPDATE quiz_questions
SET correct_answers = jsonb_build_object('correct', jsonb_build_array(correct_answers #>> '{}'))
WHERE jsonb_typeof(correct_answers) = 'string';
