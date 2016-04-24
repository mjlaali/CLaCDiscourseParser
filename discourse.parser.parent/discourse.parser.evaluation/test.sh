#!/bin/bash

java -cp target/discourse.parser.evaluation-0.1.1.jar:target/dependency/* ca.concordia.clac.parser.evaluation.ConllEvaluation -i $1 -m $2 -o $3
