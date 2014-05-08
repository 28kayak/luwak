package uk.co.flax.luwak.termextractor;

import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Copyright (c) 2014 Lemur Consulting Ltd.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class TermWeightor {

    static class WeightedTermList implements Comparable<WeightedTermList> {

        final QueryTermList terms;
        final float weight;

        WeightedTermList(QueryTermList terms, float weight) {
            this.terms = terms;
            this.weight = weight;
        }

        @Override
        public int compareTo(WeightedTermList o) {
            return Float.compare(this.weight, o.weight);
        }
    }

    public QueryTermList selectBest(List<QueryTermList> terms) {

        List<WeightedTermList> weightedTerms = new ArrayList<>();
        for (QueryTermList term : terms) {
            weightedTerms.add(new WeightedTermList(terms, weigh(terms)));
        }

        Collections.sort(weightedTerms);
        WeightedTermList preferred = Iterables.getFirst(weightedTerms, null);
        return preferred == null ? null : preferred.terms;

    }

    public float weigh(QueryTermList terms) {

    }

}