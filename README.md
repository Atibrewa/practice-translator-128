Comp 128 - HW2: Multilingual entity extraction
===
In this homework assignment you will develop a program that analyzes text in another language. Your program will:

- Detect the language of a passage of text.
- Identify concepts in the text associated with Wikipedia articles.
- Translate the concepts (if possible) to English.

Here it is in action for the [Scots language](https://en.wikipedia.org/wiki/Scots_language):

```
Kofi Annan (born 8 Aprile 1938) is a Ghanaian diplomat who served as the seivent Secretary-General o the Unitit Naitions frae 1 Januar 1997 tae 31 December 2006. 
Annan an the Unitit Naitions wur the co-recipients o the 2001 Nobel Peace Prize for his foondin the Global AIDS and Health Fund for tae support developin kintras in their fecht tae care for their fowk.

Translating text from Scots to English found entites:
	'Kofi Annan' => 'Kofi Annan'
	'8 Aprile' => 'April 8'
	'Unitit Naitions' => 'United Nations'
	'1 Januar' => 'January 1'
	'1997' => '1997'
	'31 December' => 'December 31'
	'Unitit Naitions' => 'United Nations'
	'2001' => '2001'
	'Nobel Peace Prize' => 'Nobel Peace Prize'
	'Kintra' => 'State'
```

And for Hindi:
```
कोफ़ी अन्नान(जन्म: 8 अप्रैल 1938) एक घानाई कूटनीतिज्ञ हैं। वे 1962 से 1974 तक, और 1974 से 2006 तक संयुक्त राष्ट्र में कार्यरत रहे। वे 1 जनवरी 1997 से 31 दिसम्बर 2006 तक दो कार्यकालों के लिये संयुक्त राष्ट्र के महासचिव रहे। उन्हें संयुक्त राष्ट्र के साथ 2001 में नोबेल शांति पुरस्कार से सह-पुरस्कृत किया गया।

translating text from Hindi to English found entites:
	'८ अप्रैल' => 'April 8'
	'राजनय' => 'Diplomacy'
	'१९६२' => '1962'
	'१९७४' => '1974'
	'१९७४' => '1974'
	'२००६' => '2006'
	'संयुक्त राष्ट्र' => 'United Nations'
	'१ जनवरी' => 'January 1'
	'१९९७' => '1997'
	'दिसम्बर' => 'December'
	'२००६' => '2006'
	'संयुक्त राष्ट्र' => 'United Nations'
	'संयुक्त राष्ट्र' => 'United Nations'
	'२००१' => '2001'
	'नोबेल शांति पुरस्कार' => uknown
```

Along the way, you'll get a chance to practice with Java's Maps. Otherwise known as HashMaps, hashtables, or dictionaries.
 
### Part 1: Language detector

For your first task, you'll write a class that detects the language of a text. 

In a nutshell, the Language detector generates a score that indicates how common the words in a text are in that langauge.
For example, assume you're scoring the text "Aw human sowels is born free" against the language Scots.
You will sample 500 random Scots Wikipedia articles and find:
 *  "Aw" has count 12
 *  "human" has count 19
 *  "is" has count 2408
 *  "born" has count 67
 *  "free" has count 11
 
Thus, the total score for Scots is (12 + 19 + 2408 + 67 + 11) = 2517. 
Your program will repeat this computation in all languages. It should then normalize the scores by 
 dividing the total scores by the number of words (including repeated words) found for each language. Finally it chooses the language with the highest normalized score as the detected language.

Take a look at LanguageDetector.java. 
You'll complete your work for part 1 in this class.
I have declared the main methods for you: `train()`, `detect()`, and the `main()` method, but they don't do anything useful.

A LanguageDetector must be trained once to identify words in each language. 
The train method essentially "precomputes" the counts for each word in each language to speed up language detection.

`train()`: The train method needs to do the following:

* **Hint: The Utils class has some helpful constants and a method to split words. You should also explore the methods in the WikipediaProvider class**
* For each language:
    * Extract the page text from 500 random pages.
    * Split each page text into words (look for a helper method in Utils).
    * Count how many times every unique word occurs across all 500 pages.

You'll need to create instance variables to capture the data (counts for each word for each language and the total number of words read for each language).

`detect(text)`: Given a particular text, the detect method does the following: 
* Split the text into words.
* Retrieve the number of times each word occurs in the 500 random pages for each language from the training data.
* Sum up those counts for each language and normalize them.

Choose the language with the highest count. 

Test your program by Googling texts in each language. As you are testing, it will speed up your program to use fewer random pages, although it will also make it more likely that the language is not detected.
Your program should do a pretty good job, but it may get a few languages confused - for example Scots and English which contain many duplicate words.

### Part 2: Entity extractor

I have provided you with an empty entity extractor class. 
For the second portion of the homework, you'll need to complete the entity extractor class and the main method that uses it.

**A. Implement a simple extract:** Your `extract()` method should first detect the language of the text using its language detector.
Next, split your text into words and check each word to see if it is the title of a Wikipedia article in the source langauge.
If a word does correspond to an article, try to find the english equivalent of the article (this isn't always possible).
Format your results similarly to the output you see above. Make sure that you use good method decomposition.

If a word does not correspond to a wikipedia page in the detected source language, you can simply ignore it and not print anything. Optionally, (but not required) you could search for the original word in the English wikipedia. If it exists, you can print it as the "translation". This will improve your results somewhat, especially when translating from scots to english or dealing with dates/numbers. The examples show above, do this.
You do not need to worry about capitalization (although handling this would probably improve your translation results). Just search for the pages using the existing capitalization in the text.

As usual when implementing methods, make sure you use good method decomposition. For guidelines on method decomposition, refer to our java style guide (linked on moodle). Most importantly, each method should have a single clear purpose. The description of the extract method above is doing multiple things (e.g. splitting up words, checking if a page exists in the source language, finding corresponding pages in English, etc.). You should think about how to split this into multiple methods (with clear descriptive names).

**B. Implement main:** Complete the main method of your program.
You can model your work on the LanguageDetector's main method.
Create the components necessary for an entity extractor and then create the entity extractor itself.
Repeatedly ask the user for a text and then extract entities in the text.
You should now be able to test your program, but it will only extract single word concepts.

**C. Optional Extras: Implement a fancy extract:**
This part is not required, but it's a fun little exercise that will improve your translation results.

You'll improve the performance of your algorithm by looking for more *specific* concepts that span more than one word (e.g. `Barack Obama`).
To do this, you'll need to understand the concept of [n-grams](http://en.wikipedia.org/wiki/N-gram) to complete this task. 
An n-gram is simply a series of n words that occur consecutively in a text. For example, given the text:
```
Macalester is committed to being a preeminent liberal arts college
```
* The 1-grams (i.e. unigrams) would be the individual words: "Macalester", "is", "committed", ....
* The 2-grams (i.e. bigrams) would be consecutive pairs of words: "Macalester is", "is committed", "commited to" ...
* The 3-grams (i.e. trigrams) would be consecutive triples of words: "Macalester is committed", "is committed to", ...

Adjust your program so that it looks for trigrams, bigrams, and unigrams.
Your program should prefer trigrams to bigrams and bigrams to unigrams. 
More specifically, if a word is the start of a trigram, it should not be used as the start of a bigram or unigram.
For example, if your sentence is "Macalester is a liberal arts college", you would find the following Tri/Bi/Unigrams:
* Macalester is a, Macalester is, Macalester
* is a liberal, is a, is
* a liberal arts, a liberal, a
* liberal arts college, liberal arts, liberal
* arts college, arts
* college
In this example, you would give preference to the starting trigram for each bullet point if it is found as an article title.


#### Attribution
This assignment was originally developed by Shilad Sen using the WikiBrain library. It was updated
by Bret Jackson to use the Wikipedia API instead to work around dependency issues with Wikibrain and Java 11.