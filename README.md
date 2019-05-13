# file-parser


This project accepts a file in one of 3 formats: pipe-delimited, comma-delimited and space-delimited.

It expects a header row, and exactly 5 fields: last name, first name, gender, date of birth and favorite color.

Dates should be in the format M/D/YYYY M/D/YYYY.

## Usage
Call with
```lein run <filename>```

To run with an included sample dataset containing soccer players run

```
lein run test/data/sample.csv
```

The webserver runs on port 8890.

A list of records may be accessed by doing a get of the /records route.

```
curl localhost:8890/records
```

3 sorts are available:
- Gender `curl localhost:8890/records/gender`
- Birthdate `curl localhost:8890/records/birthdate`
- Last Name (descending) `curl localhost:8890/records/name`

To add new records, send a post to /records with a `person` body parameter containing a comma, pipe,
or space delimited string.

```
curl -XPOST -d "person=Hamm,Mia,Female,Blue,3/17/1972" "http://localhost:8890/records"
```

## License

Copyright © 2019 Rick Hall

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
