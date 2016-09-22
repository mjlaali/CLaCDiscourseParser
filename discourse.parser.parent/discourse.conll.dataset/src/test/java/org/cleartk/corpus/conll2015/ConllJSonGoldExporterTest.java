package org.cleartk.corpus.conll2015;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.cleartk.corpus.conll2015.ConllDatasetPath.DatasetMode;
import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;

public class ConllJSonGoldExporterTest {
	static final File JSON_OUTPUT = new File("outputs/export.json");
	
	Function<Character, Boolean> isWhiteSpace = (ch) -> ch.charValue() == ' ';

	@Test
	public void whenGeneratingAFileForTrialDatasetItIsEqualWithNoSenseRelations() throws UIMAException, IOException{
		TokenListTools.isWhiteSpace = this.isWhiteSpace;
		File dataFld = new File("data/");
		ConllDatasetPath datasetPath = new ConllDatasetPathFactory().makeADataset2016(dataFld, DatasetMode.trial);
		
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class, 
				TextReader.PARAM_SOURCE_LOCATION, datasetPath.getRawDirectory(), 
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, "wsj_*");
		
		AnalysisEngineDescription conllSyntaxJsonReader = ConllSyntaxGoldAnnotator.getDescription(datasetPath.getParsesJSonFile());
		AnalysisEngineDescription conllDiscourseJsonReader = ConllDiscourseGoldAnnotator.getDescription(datasetPath.getRelationsJSonFile(), false);
		AnalysisEngineDescription conllGoldJSONExporter = ConllJSonGoldExporter.getDescription(JSON_OUTPUT);
		
		SimplePipeline.runPipeline(reader, conllSyntaxJsonReader, conllDiscourseJsonReader, conllGoldJSONExporter);
		
		Set<JsonElement> outputs = fileToJsonElements(JSON_OUTPUT);
		Set<JsonElement> expected = fileToJsonElements(datasetPath.getRelationsJSonFile());
		
		for (JsonElement element: outputs){
			if (expected.contains(element))
				continue;
			
			System.out.println(expected.contains(element));
			System.out.println(element.toString());
			System.out.println();
			for (JsonElement exp: expected){
				if (exp.getAsJsonObject().get("Type").equals(element.getAsJsonObject().get("Type"))){
					System.out.println(exp.toString());
					for (String key: new String[]{"Type", "DocID", "Sense", "Arg1", "Arg2", "Connective"}){
						if (!element.getAsJsonObject().get(key).equals(exp.getAsJsonObject().get(key))){
							System.out.println("Error: " + key);
							if (key.equals("Arg2")){
								for (String secondKey: new String[]{"CharacterSpanList", "RawText", "TokenList"}){
									if (!element.getAsJsonObject().getAsJsonObject(key).get(secondKey)
											.equals(exp.getAsJsonObject().getAsJsonObject(key).get(secondKey))){
										System.out.println("Internal Error: " + secondKey);
									}
								}
							}
						}
					}
				}
				
			}
			System.out.println();
			System.out.println();
		}
		assertThat(outputs).isEqualTo(expected);
		
		TokenListTools.isWhiteSpace = Character::isWhitespace;
	}
	
	private Set<JsonElement> fileToJsonElements(File file) throws IOException{
		List<String> lines = FileUtils.readLines(file);
		JsonParser parser = new JsonParser();
		
		Set<JsonElement> jsonElements = 
				lines.stream()
				.map((line) -> line.replaceAll("\"ID\": \\d+, ", ""))
				.map((line) -> line.replaceAll("\"CharacterSpanList\": \\[\\], \"RawText\": \"[^\"]+\", \"", "\"CharacterSpanList\": [], \"RawText\": \"\", \""))
				.map((line) -> parser.parse(line))
				.collect(Collectors.toSet());
		return jsonElements;
	}
	
	@Test
	public void compareTwoJson(){
		String str1 = "{\"A\": \"a\", \"B\": \"b\"}";
		String str2 = "{\"B\": \"b\", \"A\": \"a\"}";
		String str3 = "{\"B\": \"b\", \"A\": \"b\"}";
		JsonParser parser = new JsonParser();
		JsonElement json1 = parser.parse(str1);
		JsonElement json2 = parser.parse(str2);
		JsonElement json3 = parser.parse(str3);
		
		assertThat(json1).isEqualTo(json2);
		assertThat(json1.hashCode()).isEqualTo(json2.hashCode());
		assertThat(json1).isNotEqualTo(json3);
	}
	
	@Test
	public void whenExcludeExplicitRelationThenOnlyImplicitEntRelAndAltLexRelationExists() throws UIMAException, IOException{
		File dataFld = new File("data/");
		ConllDatasetPath datasetPath = new ConllDatasetPathFactory().makeADataset2016(dataFld, DatasetMode.trial);
		
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class, 
				TextReader.PARAM_SOURCE_LOCATION, datasetPath.getRawDirectory(), 
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, "wsj_*");
		
		AnalysisEngineDescription conllSyntaxJsonReader = ConllSyntaxGoldAnnotator.getDescription(datasetPath.getParsesJSonFile());
		AnalysisEngineDescription conllDiscourseJsonReader = ConllDiscourseGoldAnnotator.getDescription(datasetPath.getRelationsJSonFile(), false);
		AnalysisEngineDescription conllGoldJSONExporter = ConllJSonGoldExporter.getDescription(JSON_OUTPUT, RelationType.Explicit.toString());
		
		SimplePipeline.runPipeline(reader, conllSyntaxJsonReader, conllDiscourseJsonReader, conllGoldJSONExporter);
		Set<JsonElement> outputs = fileToJsonElements(JSON_OUTPUT);
		
		for (JsonElement element: outputs){
			assertThat(element.getAsJsonObject().get("Type").getAsString()).isNotEqualTo(RelationType.Explicit.toString());
		}
	}
	
}
