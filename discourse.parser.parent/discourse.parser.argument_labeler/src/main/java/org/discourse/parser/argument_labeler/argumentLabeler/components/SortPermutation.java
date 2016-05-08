package org.discourse.parser.argument_labeler.argumentLabeler.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class SortPermutation {
	public static <T> List<Integer> getPermutations(final List<T> list, final Comparator<? super T> comparator){
		List<Integer> permutation = new ArrayList<>();
		for (int i = 0; i < list.size(); i++)
			permutation.add(i);
		
		Collections.sort(permutation, (a, b) -> comparator.compare(list.get(a), list.get(b)));
		
		return permutation;
	}
}