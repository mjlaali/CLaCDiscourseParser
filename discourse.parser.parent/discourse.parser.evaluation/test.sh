#!/bin/bash

java -cp target/discourse.parser.evaluation-0.0.1.jar:target/dependency/* ca.concordia.clac.parser.evaluation.ConllEvaluation -m $1 -o $2 
