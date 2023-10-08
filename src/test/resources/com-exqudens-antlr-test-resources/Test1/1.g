grammar Exqudens;

// Parser Rules

process : TEXT_0 identifier_0 TEXT_1 or_0 TEXT_1 repeat_0+ eof_0 ;
identifier_0 : identifier ;
or_0 : ( area_0 | area_1 ) ;
area_0 : TEXT_2 ;
area_1 : TEXT_3 ;
repeat_0 : repeat_1+ TEXT_1 ;
repeat_1 : spaces_0 identifier_1 spaces_1 TEXT_4 numbers_0 optional_0? ;
spaces_0 : spaces ;
identifier_1 : identifier ;
spaces_1 : spaces ;
numbers_0 : numbers ;
optional_0 : TEXT_5 ;
eof_0 : eof ;

russian_letter : ( RUSSIAN_LOWER_LETTER | RUSSIAN_UPPER_LETTER ) ;
russian_letters : russian_letter+ ;
english_letter : ( ENGLISH_LOWER_LETTER | ENGLISH_UPPER_LETTER ) ;
english_letters : english_letter+ ;
lower_letter : ( RUSSIAN_LOWER_LETTER | ENGLISH_LOWER_LETTER ) ;
lower_letters : lower_letter+ ;
upper_letter : ( RUSSIAN_UPPER_LETTER | ENGLISH_UPPER_LETTER ) ;
upper_letters : upper_letter+ ;
letter : ( lower_letter | upper_letter ) ;
letters : letter+ ;
number : NUMBER ;
numbers : number+ ;
space : SPACE ;
spaces : space+ ;
new_line : NEW_LINE ;
new_lines : new_line+ ;
tab : TAB ;
tabs : tab+ ;
eof : EOF ;
word : letter+ ;
identifier : ( letter | NUMBER | DASH | UNDER_LINE )+ ;

// Lexer Rules

TEXT_0 : 'OrderNumber< ' ;
TEXT_1 : '\n' ;
TEXT_2 : '  Items:' ;
TEXT_3 : '  items:' ;
TEXT_4 : '$' ;
TEXT_5 : 'zzz' ;

RUSSIAN_LOWER_LETTER : [\u0451\u0430-\u044F] ;
RUSSIAN_UPPER_LETTER : [\u0401\u0410-\u042F] ;
ENGLISH_LOWER_LETTER : [a-z] ;
ENGLISH_UPPER_LETTER : [A-Z] ;
NUMBER : [0-9] ;
DASH : [-] ;
UNDER_LINE : [_] ;
SPACE : [ ] ;
DOT : [.] ;
COMMA : [,] ;
COLON : [:] ;
SEMICOLON : [;] ;
NEW_LINE : [\n] ;
TAB : [\t] ;
RETURN : [\r] ;