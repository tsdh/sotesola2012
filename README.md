# Tassilo's SoTeSoLa 2012 Hackathon Contribution

## Problems and Solutions

This SoTeSoLa hackathon contribution deals with two reverse engineering
problems:

  1. metrics
  2. architecture recovery

It tackles any 101 implementation that contains at least one java file.  Every
such implementation is parsed to a java syntax graph by using
[EMFText](http://www.emftext.org/index.php/EMFText)'s
[JaMoPPC](http://www.jamopp.org) parser.  Thus, abstract java syntax graph are
represented as EMF models.  The largest model, the one for the swing
implementation, contains 115520 objects and 134479 references.

To solve the two problems stated above, the
[FunnyQT library](https://github.com/jgralab/funnyqt) is used.  FunnyQT is a
model querying and transformation library for TGraphs and EMF models written in
Clojure.

### Kemerer and Chidamber Complexity Metrics

For every class in each syntax graph, all six **Kemerer and Chidamber
complexity metrics** are calculated.  Those metrics are:

1. *Depth Of Inheritance Tree (DIT)*: Computes the maximum number of
   superclasses (or super-interfaces) until reaching `java.lang.Object`.  This
   metric is also computed for interfaces here.

2. *Coupling Between Objects (CBO)*: Computes the number of classes coupled to
   a given class.  A class C is coupled to a given class G if G accesses fields
   of C or calls methods of C.

3. *Weighted Methods Per Class (WMC)*: Computes the sum of all method
   complexities of a class.  As method complexity metric I use the cyclomatic
   complexity.

4. *Number Of Children (NOC)*: Computes the number of immediate subclasses of a
   given class.  This metric is also computed for interfaces here, where the
   value is the number of immediate sub-interfaces.

5. *Response For A Class (RFC)*: Computes the number of methods that might be
   called in response to a received message, that is, the number of all methods
   defined in that class, all inherited methods, plus all methods that are
   called by own or inherited methods.

6. *Lack Of Cohesion In Object Methods (LCOM)*: Pair-wise checks if two methods
   of a given class access disjoint sets of own fields.  The metric value is
   the number of disjoint-field pairs minus the number method pairs accessing
   at least one common field.  If there are more common-field pairs than
   disjoint-field pairs, the metric value is 0.

All metric values are aggregated over all 101 implementations, and so you'll
also get a ranking of the 10 most complex classes in the complete 101 corpus
concerning each metric.

### Simple UML Class Diagrams

Furthermore, for each 101 implementation, a **UML-alike diagram** is generated
showing all classes and interfaces including generalization, implementation,
and usage relationships.  Here, a classifier uses another classifier if it has
a field of the other's type or if it has a method whose return type is the
other classifier.

See below how to run that stuff.

## Prerequisites

You only need the [Leiningen](http://leiningen.org) build management tool, and
it'll take care of every other dependency needed.  Simply download the `lein`
script [here](https://raw.github.com/technomancy/leiningen/preview/bin/lein),
make it executable, and place it somewhere on your `PATH`.

Since the metrics calculation uses the ForkJoin framework for parallelizing the
computation, you need a JDK7 (or newer).

For the diagram generation, you also need the
[GraphViz](http://www.graphviz.org/) program `dot` somewhere on your `PATH`.

Finally, the parser invocation script `build-jamopp-graphs.sh` is a BASH
script, so it'll only run on unices or windows with cygwin.

## Usage

Parse every 101companies implementation that contains at least one java file
into a java graph using EMFText's [JaMoPPC](http://www.jamopp.org) parser
(included in `lib/`).  That's the job of the `build-jamopp-graphs.sh` bash
script.  If an implementation needs to be built beforehand using ant or make,
this script will do that for you.  However, some implementations encode
absolute paths in their build scripts or rely on external plugins or libraries
which may or may not be installed.  Thus, for some of them building might
fail...

Thereafter, the folder `models/` contains one xmi file per 101 implementation
containing java files.  You'll get an error for some projects, e.g., all AntLR
implementation, because the generated AntLR parsers contain characters that are
not valid in XML and thus the XMI serialization fails.  There might be more
errors, but at least I can parse 46 implementations here...

Do it like so:

```
$ pwd
/path/to/sotesola2012
$ cd scripts/
$ ./build-jamopp-graphs.sh ~/path/to/101repo/contributions
Running make in antlrAcceptor...done!
Building JaMoPP model for antlrAcceptor... failed!
Running make in antlrLexer...done!
Building JaMoPP model for antlrLexer... failed!
...
Running ant in jgralab...done!
Building JaMoPP model for jgralab... done!
Building JaMoPP model for jsf... done!
Building JaMoPP model for sax... done!
...
```

Then, calculate the metrics and do the architecture recovery!

```
$ pwd
/path/to/sotesola2012
$ lein run
Processing #<File models/jena2.xmi>
    Calculating metrics...done!
    Generating DOT visualization...done!
    Generating PDF...done!
Processing #<File models/javaParseLib.xmi>
    Calculating metrics...done!
    Generating DOT visualization...done!
    Generating PDF...done!
...
```

In the `output/` directory, you'll get one...

- ... txt file per 101 implementation which lists the Kemerer and Chidamber
  metric values for any class of that implementation.  Actually, the *Depth Of
  Inheritance Tree* and the *Number Of Children* metrics are calculated for
  interfaces, too.

- ... `OVERALL_SCORES.txt` file which contains a ranking of the 10 most complex
  classes according to each metric.  The ranking shows the metric value, the
  class with that value, and the 101 implementation that contains that class.

- ... pdf file per 101 implementation which is a poor-man's UML class diagram.
  Generalizations and realizations are shown in the usual UML style, i.e.,
  arrows with empty heads and dashed arrows with empty heads.  Usage
  relationships are shown as arrows with open heads.

On my 4-years old dual core notebook, processing all 46 one-o-one
implementations takes less than two minutes, i.e., approximately 3 seconds per
implementation.  This time includes the loading of the models, the metric and
ranking calculation, the UML-diagram generation, and spitting that stuff to
files.  It does not include the time needed for building the 101
implementations and parsing their java files using JaMoPPC.

Just in case you have no time to run that stuff yourself, I've included a
`sample-output.tar.xz` file in the repository which contains all result files.

Well, that's it.  Have fun!

## License

Copyright © 2012 Tassilo Horn <horn@uni-koblenz.de>

Distributed under the General Public License, Version 3 or later.
