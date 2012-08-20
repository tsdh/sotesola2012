# Problems & Solutions



## Usage

- Parse every 101companies implementation that contains at least one java file
  into a java graph using EMFText's [JaMoPPC](http://www.jamopp.org) parser
  (included in `lib/`).  That's the job of the `build-jamopp-graphs.sh` bash
  script.

```
cd scripts/
./build-jamopp-graphs.sh ~/path/to/101repo/contributions
```

  Thereafter, the folder `models/` contains one xmi file per 101 implementation
  containing java files.  You'll get an error for some projects, e.g., all
  AntLR implementation, because the generated AntLR parsers contain characters
  that are not valid in XML and thus the XMI serialization fails. :-(

## License

Copyright © 2012 Tassilo Horn

Distributed under the General Public License, Version 3 or later.
