# ph-stx
Java STX implementation based on Joost 0.9.1 from http://joost.sourceforge.net/
Licensed under Mozilla Public License 1.1

# Abandoned because of lack of time
(and because I heard that XSLT 3 has focus on streaming support)

If you want to take it over, just drop me a note. Find my email address in the pom.xml.

# Changes so far
* Using Maven to build
* Integrated jflex and java_cup for consistency
* Upgraded to JUnit 4
* Changed from commons-logging to SLF4J
* Changed from log4j to SLF4J
* Removed commons-discovery in favour of ServiceLoader
* Removed bsf - will use the Java scripting API instead
* Started a JavaCC based parser
* Using generics where applicable

#Todos:
* Remove unnecessary dependencies
* Changed to a more convenient grammar
* Modernize
* Use scripting API
---

My personal [Coding Styleguide](https://github.com/phax/meta/blob/master/CodeingStyleguide.md) |
On Twitter: <a href="https://twitter.com/philiphelger">@philiphelger</a>
