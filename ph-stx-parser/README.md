# ph-xpath2
The attempt to create a pure Java XPath2 parser and interpreter.

Current status: grammar looks good. Currently working on the transition from syntax nodes to a domain model.

#Maven usage
No Maven release of this artifact was published so far, so you need to use the source version. 

# Grammar
The grammar is defined for JavaCC and resides in `src/main/jjtree/ParserXP2.jjt`.
The Java code generation of the grammar happens with the [ph-javacc-maven-plugin](https://github.com/phax/ph-javacc-maven-plugin) Maven plugin.

# Building from source
Just run `mvn clean install` to get the current version build.
No external Maven repositories are needed.

---

My personal [Coding Styleguide](https://github.com/phax/meta/blob/master/CodeingStyleguide.md) |
On Twitter: <a href="https://twitter.com/philiphelger">@philiphelger</a>
