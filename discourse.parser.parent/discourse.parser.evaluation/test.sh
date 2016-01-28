#!/bin/bash

java -cp target/discourse.parser.evaluation-0.0.1.jar:target/dependency/* ca.concordia.clac.parser.evaluation.ConllEvaluation -m $1 -o $2 
echo 'Check the perfomance of the outputs ...'
python ../discourse.conll.dataset/data/validator/scorer2.py ../discourse.conll.dataset/data/conll15st-train-dev/conll15st_data/conll15-st-03-04-15-dev/pdtb-data.json outputs/dev/pdtb-data.json 
