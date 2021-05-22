package searchEngine;

import Jama.*;

public class SVD {
   public double[][] SVD(double[][] array) { 

	   int rows = array.length;
	   int columns = array[0].length;
	   Matrix A = new Matrix(array);
	  Matrix B = Matrix.random(rows, 3);
	  A = A.times(B).times(B.transpose());
	  System.out.print("A = ");
	  A.print(9, 6);
	
	  // compute the singular value decomposition (SVD)
	  System.out.println("A = U S V^T");
	  System.out.println();
	  SingularValueDecomposition s = A.svd();
	  System.out.print("U = ");
	  Matrix U = s.getU();
	  U.print(9, 6);
	  System.out.print("Sigma = ");
	  Matrix S = s.getS();
	  S.print(9, 6);
	  System.out.print("V = ");
	  Matrix V = s.getV();
	  V.print(9, 6);
	  System.out.println("rank = " + s.rank());
	  System.out.println("condition number = " + s.cond());
	  System.out.println("2-norm = " + s.norm2());
	
	  // print out singular values
	  System.out.print("singular values = ");
	  Matrix svalues = new Matrix(s.getSingularValues(), 1);
	  svalues.print(9, 6);
	  
	  //U = U[:,:k]
	  //S = S[:k,:k]
	  //V = V[:k,:]
	  System.out.println(U.getArray().length);
	  System.out.println(U.getArray()[0].length);
	  int k=4;
	  U = U.getMatrix(0,rows-1,0,k); //U[:,:k]
	  S = S.getMatrix(0,k,0,k); //S[:k,:k]
	  V = V.getMatrix(0,k,0,columns-1); //V[:k,:]
	  
	  Matrix A_new = U.times(S).times(V);
	  A_new.print(9, 6);
	  return A_new.getArray();
   }
}
