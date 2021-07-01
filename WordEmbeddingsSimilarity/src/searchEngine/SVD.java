package searchEngine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import Jama.*;
import txtparsing.DocSimilarity;

public class SVD {
	public static Matrix Uk;
	public static Matrix Sk;
	public static Matrix Vk; //Use Vk for cosine similarity
	public static Matrix A;
	public static Matrix Ak;

	public static void computeSVD(double[][] array, int[] k_array) {

		int rows = array.length;
		int columns = array[0].length;
		System.out.println("Size ("+rows+","+columns+")");
		A = new Matrix(array).transpose(); //Columns are Docs
//		Matrix B = Matrix.random(rows, 3);
//		A = A.times(B).times(B.transpose());
//		System.out.print("A = ");
//		A.print(columns-1, 1);

		// compute the singular value decomposition (SVD)
//		System.out.println("A = U S V^T");
//		System.out.println();
		SingularValueDecomposition s = A.svd();
//		System.out.print("U = ");
		Matrix U = s.getU();
//		U.print(getColumns(U)-1, 6);
//		System.out.print("Sigma = ");
		Matrix S = s.getS();
//		S.print(getColumns(S)-1, 6);
//		System.out.print("V = ");
		Matrix V = s.getV();
//		V.print(getColumns(V)-1, 6);
		System.out.println("rank = " + s.rank());
		System.out.println("condition number = " + s.cond());
		System.out.println("2-norm = " + s.norm2());

		// print out singular values
//		System.out.print("singular values = ");
//		Matrix svalues = new Matrix(s.getSingularValues(), 1);
//		svalues.print(9, 6);

		// U = U[:,:k]
		// S = S[:k,:k]
		// V = V[:k,:]
		
		for(int k : k_array) {
			Uk = U.getMatrix(0, getRows(U) - 1, 0, k-1); // U[:,:k]
			Sk = S.getMatrix(0, k-1, 0, k-1); // S[:k,:k]
			Vk = V.getMatrix(0, k-1, 0, getColumns(V) - 1); // V[:k,:]

			saveMatrix(Vk, "index/V"+k+".txt");
			saveMatrix(Uk, "index/U"+k+".txt");
			saveMatrix(Sk, "index/S"+k+".txt");
		}
//		Ak = U.times(S).times(V);
//		System.out.print("Ak = ");
//		Ak.print(getColumns(Ak)-1, 6);

//		System.out.print("V"+k+" = ");
//		Vk.print(getRows(Vk)-1, getColumns(Vk)-1);

	}
	
	public static double[] transformQuery(double[] querySparse) {
		Matrix q = new Matrix(querySparse, 1); //Create 1-d Matrix
		return (q.times(Uk).times(Sk)).getArray()[0];
	}
	
	public static DocSimilarity[] cosineSimilarity(double[] query) {
		Matrix q = new Matrix(query, 1).transpose(); //Create 1-d Matrix
		int terms = getRows(Vk);
		int docs = getColumns(Vk);
		DocSimilarity[] similarity = new DocSimilarity[docs];
		for(int i=0; i<docs; ++i) {
			Matrix doc = Vk.getMatrix(0, terms-1, i, i); //Get column i
			//Calculate cosine similarity with doc i
			double dotProduct = q.arrayTimes(doc).norm1();
			double euclDist = q.normF() * doc.normF();
//			similarity[i] = dotProduct/euclDist;
			similarity[i] = new DocSimilarity(i, dotProduct/euclDist);
		}
		return similarity;
	}
	
	public static void saveMatrix(Matrix matrix, String file) {
		File VkArrayFile = new File(file);
		try {
			VkArrayFile.createNewFile();
			FileWriter writer = new FileWriter(VkArrayFile);
			for (int i = 0; i < getRows(matrix); i++) {
				for (int j = 0; j < getColumns(matrix); j++) {
					writer.write(matrix.get(i,j) + " ");
				}
					writer.write("\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void reloadVk(String file) {
		Vk = reloadMatrix(file);
	}
	
	public static void reloadUk(String file) {
		Uk = reloadMatrix(file);
	}

	public static void reloadSk(String file) {
		Sk = reloadMatrix(file);
	}
	
	private static Matrix reloadMatrix(String file) {
		File VkArrayFile = new File(file);
		try {
			 Scanner reader = new Scanner(VkArrayFile);
			 System.out.println("Reload Matrix from "+file+"...");
			 List<double[]> temp = new ArrayList<double[]>();
			 String[] row = null;
		      while (reader.hasNextLine()) {
		    	//Split row
		        row = reader.nextLine().split(" ");
		        double[] nums = new double[row.length]; 
		        for(int i=0; i<row.length; ++i) { 
		        	nums[i] = Double.parseDouble(row[i]); //Get doubles
		        }
		        temp.add(nums);
		      }
		      reader.close();
		      
		      double[][] VkArray = new double[temp.size()][row.length];
		      temp.toArray(VkArray);
		      return new Matrix(VkArray);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
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