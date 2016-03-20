#!/bin/bash

java -Xmx16g -cp target/discourse.parser.arg_labeler_nn-0.0.1.jar:target/dependency/* discourse.parser.arg_labeler_nn.ArgLabelerFeatureExtractor
