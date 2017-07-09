#!/bin/bash

cd /Users/majid/Documents/git/mjtools-code
mvn install source:jar deploy -U $1
cd /Users/majid/Documents/git/CLaCDiscourseParser/discourse.parser.parent
mvn install source:jar deploy -U $1
cd /Users/majid/Documents/git/europarl/europarl.uima
mvn install source:jar deploy -U $1
cd /Users/majid/Documents/git/french-connective-disambiguation/connective-disambiguation
mvn install source:jar deploy -U $1
cd /Users/majid/Documents/git/disco-parallel/parallel.corpus
mvn install source:jar deploy -U $1
cd /Users/majid/Documents/git/parallel.corpus.sampling/parallel.corpus.sampling
mvn install source:jar deploy -U $1
