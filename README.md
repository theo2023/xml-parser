# XML Parser

I began by researching XML syntax and learning the differences between elements and attributes because I knew I would have to model them in my code. An [online XML validator](https://codebeautify.org/xmlvalidator) and [viewer](https://codebeautify.org/xmlviewer) proved extremely useful for seeing which inputs were valid/invalid (since I was new to working with XML) and for helping me come up with edge cases to eventually test. It made sense to model an element as its own class with its name, attributes as a hash map (so that I could associate the attribute name with its value), and data as fields. From my experimentation with which XML was valid, I noticed that attributes "belonged" to elements and couldn't exist by themselves, which is why I modeled them as a field instead of another class.

## Brainstorming and Initial Approaches

My brainstorming included thinking about how I would process each part of the input string while maintaining the invariant that my parser had to be forward-only. I originally considered using regular expressions to pattern-match based on the `<` and `>` characters, and using a stack felt natural to model the content in between the opening and closing tags—I would be finished parsing an element if I encountered its closing tag and thus could pop its opening tag off the stack. The regular expressions idea led me to the `String split()` method, which separates the input into an array of substrings according to a given regular expression. Using a linked list seemed to work well with this because I sought to retrieve the next string to parse in constant time by pulling from the front of the list. I explored this for a while but then realized that if `split()` goes through the entire input to produce the array, I would basically be traversing the input again by working with that array's contents, violating the forward-only requirement. So I decided to traverse the input by moving a pointer and going character-by-character instead.

## Interactivity

I proceeded with developing a working prototype through TDD, keeping in mind all the different locations whitespace can appear in XML (this influenced how I wrote my early tests) along the way. It made the most sense to me to complete the parsing of an element every time I popped the stack, so that's what I did. Also, since the specifications stated that information about the current element or attribute had to be output/accessible, I decided to make the parser interactive. Otherwise, I thought it would be difficult for the user to retrieve the desired data if the entire input was parsed at once.
In the spirit of separation of concerns, I wanted to keep the interactive logic/print statements in the Runner class separate from the Parser class as much as possible. I figured the Parser class shouldn't need to know anything about how it's being used in the Runner class.

## Assumptions
- The input is a single string containing valid XML
- The input string is one continuous line (due to running into problems with the reading of whitespace escape sequences)
- How I implemented parsing the XML escaped entities does not explicitly violate the forward-only requirement

## Final Reflections 

During the project, I definitely was most motivated by writing failing tests and working to get them to pass, as well as debugging the interactive portion of the parser. The most difficult and frustrating parts of the experience were having to handle obscure XML formatting edge cases, getting a decent enough prototype done/having to make a lot of decisions about how to write it from scratch, and having to abandon ideas that I thought would work and to which I dedicated a lot of time and effort.

If I had more time, I would have nested the console output of the interactive parser with a number of tabs/spaces proportional to which depth level the parser was at, and I would have built an object tree representing the XML document as a data structure.

In total, it took me roughly 20 hours, including brainstorming, coding, testing, and debugging.
