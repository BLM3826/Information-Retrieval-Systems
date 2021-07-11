package searchEngine;

import java.io.IOException;
import java.util.Arrays;

//import com.manning.dl4s.utils.VectorizeUtils;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.SmallFloat;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

/**
 * A {@link Similarity} based on document embeddings generated by averaging {@link Word2Vec} word vectors.
 * Averaging can be done by simple "mean", but also using index stats like tf, idf and tf-idf.
 */
public class WordEmbeddingsSimilarity extends Similarity {

  public enum Smoothing {
    MEAN,
    IDF,
    TF,
    TF_IDF
  }

  private final Word2Vec word2Vec;
  private final String fieldName;
  private final Smoothing smoothing;

  public WordEmbeddingsSimilarity(Word2Vec word2Vec, String fieldName, Smoothing smoothing) {
    this.word2Vec = word2Vec;
    this.fieldName = fieldName;
    this.smoothing = smoothing;
  }

  @Override
  public long computeNorm(FieldInvertState state) {
    final int numTerms = state.getLength() - state.getNumOverlap();
    int indexCreatedVersionMajor = state.getIndexCreatedVersionMajor();
    if (indexCreatedVersionMajor >= 7) {
      return SmallFloat.intToByte4(numTerms);
    } else {
      return SmallFloat.floatToByte315((float) (1 / Math.sqrt(numTerms)));
    }
  }

  @Override
  public SimWeight computeWeight(float boost, CollectionStatistics collectionStats,
                                 TermStatistics... termStats) {
    return new EmbeddingsSimWeight(boost, collectionStats, termStats);
  }

  @Override
  public SimScorer simScorer(SimWeight weight, LeafReaderContext context) throws IOException {
    return new EmbeddingsSimScorer(weight, context);
  }

  private class EmbeddingsSimScorer extends SimScorer {
    private final EmbeddingsSimWeight weight;
    private final LeafReaderContext context;
    private Terms fieldTerms;
    private LeafReader reader;

    public EmbeddingsSimScorer(SimWeight weight, LeafReaderContext context) {
      this.weight = (EmbeddingsSimWeight) weight;
      this.context = context;
      this.reader = context.reader();
    }

    @Override
    public String toString() {
      return "EmbeddingsSimScorer{" +
          "weight=" + weight +
          ", context=" + context +
          ", fieldTerms=" + fieldTerms +
          ", reader=" + reader +
          '}';
    }

    @Override
    public float score(int doc, float freq) throws IOException {
      INDArray denseQueryVector = getQueryVector();
      INDArray denseDocumentVector = VectorizeUtils.toDenseAverageVector(
          reader.getTermVector(doc, fieldName), reader.numDocs(), word2Vec, smoothing);
      return (float) Transforms.cosineSim(denseQueryVector, denseDocumentVector);
    }

    private INDArray getQueryVector() throws IOException {
      INDArray denseQueryVector = Nd4j.zeros(word2Vec.getLayerSize());
      String[] queryTerms = new String[weight.termStats.length];
      int i = 0;
      for (TermStatistics termStats : weight.termStats) {
        queryTerms[i] = termStats.term().utf8ToString();
        i++;
      }

      if (fieldTerms == null) {
        fieldTerms = MultiFields.getTerms(reader, fieldName);
      }

      for (String queryTerm : queryTerms) {
        TermsEnum iterator = fieldTerms.iterator();
        BytesRef term;
        while ((term = iterator.next()) != null) {
          TermsEnum.SeekStatus seekStatus = iterator.seekCeil(term);
          if (seekStatus.equals(TermsEnum.SeekStatus.END)) {
            iterator = fieldTerms.iterator();
          }
          if (seekStatus.equals(TermsEnum.SeekStatus.FOUND)) {
            String string = term.utf8ToString();
            if (string.equals(queryTerm)) {
              INDArray vector = word2Vec.getLookupTable().vector(queryTerm);
              if (vector != null) {
                double tf = iterator.totalTermFreq();
                double docFreq = iterator.docFreq();
                double smooth;
                switch (smoothing) {
                  case MEAN:
                    smooth = queryTerms.length;
                    break;
                  case TF:
                    smooth = tf;
                    break;
                  case IDF:
                    smooth = docFreq;
                    break;
                  case TF_IDF:
                    smooth = VectorizeUtils.tfIdf(reader.numDocs(), tf, docFreq);
                    break;
                  default:
                    smooth = VectorizeUtils.tfIdf(reader.numDocs(), tf, docFreq);
                }
                denseQueryVector.addi(vector.div(smooth));
              }
              break;
            }
          }
        }
      }
      return denseQueryVector;
    }

    @Override
    public float computeSlopFactor(int distance) {
      return 1;
    }

    @Override
    public float computePayloadFactor(int doc, int start, int end, BytesRef payload) {
      return 1;
    }
  }

  private class EmbeddingsSimWeight extends SimWeight {
    private final float boost;
    private final CollectionStatistics collectionStats;
    private final TermStatistics[] termStats;

    public EmbeddingsSimWeight(float boost, CollectionStatistics collectionStats, TermStatistics[] termStats) {
      this.boost = boost;
      this.collectionStats = collectionStats;
      this.termStats = termStats;
    }

    @Override
    public String toString() {
      return "EmbeddingsSimWeight{" +
          "boost=" + boost +
          ", collectionStats=" + collectionStats +
          ", termStats=" + Arrays.toString(termStats) +
          '}';
    }
  }
}
