package searchEngine;

import Jama.*;

public class SVD {
	public static Matrix Uk;
	public static Matrix Sk;
	public static Matrix Vk; //Use Vk for cosine similarity
	public static Matrix A;
	public static Matrix Ak;

	public static void computeSVD(double[][] array, int k) {

		int rows = array.length;
		int columns = array[0].length;
		System.out.println("Size ("+rows+","+columns+")");
		A = new Matrix(array).transpose();
		Matrix B = Matrix.random(rows, 3);
		A = A.times(B).times(B.transpose());
		System.out.print("A = ");
		A.print(columns-1, 6);

		// compute the singular value decomposition (SVD)
		System.out.println("A = U S V^T");
		System.out.println();
		SingularValueDecomposition s = A.svd();
		System.out.print("U = ");
		Matrix U = s.getU();
		U.print(getColumns(U)-1, 6);
		System.out.print("Sigma = ");
		Matrix S = s.getS();
		S.print(getColumns(S)-1, 6);
		System.out.print("V = ");
		Matrix V = s.getV();
		V.print(getColumns(V)-1, 6);
		System.out.println("rank = " + s.rank());
		System.out.println("condition number = " + s.cond());
		System.out.println("2-norm = " + s.norm2());

		// print out singular values
		System.out.print("singular values = ");
		Matrix svalues = new Matrix(s.getSingularValues(), 1);
		svalues.print(9, 6);

		// U = U[:,:k]
		// S = S[:k,:k]
		// V = V[:k,:]
		
		Uk = U.getMatrix(0, getRows(U) - 1, 0, k-1); // U[:,:k]
		Sk = S.getMatrix(0, k-1, 0, k-1); // S[:k,:k]
		Vk = V.getMatrix(0, k-1, 0, getColumns(V) - 1); // V[:k,:]

		Ak = U.times(S).times(V);
		System.out.print("Ak = ");
		Ak.print(getColumns(Ak)-1, 6);

		System.out.print("V"+k+" = ");
		Vk.print(getRows(Vk)-1, getColumns(Vk)-1);
	}
	
	public static double[] transformQuery(double[] querySparse) {
		Matrix q = new Matrix(querySparse, 1); //Create 1-d Matrix
		return (q.times(Uk).times(Sk)).getArray()[0];
	}
	
	public static double[] cosineSimilarity(double[] query) {
		Matrix q = new Matrix(query, 1).transpose(); //Create 1-d Matrix
		int terms = getRows(Vk);
		int docs = getColumns(Vk);
		double[] similarity = new double[docs];
		for(int i=0; i<docs; ++i) {
			Matrix doc = Vk.getMatrix(0, terms-1, i, i); //Get column i
			//Calculate cosine similarity with doc i
			double dotProduct = q.arrayTimes(doc).norm1();
			double euclDist = q.normF() * doc.normF();
			similarity[i] = dotProduct/euclDist;
		}
		return similarity;
	}
	
	public static int getRows(Matrix m) {
		return m.getArray().length;
	}
	
	public static int getColumns(Matrix m) {
		return m.getArray()[0].length;
	}
	
	//Testing
	public static void main(String[] args) {
		double[] test = {1,2,3,4};
		Matrix m = new Matrix(test,1);
		m.print(9, 6);
	}
}
