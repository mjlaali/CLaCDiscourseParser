#CLaC Discourse Parser


CLaC Discourse Parser is a discourse parser based on the UIMA framework. 
We used ClearTK to add machine learning functionality to the UIMA framework for this parser. 
CLaC Discourse Parser participated in CoNLL Shared Task 2015 and achieves achieves 
a result of 17.3 F1 on the identification of discourse relations on the blind CoNLL-2015 test set, 
ranking in sixth place. For more information please consult with:

<cite> Majid Laali, Elnaz Davoodi, and Leila Kosseim (2015).<b> The CLaC Discourse Parser at CoNLL-2015 </b>. In Proceedings of the Nineteenth Conference on Computational Natural Language Learning: Shared Task. Beijing, China.</cite>

Requires JDK 1.7 or higher, Maven 3.0 or higher and Python 2.6 or higher.

## Quick Start

1. `git clone --recursive git@github.com:mjlaali/CLaCDiscourseParser.git`
2. `cd CLaCDiscourseParser/discourse.parser.parent/`
3. `mvn clean install package dependency:copy-dependencies`
4. `cp -r <SHARED-TASK-DATE> discourse.conll.dataset/data/`
5. `discourse.parser.evaluation/test.sh dev outputs/dev`
6. `python discourse.conll.dataset/data/validator/scorer2.sh discourse.conll.dataset/data/conll15st-train-dev/conll15st_data/conll15-st-03-04-15-dev/pdtb-data.json outputs/dev/pdtb-data.json`


## Copyright notice and statement of copying permission

Copyright (c) 2015, Majid Laali, CLaC Group.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies,
either expressed or implied, of the FreeBSD Project.                                                           
