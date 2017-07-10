#!/bin/bash

java -Xmx16g -cp target/*:target/dependency/* org.discourse.parser.argument_labeler.argumentLabeler.ArgumentSequenceLabeler -a -n -o outputs/resources -c "weka.classifiers.meta.Bagging -P 100 -S 1 -num-slots 10 -I 10 -W weka.classifiers.trees.J48 -- -C 0.25 -M 2"

cp -rf outputs/resources/argumentSequenceLabeler src/main/resources/clacParser/model
