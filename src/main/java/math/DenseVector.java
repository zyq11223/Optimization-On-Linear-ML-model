package math;

import Utils.LabeledData;

import java.util.Arrays;

/**
 * Created by leleyu on 2016/6/30.
 */
public class DenseVector {
  public int dim;
  public double[] values;

  public DenseVector(int dim) {
    this.dim = dim;
    this.values = new double[dim];
    Arrays.fill(this.values, 0);
  }

  public DenseVector(DenseVector a){
    this.dim = a.dim;
    this.values = new double[dim];
    System.arraycopy(a.values, 0, values, 0, dim);
  }
  //For NesterovMomentum:
  @SuppressWarnings("unused")
  public double dotNesterovMomentum(SparseVector other, double[] v_t, double gamma) {
    int[] indices = other.indices;
    double ret = 0.0;
    if (other.values != null) {
      for (int i = 0; i < indices.length; i ++) {
        ret += values[indices[i]] * (other.values[i] + gamma * v_t[indices[i]]);
      }
    } else {
      for (int i: indices) {
        ret += values[i] * (1 - gamma * v_t[i]);
      }
    }
    return ret;
  }
  public double dot(SparseVector other) {
    int[] indices = other.indices;
    double ret = 0.0;
    if (other.values != null) {
      for (int i = 0; i < indices.length; i ++) {
        ret += values[indices[i]] * other.values[i];
      }
    } else {
      for (int i: indices) {
        ret += values[i];
      }
    }
    return ret;
  }
    @SuppressWarnings("unused")

  public void plusBy(SparseVector other, double x) {
    int[] indices = other.indices;
    for (int i: indices) {
      values[i] += x;
    }
  }
    @SuppressWarnings("unused")

    public void allPlusBy(double x){
    for(int i = 0; i < values.length; i++){
      values[i] += x;
    }
  }
  public void allDividedBy(int x){
    assert (x != 0);
    for(int i = 0; i < values.length; i++){
      values[i] /= x;
    }
  }
  @SuppressWarnings("unused")
  public void positiveOrZero(SparseVector other){
    for(int i = 0; i <  other.indices.length; i++){
      int idx = other.indices[i];
      values[idx] = Math.max(0, values[idx]);
      if(Double.isNaN(values[idx]) || Double.isInfinite(values[idx])){
            System.out.println(idx);
      }
    }
  }
  @SuppressWarnings("unused")
  public void positiveOrZero(){
    for(int i = 0; i < values.length; i++){
      values[i] = Math.max(0, values[i]);
    }
  }
  @SuppressWarnings("unused")

    public void plusAndPositive(double x){
    for(int i = 0; i < values.length; i++){
      values[i] += x;
      values[i] = Math.max(0, values[i]);
    }
  }
  public void plusGradient(SparseVector other, double scala){
    for(int i = 0; i <  other.indices.length; i++){
      int idx = other.indices[i];
      if(other.values != null){
        values[idx] += scala * other.values[i];
      }else{
        values[idx] += scala;
      }
    }
  }
    @SuppressWarnings("unused")

  public void plusSparse(SparseVector other, double scala){
    for(int i = 0; i <  other.indices.length; i++){
      int idx = other.indices[i];
      values[idx] += scala;
    }
  }
  public void multiplySparse(SparseVector other, double scala){
    for(int i = 0; i <  other.indices.length; i++){
      int idx = other.indices[i];
      values[idx] *= (1 + scala);
    }
  }

  public void plusDense(DenseVector d){
    assert (this.dim == d.dim);
    for(int i = 0; i < d.dim; i++){
      values[i] += d.values[i];
    }
  }

  public void update(SparseVector other, double modelPenalty, double gradient) {
    for(int i = 0; i <  other.indices.length; i++){
        int idx = other.indices[i];
        if(values[idx] > 0) {
            values[idx] = Math.max(values[idx] - Math.abs(modelPenalty) + gradient * (other.values==null? 1: other.values[i]), 0);
        }
        else {
            values[idx] = Math.max(values[idx] + gradient * (other.values==null? 1: other.values[i]), 0);
        }
    }
  }
}
