# ph-stx
Java STX implementation based on Joost 0.9.1 from http://joost.sourceforge.net/
Licensed under Mozilla Public License 1.1

Changes so far:
* Using Maven to build
* Integrated jflex and java_cup for consistency
* Upgraded to JUnit 4
* Changed from commons-logging to SLF4J
* Removed commons-discovery in favour of ServiceLoader
* Removed bsf - will use the Java scripting API instead
* Started a JavaCC based parser

Todos:
* Remove unnecessary dependencies
* Changed to a more convenient grammar
* Modernize
