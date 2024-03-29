/**
 * Copyright (C) 2020 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.akehurst.language.editor.information.examples

import net.akehurst.language.editor.information.Example

object English {
    val id = "english"
    val label = "English (Natural Language)"
    /*
    val sentence = """
In the beginning was the Word, and the Word was with God, and the Word was God.
He was with God in the beginning.
Through him all things were made; without him nothing was made that has been made.
In him was life, and that life was the light of all mankind.
The light shines in the darkness, and the darkness has not overcome it.
    """.trimIndent()
     */
    val sentence = """
I am.
John should not be in America.
Who are they?        
    """.trimIndent()
    val grammar = """
// based on [https://www.scientificpsychic.com/grammar/enggram1.html]

namespace net.akehurst.language.natural

grammar English {

    skip WHITESPACE = "\s+" ;

    text = sentence* ;

    sentence
        = simple-sentence
        || compound-sentence
        ;

    simple-sentence
        = declarative-sentence '.'
        || interrogative-sentence '?'
        || imperative-sentence '.'
        || conditional-sentence '.'
        ;

    compound-sentence
        = simple-sentence CONJUNCTION simple-sentence
        || 'Either' declarative-sentence 'or' declarative-sentence
        || 'Either' imperative-sentence 'or' imperative-sentence
        ;

    interrogative-sentence
        = declarative-sentence
        || 'Who' predicate
        || ('What' | 'Which') (ADVERB* adjective)? noun predicate
        || ('What' | 'When' | 'Where' | 'Who' | 'To whom' | 'Why')?  ('do' |'does' |'don\'t' |'doesn\'t' |'did' |'didn\'t') subject predicate
        || 'Which'  noun-phrase? ('do' |'does' |'don\'t' |'doesn\'t' |'did' |'didn\'t') subject predicate
        || ('What' |'Which' |'When' |'Where' |'Who' |'To whom' | 'Why')? ('will' |'won\'t') subject predicate
        || ('What' |'Which' |'When' |'Where' |'Who' |'To whom' | 'Why')? ('has' |'have' |'hasn\'t' |'haven\'t') subject predicate2
        || ('What' |'Which' |'When' |'Where' |'Who' |'To whom' | 'Why')? ('are' |'is' |'was' |'were' |  'aren\'t' |'isn\'t' |'wasn\'t' |'weren\'t') subject (ADVERB* adjective | prepositional-phrase* | predicate3)?
        ;

    imperative-sentence =  imperative-predicate ;
    imperative-predicate = imperative-verb complement ;
    imperative-verb = VERB_INFINITIVE ;

    conditional-sentence
        = dependent-clause ',' independent-clause
        || independent-clause dependent-clause
        ;
    dependent-clause = ('if' | 'when') declarative-sentence ;
    independent-clause
        = declarative-sentence
        || interrogative-sentence
        ;

    declarative-sentence = subject predicate ;

    subject = simple-subject | compound-subject ;

    simple-subject = noun-phrase | NOMINATIVE_PERSONAL_PRONOUN ;

    noun-phrase
        = 'the' SPECIFIC_PROPER_NOUN
        || PROPER_NOUN
        || NON_PERSONAL_PRONOUN
        || ARTICLE (ADVERB* compound-adjective)? compound-noun
        || (ADVERB* compound-adjective)? COMMON_NOUN_PLURAL
        || PROPER_NOUN_POSSESSIVE (ADVERB* compound-adjective)? compound-noun
        || PERSONAL_PRONOUN_POSSESSIVE_ADJECTIVE (ADVERB* compound-adjective)? compound-noun
        || ARTICLE COMMON_NOUN_POSSESSIVE  (ADVERB* compound-adjective)? compound-noun
        ;

    compound-noun = noun prepositional-phrase* ;
    prepositional-phrase = PREPOSITION object ;

    compound-adjective = [adjective / ('and' | 'or') ]+ ;

    compound-subject
        = simple-subject ('and' | 'or') simple-subject
        || 'Either' simple-subject 'or' simple-subject
        || 'Neither' simple-subject 'nor' simple-subject
        ;

    predicate = (verb | verb-phrase) complement? ;

    verb = VERB_1s_PRESENT
         || VERB_2s_PRESENT
         || VERB_3s_PRESENT
         || VERB_1p_PRESENT
         || VERB_2p_PRESENT
         || VERB_3p_PRESENT
         || VERB_1s_PAST
         || VERB_2s_PAST
         || VERB_3s_PAST
         || VERB_1p_PAST
         || VERB_2p_PAST
         || VERB_3p_PAST
         || LINKING_VERB
         ;

    complement
        =  object? object     // indirect-object? object
        ||  ADVERB* adjective
        ||  prepositional-phrase*
        ||  'to' VERB_INFINITIVE object?
        ||  VERB_PRESENT_PARTICIPLE
        ;

    object =
       simple-object | compound-object ;


    simple-object
        = noun-phrase
        || OBJECTIVE_PERSONAL_PRONOUN
        ;

    compound-object = simple-object ('and' | 'or') simple-object ;

    verbal-phrase
        = 'to' VERB_INFINITIVE complement?
        || VERB_PRESENT_PARTICIPLE complement?
        || VERB_PAST_PARTICIPLE complement?
        ;

    predicate1 = verb1 complement? ;
    verb1
        = 'have' VERB_PAST_PARTICIPLE
        || 'have' 'been' VERB_PRESENT_PARTICIPLE
        || 'be' VERB_PRESENT_PARTICIPLE
        || VERB_INFINITIVE
        ;

    predicate2 = verb2 complement? ;
    verb2 = VERB_PAST_PARTICIPLE ;

    predicate3 = verb3 complement? ;
    verb3 = VERB_PRESENT_PARTICIPLE | VERB_PAST_PARTICIPLE ;

    verb-phrase
        = ('had' | 'have' | 'has') 'not'? VERB_PAST_PARTICIPLE
        || ('had' | 'have' | 'has') 'not'? 'been' (VERB_PAST_PARTICIPLE | VERB_PRESENT_PARTICIPLE)?
        || AUX_VERB 'not'? 'have' VERB_PAST_PARTICIPLE
        || AUX_VERB 'not'? 'have' 'been' (VERB_PAST_PARTICIPLE | VERB_PRESENT_PARTICIPLE)?
        || AUX_VERB 'not'? 'be' (VERB_PAST_PARTICIPLE | VERB_PRESENT_PARTICIPLE)?
        || AUX_VERB 'not'? VERB_INFINITIVE
        || 'ought' ('to' | 'not') VERB_INFINITIVE
        || 'ought' ('to' | 'not') 'be' (VERB_PAST_PARTICIPLE | VERB_PRESENT_PARTICIPLE)?
        || 'ought' ('to' | 'not') 'have' VERB_PAST_PARTICIPLE
        || 'ought' ('to' | 'not') 'have' 'been' (VERB_PAST_PARTICIPLE | VERB_PRESENT_PARTICIPLE)?
        || ('do' |'does' |'did') 'not'? VERB_INFINITIVE?
        || ('am' |'are' |'is' |'was' |'were') 'not'? (VERB_PAST_PARTICIPLE | VERB_PRESENT_PARTICIPLE)?
        || ('am' |'are' |'is' |'was' |'were') 'not'? 'being' VERB_PAST_PARTICIPLE?
        || ('am' |'are' |'is' |'was' |'were') 'not'? 'going' 'to' VERB_INFINITIVE?
        ;

    adjective
        = ADJECTIVE_POSITIVE
        || ADJECTIVE_COMPARATIVE
        || ADJECTIVE_SUPERLATIVE
        ;

    //TODO: need to enable some kind of function to get these words!
    noun = noun-singular | noun-plural | noun-possessive ;
    noun-singular = COMMON_NOUN | PROPER_NOUN ;
    noun-plural = COMMON_NOUN_PLURAL | PROPER_NOUN_PLURAL ;
    noun-possessive = COMMON_NOUN_POSSESSIVE | PROPER_NOUN_POSSESSIVE ;


    //TODO: these should come from a dictionary not be listed here
    PROPER_NOUN = 'John' | 'America' | 'Dr. Allen' | 'State Street' | 'Mary' ;
    PROPER_NOUN_PLURAL = 'Johns' | 'Americas' ;
    PROPER_NOUN_POSSESSIVE = 'John\'s' | 'America\'s' | 'Dr. Allen\'s' ;
    SPECIFIC_PROPER_NOUN = 'Atlantic Ocean' | 'Sahara' ;

    COMMON_NOUN = 'mouse' | 'cat' | 'book' | 'tree' | 'name' ;
    COMMON_NOUN_PLURAL = 'cats' | 'books' | 'trees' | 'sentences' | 'statements' ;
    COMMON_NOUN_POSSESSIVE = 'cat\'s' | 'book\'s' | 'tree\'s' ;


    //TODO: these should come from a dictionary not be listed here
    ADJECTIVE_POSITIVE = 'new' | 'yellow' | 'green' | 'red' | 'blue' | 'pretty' | 'useful' | 'declarative' ;
    ADJECTIVE_COMPARATIVE = 'newer' | 'yellower' | 'prettier' ;
    ADJECTIVE_SUPERLATIVE = 'newest' | 'yellowest' | 'prettiest' ;
    ARTICLE = 'a' | 'an' | 'the' ; //TODO: definite or indefinite
    ADVERB = 'very' | 'extremely' | 'carefully' | 'gracefully' ;
    CONJUNCTION = 'and' | 'or' | 'but' ;
    SUBORDINATE_CONJUNCTION = 'where' | 'when' | 'while' | 'because' | 'if' | 'unless' ; //TODO: where is this used?
    INTERJECTION = 'Oh!' | 'Aha!' ;
    PREPOSITION =  'in' | 'about' | 'toward' | 'from' | 'over' | 'under' | 'at' | 'below' ;

    LINKING_VERB = 'be' | 'am' | 'are' | 'is' | 'was' | 'were' | 'look' | 'looks' | 'looked' | 'become' | 'became' | 'become' ;

    //TODO: these should come from a dictionary not be listed here
    VERB_INFINITIVE         = 'be'    | 'start'    | 'form' ;
    VERB_PRESENT_PARTICIPLE = 'being' | 'starting' ;
    VERB_PAST_PARTICIPLE    = 'been'  | 'started'  ;
    VERB_1s_PRESENT         = 'am'    | 'start'    ;
    VERB_2s_PRESENT         = 'are'   | 'start'    ;
    VERB_3s_PRESENT         = 'is'    | 'starts'   ;
    VERB_1p_PRESENT         = 'are'   | 'start'    ;
    VERB_2p_PRESENT         = 'are'   | 'start'    ;
    VERB_3p_PRESENT         = 'are'   | 'start'    ;
    VERB_1s_PAST            = 'was'   | 'started'  ;
    VERB_2s_PAST            = 'were'  | 'started'  ;
    VERB_3s_PAST            = 'was'   | 'started'  ;
    VERB_1p_PAST            = 'were'  | 'started'  ;
    VERB_2p_PAST            = 'were'  | 'started'  ;
    VERB_3p_PAST            = 'were'  | 'started' | 'used' ;


    NOMINATIVE_PERSONAL_PRONOUN = 'I' | 'you' | 'He' | 'he' | 'she' | 'it' | 'we' | 'they' ;
    NON_PERSONAL_PRONOUN = 'someone' | 'anyone' | 'this' ;
    DEMONSTRATIVE_PRONOUN = 'this' | 'that' | 'such' ;
    INTERROGATIVE_PRONOUN = 'who' | 'which' ;
    RELATIVE_PRONOUN = 'who' | 'which' ;
    INDEFINITE_PRONOUN = 'each' | 'either' | 'some' | 'any' | 'many' | 'few' | 'all' ;
    OBJECTIVE_PERSONAL_PRONOUN = 'me' |'you' | 'him' | 'her' | 'us' | 'you' | 'them' ;
    PERSONAL_PRONOUN_POSSESSIVE_ADJECTIVE = 'my' | 'your' | 'his' | 'her' | 'our' | 'their' ;
    PERSONAL_PRONOUN_POSSESSIVE = 'mine' | 'yours' | 'his' | 'hers' | 'ours' | 'theirs' ;

    AUX_VERB = 'must' | 'may' | 'might' | 'will' | 'would' | 'shall' | 'should' | 'can' | 'could' ;

}
    """.trimIndent()
    val style = """

    """.trimIndent()
    val format = """
        
    """.trimIndent()

    val example = Example(id, label, sentence, grammar, style, format)

}