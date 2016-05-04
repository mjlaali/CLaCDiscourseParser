#!/bin/bash

java -Xmx16g -cp target/discourse.parser.argument_labeler-0.1.1.jar:target/dependency/* org.discourse.parser.argument_labeler.argumentLabeler.ArgumentSequenceLabeler
